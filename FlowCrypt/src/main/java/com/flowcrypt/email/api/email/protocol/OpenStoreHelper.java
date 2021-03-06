/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email.protocol;

import android.content.Context;

import com.flowcrypt.email.BuildConfig;
import com.flowcrypt.email.api.email.JavaEmailConstants;
import com.flowcrypt.email.api.email.gmail.GmailConstants;
import com.flowcrypt.email.api.email.model.AuthCredentials;
import com.flowcrypt.email.database.dao.source.AccountDao;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.sun.mail.gimap.GmailSSLStore;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * This util class help generate Store classes.
 *
 * @author DenBond7
 *         Date: 03.05.2017
 *         Time: 18:00
 *         E-mail: DenBond7@gmail.com
 */

public class OpenStoreHelper {

    /**
     * Open and connect to the store using gimaps protocol.
     *
     * @param token       An OAuth2 access token;
     * @param accountName An account name which use to create connection;
     * @return <tt>GmailSSLStore</tt> A GmailSSLStore object based on properties for
     * gimaps.
     */

    public static GmailSSLStore openAndConnectToGimapsStore(String token, String accountName) throws
            MessagingException {
        GmailSSLStore gmailSSLStore = (GmailSSLStore) getGmailSession().getStore(JavaEmailConstants.PROTOCOL_GIMAPS);
        gmailSSLStore.connect(GmailConstants.GMAIL_IMAP_SERVER, accountName, token);
        return gmailSSLStore;
    }

    /**
     * Open and connect to the store using gimaps protocol.
     *
     * @param context    Interface to global information about an application environment.
     * @param session    The session which will be used for connection.
     * @param accountDao The object which contains information about an email account.
     * @return <tt>GmailSSLStore</tt> A GmailSSLStore object based on properties for
     * gimaps.
     */

    public static GmailSSLStore openAndConnectToGimapsStore(Context context, Session session, AccountDao accountDao)
            throws MessagingException, IOException, GoogleAuthException {
        GmailSSLStore gmailSSLStore;
        if (accountDao != null) {
            gmailSSLStore = (GmailSSLStore) session.getStore(JavaEmailConstants.PROTOCOL_GIMAPS);
            if (accountDao.getAccount() != null) {
                gmailSSLStore.connect(GmailConstants.GMAIL_IMAP_SERVER, accountDao.getEmail(),
                        GoogleAuthUtil.getToken(context, accountDao.getAccount(),
                                JavaEmailConstants.OAUTH2 + GmailConstants.SCOPE_MAIL_GOOGLE_COM));
            } else throw new NullPointerException("Account can't be a null!");
        } else throw new NullPointerException("AccountDao can't be a null!");
        return gmailSSLStore;
    }

    /**
     * Generate a session for gimaps protocol.
     *
     * @return <tt>Session</tt> A new session for gimaps protocol based on properties for gimaps.
     */
    public static Session getGmailSession() {
        Session session = Session.getInstance(PropertiesHelper.generatePropertiesForGmail());
        session.setDebug(BuildConfig.DEBUG);
        return session;
    }

    /**
     * Generate a session which will be use for download attachments.
     *
     * @return <tt>Session</tt> A new session based on for download attachments.
     */
    public static Session getAttachmentSession(AccountDao accountDao) {
        if (accountDao != null) {
            switch (accountDao.getAccountType()) {
                case AccountDao.ACCOUNT_TYPE_GOOGLE:
                    return getAttachmentGmailSession();

                default:
                    Session session = Session.getInstance(
                            PropertiesHelper.generatePropertiesForDownloadAttachments(accountDao.getAuthCredentials()));
                    session.setDebug(BuildConfig.DEBUG);
                    return session;
            }
        } else throw new NullPointerException("AccountDao must not be a null!");
    }

    /**
     * Generate a session for gimaps protocol which will be use for download attachments.
     *
     * @return <tt>Session</tt> A new session for gimaps protocol based on properties for gimaps.
     */
    public static Session getAttachmentGmailSession() {
        Session session = Session.getInstance(PropertiesHelper.generatePropertiesForDownloadGmailAttachments());
        session.setDebug(BuildConfig.DEBUG);
        return session;
    }

    public static Session getSessionForAccountDao(AccountDao accountDao) {
        if (accountDao != null) {
            switch (accountDao.getAccountType()) {
                case AccountDao.ACCOUNT_TYPE_GOOGLE:
                    return getGmailSession();

                default:
                    Session session = Session.getInstance(
                            PropertiesHelper.generatePropertiesFromAuthCredentials(accountDao.getAuthCredentials()));
                    session.setDebug(BuildConfig.DEBUG);
                    return session;
            }
        } else throw new NullPointerException("AccountDao must not be a null!");
    }

    public static Store openAndConnectToStore(Context context, AccountDao accountDao, Session session) throws
            MessagingException, IOException, GoogleAuthException {
        if (accountDao != null) {
            switch (accountDao.getAccountType()) {
                case AccountDao.ACCOUNT_TYPE_GOOGLE:
                    return OpenStoreHelper.openAndConnectToGimapsStore(context, session, accountDao);

                default:
                    Store store = session.getStore(JavaEmailConstants.PROTOCOL_IMAP);
                    AuthCredentials authCredentials = accountDao.getAuthCredentials();
                    store.connect(authCredentials.getImapServer(), authCredentials.getUsername(),
                            authCredentials.getPassword());
                    return store;
            }
        } else throw new NullPointerException("AccountDao must not be a null!");
    }
}
