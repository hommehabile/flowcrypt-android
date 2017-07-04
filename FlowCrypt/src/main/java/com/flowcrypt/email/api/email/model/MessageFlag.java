/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (tom@cryptup.org).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email.model;

/**
 * The message flags. This flags will be used in the local database.
 *
 * @author DenBond7
 *         Date: 20.06.2017
 *         Time: 17:47
 *         E-mail: DenBond7@gmail.com
 */

public class MessageFlag {
    public static final String ANSWERED = "\\ANSWERED";
    public static final String DELETED = "\\DELETED";
    public static final String DRAFT = "\\DRAFT";
    public static final String FLAGGED = "\\FLAGGED";
    public static final String RECENT = "\\RECENT";
    public static final String SEEN = "\\SEEN";
}
