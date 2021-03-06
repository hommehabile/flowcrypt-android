/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email;

import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;

/**
 * @author Denis Bondarenko
 *         Date: 29.09.2017
 *         Time: 15:31
 *         E-mail: DenBond7@gmail.com
 */

public class EmailUtil {
    /**
     * Generate an unique content id.
     *
     * @return A generated unique content id.
     */
    public static String generateContentId() {
        return "<" + UUID.randomUUID().toString() + "@flowcrypt" + ">";
    }

    /**
     * Check if current folder has {@link JavaEmailConstants#FOLDER_ATTRIBUTE_NO_SELECT}. If the
     * folder contains it attribute we will not show this folder in the list.
     *
     * @param imapFolder The {@link IMAPFolder} object.
     * @return true if current folder contains attribute
     * {@link JavaEmailConstants#FOLDER_ATTRIBUTE_NO_SELECT}, false otherwise.
     * @throws MessagingException
     */
    public static boolean isFolderHasNoSelectAttribute(IMAPFolder imapFolder) throws MessagingException {
        List<String> attributes = Arrays.asList(imapFolder.getAttributes());
        return attributes.contains(JavaEmailConstants.FOLDER_ATTRIBUTE_NO_SELECT);
    }

    /**
     * Get a domain of some email.
     *
     * @return The domain of some email.
     */
    public static String getDomain(String email) {
        if (TextUtils.isEmpty(email)) {
            return "";
        } else if (email.contains("@")) {
            return email.substring(email.indexOf('@') + 1, email.length());
        } else {
            return "";
        }
    }
}
