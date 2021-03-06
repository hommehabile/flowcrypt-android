/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flowcrypt.email.BuildConfig;
import com.flowcrypt.email.R;
import com.flowcrypt.email.api.email.Folder;
import com.flowcrypt.email.api.email.FoldersManager;
import com.flowcrypt.email.api.email.sync.SyncErrorTypes;
import com.flowcrypt.email.database.dao.source.AccountDao;
import com.flowcrypt.email.database.dao.source.AccountDaoSource;
import com.flowcrypt.email.database.dao.source.imap.ImapLabelsDaoSource;
import com.flowcrypt.email.database.provider.FlowcryptContract;
import com.flowcrypt.email.model.MessageEncryptionType;
import com.flowcrypt.email.service.CheckClipboardToFindPrivateKeyService;
import com.flowcrypt.email.service.EmailSyncService;
import com.flowcrypt.email.ui.activity.base.BaseSyncActivity;
import com.flowcrypt.email.ui.activity.fragment.EmailListFragment;
import com.flowcrypt.email.ui.activity.settings.SettingsActivity;
import com.flowcrypt.email.util.GeneralUtil;
import com.flowcrypt.email.util.UIUtil;
import com.flowcrypt.email.util.google.GoogleApiClientHelper;
import com.flowcrypt.email.util.graphics.glide.transformations.CircleTransformation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

/**
 * This activity used to show messages list.
 *
 * @author DenBond7
 *         Date: 27.04.2017
 *         Time: 16:12
 *         E-mail: DenBond7@gmail.com
 */
public class EmailManagerActivity extends BaseSyncActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener, EmailListFragment.OnManageEmailsListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String EXTRA_KEY_ACCOUNT_DAO = GeneralUtil.generateUniqueExtraKey(
            "EXTRA_KEY_ACCOUNT_DAO", EmailManagerActivity.class);
    public static final int REQUEST_CODE_ADD_NEW_ACCOUNT = 100;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private AccountDao accountDao;
    private FoldersManager foldersManager;
    private Folder folder;
    private LinearLayout accountManagementLayout;
    private GoogleApiClient googleApiClient;

    public EmailManagerActivity() {
        this.foldersManager = new FoldersManager();
    }

    /**
     * This method can bu used to start {@link EmailManagerActivity}.
     *
     * @param context    Interface to global information about an application environment.
     * @param accountDao The object which contains information about an email account.
     */
    public static void runEmailManagerActivity(Context context, AccountDao accountDao) {
        Intent intentRunEmailManagerActivity = new Intent(context, EmailManagerActivity.class);
        intentRunEmailManagerActivity.putExtra(EmailManagerActivity.EXTRA_KEY_ACCOUNT_DAO, accountDao);
        context.stopService(new Intent(context, CheckClipboardToFindPrivateKeyService.class));
        context.startActivity(intentRunEmailManagerActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = GoogleApiClientHelper.generateGoogleApiClient(this, this, this, this, GoogleApiClientHelper
                .generateGoogleSignInOptions());

        if (getIntent() != null) {
            accountDao = getIntent().getParcelableExtra(EXTRA_KEY_ACCOUNT_DAO);
            if (accountDao == null) {
                throw new IllegalArgumentException("You must pass an AccountDao to this activity.");
            }

            getSupportLoaderManager().initLoader(R.id.loader_id_load_gmail_labels, null, this);
        } else {
            finish();
        }

        initViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (drawerLayout != null) {
            drawerLayout.removeDrawerListener(actionBarDrawerToggle);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_NEW_ACCOUNT:
                switch (resultCode) {
                    case RESULT_OK:
                        EmailSyncService.switchAccount(EmailManagerActivity.this);
                        AccountDao accountDao = data.getParcelableExtra(AddNewAccountActivity.KEY_EXTRA_NEW_ACCOUNT);
                        if (accountDao != null) {
                            runEmailManagerActivity(EmailManagerActivity.this, accountDao);
                            finish();
                        }
                        break;
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onReplyFromSyncServiceReceived(int requestCode, int resultCode, Object obj) {
        switch (requestCode) {
            case R.id.syns_request_code_update_label:
                getSupportLoaderManager().restartLoader(R.id.loader_id_load_gmail_labels, null,
                        EmailManagerActivity.this);
                break;

            case R.id.syns_request_code_load_next_messages:
                onNextMessagesLoaded(resultCode == EmailSyncService.REPLY_RESULT_CODE_NEED_UPDATE);
                break;

            case R.id.syns_request_code_force_load_new_messages:
                onForceLoadNewMessagesCompleted(resultCode == EmailSyncService.REPLY_RESULT_CODE_NEED_UPDATE);
                break;
        }
    }

    @Override
    public void onErrorFromSyncServiceReceived(int requestCode, int errorType, Exception e) {
        switch (requestCode) {
            case R.id.syns_request_code_load_next_messages:
            case R.id.syns_request_code_force_load_new_messages:
                notifyEmailListFragmentAboutError(requestCode, errorType, e);
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        updateLabels(R.id.syns_request_code_update_label);
    }

    @Override
    public View getRootView() {
        return drawerLayout;
    }

    @Override
    public boolean isDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public int getContentViewResourceId() {
        return R.layout.activity_email_manager;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigationMenuLogOut:
                logout();
                break;

            case R.id.navigationMenuActionSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.navigationMenuDevSettings:
                startActivity(new Intent(this, DevSettingsActivity.class));
                break;

            case Menu.NONE:
                Folder newFolder = foldersManager.getFolderByAlias(item.getTitle().toString());
                if (folder == null || !folder.getServerFullFolderName().equals(newFolder.getServerFullFolderName())) {
                    this.folder = newFolder;
                    updateEmailsListFragmentAfterFolderChange();
                }
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.loader_id_load_gmail_labels:
                return new CursorLoader(this, new ImapLabelsDaoSource().getBaseContentUri(), null,
                        ImapLabelsDaoSource.COL_EMAIL + " = ?", new String[]{accountDao.getEmail()}, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case R.id.loader_id_load_gmail_labels:
                if (data != null) {
                    ImapLabelsDaoSource imapLabelsDaoSource = new ImapLabelsDaoSource();

                    if (data.getCount() > 0) {
                        foldersManager.clear();
                    }

                    while (data.moveToNext()) {
                        foldersManager.addFolder(imapLabelsDaoSource.getFolder(data));
                    }

                    if (!foldersManager.getAllFolders().isEmpty()) {
                        MenuItem mailLabels = navigationView.getMenu().findItem(R.id.mailLabels);
                        mailLabels.getSubMenu().clear();

                        for (Folder s : foldersManager.getServerFolders()) {
                            mailLabels.getSubMenu().add(s.getFolderAlias());
                        }

                        for (Folder s : foldersManager.getCustomLabels()) {
                            mailLabels.getSubMenu().add(s.getFolderAlias());
                        }
                    }

                    if (folder == null) {
                        folder = foldersManager.getFolderInbox();
                        if (folder == null) {
                            folder = foldersManager.findInboxFolder();
                        }

                        updateEmailsListFragmentAfterFolderChange();
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatActionButtonCompose:
                startActivity(CreateMessageActivity.generateIntent(this, accountDao.getEmail(), null,
                        MessageEncryptionType.ENCRYPTED));
                break;

            case R.id.viewIdAddNewAccount:
                startActivityForResult(new Intent(this, AddNewAccountActivity.class), REQUEST_CODE_ADD_NEW_ACCOUNT);
                break;
        }
    }

    @Override
    public AccountDao getCurrentAccountDao() {
        return accountDao;
    }

    @Override
    public Folder getCurrentFolder() {
        return folder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        UIUtil.showInfoSnackbar(getRootView(), connectionResult.getErrorMessage());
    }

    private void logout() {
        AccountDaoSource accountDaoSource = new AccountDaoSource();
        List<AccountDao> accountDaoList = accountDaoSource.getAccountsWithoutActive(this, accountDao.getEmail());

        switch (accountDao.getAccountType()) {
            case AccountDao.ACCOUNT_TYPE_GOOGLE:
                if (googleApiClient != null && googleApiClient.isConnected()) {
                    GoogleApiClientHelper.signOutFromGoogleAccount(this, googleApiClient);
                } else {
                    showInfoSnackbar(getRootView(), getString(R.string.google_api_is_not_available));
                }
                break;
        }

        if (accountDao != null) {
            getContentResolver().delete(Uri.parse(FlowcryptContract.AUTHORITY_URI + "/"
                    + FlowcryptContract.CLEAN_DATABASE), null, new String[]{accountDao.getEmail()});
        }

        if (accountDaoList != null && !accountDaoList.isEmpty()) {
            AccountDao newActiveAccount = accountDaoList.get(0);
            new AccountDaoSource().setActiveAccount(EmailManagerActivity.this, newActiveAccount.getEmail());
            EmailSyncService.switchAccount(EmailManagerActivity.this);
            runEmailManagerActivity(EmailManagerActivity.this, newActiveAccount);
        } else {
            stopService(new Intent(this, EmailSyncService.class));
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        finish();
    }

    /**
     * Handle an error from the sync service.
     *
     * @param requestCode The unique request code for the reply to {@link android.os.Messenger}.
     * @param errorType   The {@link SyncErrorTypes}
     * @param e           The exception which happened.
     */
    private void notifyEmailListFragmentAboutError(int requestCode, int errorType, Exception e) {
        EmailListFragment emailListFragment = (EmailListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.emailListFragment);

        if (emailListFragment != null) {
            emailListFragment.onErrorOccurred(requestCode, errorType, e);
        }
    }

    /**
     * Handle a result from the load new messages action.
     *
     * @param needToRefreshList true if we must reload the emails list.
     */
    private void onForceLoadNewMessagesCompleted(boolean needToRefreshList) {
        EmailListFragment emailListFragment = (EmailListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.emailListFragment);

        if (emailListFragment != null) {
            emailListFragment.onForceLoadNewMessagesCompleted(needToRefreshList);
        }
    }

    /**
     * Handle a result from the load next messages action.
     *
     * @param needToRefreshList true if we must reload the emails list.
     */
    private void onNextMessagesLoaded(boolean needToRefreshList) {
        EmailListFragment emailListFragment = (EmailListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.emailListFragment);

        if (emailListFragment != null) {
            emailListFragment.onNextMessagesLoaded(needToRefreshList);
        }
    }

    /**
     * Update the list of emails after changing the folder.
     */
    private void updateEmailsListFragmentAfterFolderChange() {
        EmailListFragment emailListFragment = (EmailListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.emailListFragment);

        if (emailListFragment != null) {
            emailListFragment.updateList(true);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new CustomDrawerToggle(this, drawerLayout, getToolbar(),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.addHeaderView(generateAccountManagementLayout());

        MenuItem navigationMenuDevSettings = navigationView.getMenu().findItem(R.id.navigationMenuDevSettings);
        if (navigationMenuDevSettings != null) {
            navigationMenuDevSettings.setVisible(BuildConfig.DEBUG);
        }

        if (findViewById(R.id.floatActionButtonCompose) != null) {
            findViewById(R.id.floatActionButtonCompose).setOnClickListener(this);
        }

        initUserProfileView(navigationView.getHeaderView(0));
    }

    /**
     * Init the user profile in the top of the navigation view.
     *
     * @param view The view which contains user profile views.
     */
    private void initUserProfileView(View view) {
        ImageView imageViewUserPhoto = view.findViewById(R.id.imageViewUserPhoto);
        TextView textViewUserDisplayName = view.findViewById(R.id.textViewUserDisplayName);
        TextView textViewUserEmail = view.findViewById(R.id.textViewUserEmail);

        if (accountDao != null) {
            if (TextUtils.isEmpty(accountDao.getDisplayName())) {
                textViewUserDisplayName.setVisibility(View.GONE);
            } else {
                textViewUserDisplayName.setText(accountDao.getDisplayName());
            }
            textViewUserEmail.setText(accountDao.getEmail());

            if (!TextUtils.isEmpty(accountDao.getPhotoUrl())) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.transform(new CircleTransformation());
                requestOptions.error(R.mipmap.ic_account_default_photo);

                Glide.with(this)
                        .load(accountDao.getPhotoUrl())
                        .apply(requestOptions)
                        .into(imageViewUserPhoto);
            }
        }

        View currentAccountDetailsItem = view.findViewById(R.id.layoutUserDetails);
        final ImageView imageViewExpandAccountManagement = view.findViewById(R.id.imageViewExpandAccountManagement);
        if (currentAccountDetailsItem != null) {
            handleClickOnAccountManagementButton(currentAccountDetailsItem, imageViewExpandAccountManagement);
        }
    }

    private void handleClickOnAccountManagementButton(View currentAccountDetailsItem, final ImageView imageView) {
        currentAccountDetailsItem.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded;

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    imageView.setImageResource(R.mipmap.ic_arrow_drop_down);
                    navigationView.getMenu().setGroupVisible(0, true);
                    accountManagementLayout.setVisibility(View.GONE);
                } else {
                    imageView.setImageResource(R.mipmap.ic_arrow_drop_up);
                    navigationView.getMenu().setGroupVisible(0, false);
                    accountManagementLayout.setVisibility(View.VISIBLE);
                }

                isExpanded = !isExpanded;
            }
        });
    }

    /**
     * Generate view which contains information about added accounts and using him we can add a new one.
     *
     * @return The generated view.
     */
    private ViewGroup generateAccountManagementLayout() {
        accountManagementLayout = new LinearLayout(this);
        accountManagementLayout.setOrientation(LinearLayout.VERTICAL);
        accountManagementLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        accountManagementLayout.setVisibility(View.GONE);

        List<AccountDao> accountDaoList = new AccountDaoSource().getAccountsWithoutActive(this, accountDao.getEmail());
        for (final AccountDao accountDao : accountDaoList) {
            accountManagementLayout.addView(generateAccountItemView(accountDao));
        }

        View addNewAccountView = LayoutInflater.from(this).inflate(R.layout.add_account,
                accountManagementLayout, false);
        addNewAccountView.setId(R.id.viewIdAddNewAccount);
        addNewAccountView.setOnClickListener(this);
        accountManagementLayout.addView(addNewAccountView);

        return accountManagementLayout;
    }

    private View generateAccountItemView(final AccountDao accountDao) {
        View accountItemView = LayoutInflater.from(this).inflate(R.layout.nav_menu_account_item,
                accountManagementLayout, false);
        accountItemView.setTag(accountDao);

        ImageView imageViewUserPhoto = accountItemView.findViewById(R.id.imageViewUserPhoto);
        TextView textViewUserDisplayName = accountItemView.findViewById(R.id.textViewUserDisplayName);
        TextView textViewUserEmail = accountItemView.findViewById(R.id.textViewUserEmail);

        if (accountDao != null) {
            if (TextUtils.isEmpty(accountDao.getDisplayName())) {
                textViewUserDisplayName.setVisibility(View.GONE);
            } else {
                textViewUserDisplayName.setText(accountDao.getDisplayName());
            }
            textViewUserEmail.setText(accountDao.getEmail());

            if (!TextUtils.isEmpty(accountDao.getPhotoUrl())) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.transform(new CircleTransformation());
                requestOptions.error(R.mipmap.ic_account_default_photo);

                Glide.with(this)
                        .load(accountDao.getPhotoUrl())
                        .apply(requestOptions)
                        .into(imageViewUserPhoto);
            }
        }

        accountItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if (accountDao != null) {
                    new AccountDaoSource().setActiveAccount(EmailManagerActivity.this, accountDao.getEmail());
                    EmailSyncService.switchAccount(EmailManagerActivity.this);
                    runEmailManagerActivity(EmailManagerActivity.this, accountDao);
                }
            }
        });

        return accountItemView;
    }

    /**
     * The custom realization of {@link ActionBarDrawerToggle}. Will be used to start a labels
     * update task when the drawer will be opened.
     */
    private class CustomDrawerToggle extends ActionBarDrawerToggle {

        CustomDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar,
                           @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            if (GeneralUtil.isInternetConnectionAvailable(EmailManagerActivity.this)) {
                updateLabels(R.id.syns_request_code_update_label);
            }

            getSupportLoaderManager().restartLoader(R.id.loader_id_load_gmail_labels, null, EmailManagerActivity.this);
        }
    }
}
