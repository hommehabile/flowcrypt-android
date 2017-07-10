/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (tom@cryptup.org).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email.sync.tasks;

import android.os.Messenger;
import android.text.TextUtils;

import com.flowcrypt.email.api.email.JavaEmailConstants;
import com.flowcrypt.email.api.email.gmail.GmailConstants;
import com.flowcrypt.email.api.email.sync.SyncListener;
import com.sun.mail.gimap.GmailRawSearchTerm;
import com.sun.mail.gimap.GmailSSLStore;
import com.sun.mail.imap.IMAPFolder;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

/**
 * This task load the private keys from the user Gmail INBOX.
 *
 * @author DenBond7
 *         Date: 05.07.2017
 *         Time: 10:27
 *         E-mail: DenBond7@gmail.com
 */

public class LoadPrivateKeysFromGmailSynsTask extends BaseSyncTask {
    private String searchTermString;

    /**
     * The base constructor.
     *
     * @param searchTermString The search phrase.
     * @param ownerKey         The name of the reply to {@link Messenger}.
     * @param requestCode      The unique request code for the reply to {@link Messenger}.
     */
    public LoadPrivateKeysFromGmailSynsTask(String searchTermString,
                                            String ownerKey, int requestCode) {
        super(ownerKey, requestCode);
        this.searchTermString = searchTermString;
    }

    @Override
    public void run(GmailSSLStore gmailSSLStore, SyncListener syncListener) throws Exception {
        super.run(gmailSSLStore, syncListener);

        if (syncListener != null) {
            IMAPFolder imapFolder =
                    (IMAPFolder) gmailSSLStore.getFolder(GmailConstants.FOLDER_NAME_INBOX);
            imapFolder.open(Folder.READ_ONLY);

            List<String> keys = new ArrayList<>();

            Message[] foundMessages = imapFolder.search(new GmailRawSearchTerm(searchTermString));

            for (Message message : foundMessages) {
                String key = getKeyFromMessageIfItExists(message);
                if (!TextUtils.isEmpty(key) && !keys.contains(key)) {
                    keys.add(key);
                }
            }

            syncListener.onPrivateKeyFound(keys, ownerKey, requestCode);

            imapFolder.close(false);
        }
    }

    /**
     * Get a private key from {@link Message}, if it exists in.
     *
     * @param message The original {@link Message} object.
     * @return <tt>String</tt> A private key.
     * @throws MessagingException
     * @throws IOException
     */
    private String getKeyFromMessageIfItExists(Message message) throws MessagingException,
            IOException {
        if (message.isMimeType(JavaEmailConstants.MIME_TYPE_MULTIPART)) {
            Multipart multiPart = (Multipart) message.getContent();
            int numberOfParts = multiPart.getCount();
            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                BodyPart bodyPart = multiPart.getBodyPart(partCount);
                if (bodyPart instanceof MimeBodyPart) {
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                    if (Part.ATTACHMENT.equalsIgnoreCase(mimeBodyPart.getDisposition())) {
                        return IOUtils.toString(mimeBodyPart.getInputStream(),
                                StandardCharsets.UTF_8);
                    }
                }
            }
        }

        return null;
    }
}