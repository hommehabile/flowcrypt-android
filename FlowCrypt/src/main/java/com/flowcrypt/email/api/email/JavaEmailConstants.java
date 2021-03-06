/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email;

/**
 * This class described JavaEmail constants.
 *
 * @author DenBond7
 *         Date: 28.04.2017
 *         Time: 9:40
 *         E-mail: DenBond7@gmail.com
 */

public class JavaEmailConstants {
    /*IMAP*/
    public static final String PROPERTY_NAME_MAIL_IMAP_SSL_ENABLE = "mail.imap.ssl.enable";
    public static final String PROPERTY_NAME_MAIL_IMAP_STARTTLS_ENABLE = "mail.imap.starttls.enable";
    public static final String PROPERTY_NAME_MAIL_IMAP_AUTH_MECHANISMS = "mail.imap.auth.mechanisms";
    public static final String PROPERTY_NAME_MAIL_IMAP_FETCH_SIZE = "mail.imap.fetchsize";

    /*SMTP*/
    public static final String PROPERTY_NAME_MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROPERTY_NAME_MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";
    public static final String PROPERTY_NAME_MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String PROPERTY_NAME_MAIL_SMTP_AUTH_MECHANISMS = "mail.smtp.auth.mechanisms";

    /*AUTH MECHANISMS*/
    public static final String AUTH_MECHANISMS_XOAUTH2 = "XOAUTH2";
    public static final String AUTH_MECHANISMS_PLAIN = "PLAIN";
    public static final String AUTH_MECHANISMS_LOGIN = "LOGIN";

    /*PROTOCOLS*/
    public static final String PROTOCOL_IMAP = "imap";
    public static final String PROTOCOL_SMTP = "smtp";
    public static final String PROTOCOL_GIMAPS = "gimaps";
    public static final String OAUTH2 = "oauth2:";

    public static final String MIME_TYPE_MULTIPART = "multipart/*";
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_TEXT_HTML = "text/html";

    public static final String FOLDER_ATTRIBUTE_NO_SELECT = "\\Noselect";

    public static final String HEADER_X_ATTACHMENT_ID = "X-Attachment-Id";
    public static final String HEADER_CONTENT_ID = "Content-ID";
    public static final String FOLDER_INBOX = "INBOX";

    public static final String EMAIL_PROVIDER_GMAIL = "gmail.com";
}
