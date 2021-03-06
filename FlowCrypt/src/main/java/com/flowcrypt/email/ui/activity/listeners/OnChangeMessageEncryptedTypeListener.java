/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity.listeners;

import com.flowcrypt.email.model.MessageEncryptionType;

/**
 * This interface can be used when need to notify if {@link MessageEncryptionType} was changed.
 *
 * @author Denis Bondarenko
 *         Date: 28.07.2017
 *         Time: 15:39
 *         E-mail: DenBond7@gmail.com
 */

public interface OnChangeMessageEncryptedTypeListener {

    /**
     * Handle a switch of the message encryption type.
     *
     * @param messageEncryptionType The new message encryption type.
     */
    void onMessageEncryptionTypeChange(MessageEncryptionType messageEncryptionType);

    MessageEncryptionType getMessageEncryptionType();
}

