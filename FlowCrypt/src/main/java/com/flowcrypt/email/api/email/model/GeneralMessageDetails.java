/*
 * Business Source License 1.0 © 2017 FlowCrypt Limited (human@flowcrypt.com).
 * Use limitations apply. See https://github.com/FlowCrypt/flowcrypt-android/blob/master/LICENSE
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.email.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * Simple POJO class which describe a general message details.
 *
 * @author DenBond7
 *         Date: 28.04.2017
 *         Time: 11:51
 *         E-mail: DenBond7@gmail.com
 */

public class GeneralMessageDetails implements Parcelable {

    public static final Creator<GeneralMessageDetails> CREATOR = new
            Creator<GeneralMessageDetails>() {
                @Override
                public GeneralMessageDetails createFromParcel(Parcel source) {
                    return new GeneralMessageDetails(source);
                }

                @Override
                public GeneralMessageDetails[] newArray(int size) {
                    return new GeneralMessageDetails[size];
                }
            };

    private String email;
    private String label;
    private int uid;
    private long receivedDateInMillisecond;
    private long sentDateInMillisecond;
    private String[] from;
    private String[] to;
    private String subject;
    private String[] flags;
    private String rawMessageWithoutAttachments;
    private boolean isMessageHasAttachment;

    public GeneralMessageDetails() {
    }

    protected GeneralMessageDetails(Parcel in) {
        this.email = in.readString();
        this.label = in.readString();
        this.uid = in.readInt();
        this.receivedDateInMillisecond = in.readLong();
        this.sentDateInMillisecond = in.readLong();
        this.from = in.createStringArray();
        this.to = in.createStringArray();
        this.subject = in.readString();
        this.flags = in.createStringArray();
        this.rawMessageWithoutAttachments = in.readString();
        this.isMessageHasAttachment = in.readByte() != 0;
    }

    @Override
    public String toString() {
        return "GeneralMessageDetails{" +
                "email='" + email + '\'' +
                ", label='" + label + '\'' +
                ", uid=" + uid +
                ", receivedDateInMillisecond=" + receivedDateInMillisecond +
                ", sentDateInMillisecond=" + sentDateInMillisecond +
                ", from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                ", subject='" + subject + '\'' +
                ", flags=" + Arrays.toString(flags) +
                ", rawMessageWithoutAttachments =" + TextUtils.isEmpty
                (rawMessageWithoutAttachments) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.label);
        dest.writeInt(this.uid);
        dest.writeLong(this.receivedDateInMillisecond);
        dest.writeLong(this.sentDateInMillisecond);
        dest.writeStringArray(this.from);
        dest.writeStringArray(this.to);
        dest.writeString(this.subject);
        dest.writeStringArray(this.flags);
        dest.writeString(this.rawMessageWithoutAttachments);
        dest.writeByte(this.isMessageHasAttachment ? (byte) 1 : (byte) 0);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getReceivedDateInMillisecond() {
        return receivedDateInMillisecond;
    }

    public void setReceivedDateInMillisecond(long receivedDateInMillisecond) {
        this.receivedDateInMillisecond = receivedDateInMillisecond;
    }

    public long getSentDateInMillisecond() {
        return sentDateInMillisecond;
    }

    public void setSentDateInMillisecond(long sentDateInMillisecond) {
        this.sentDateInMillisecond = sentDateInMillisecond;
    }

    public String[] getFrom() {
        return from;
    }

    public void setFrom(String[] from) {
        this.from = from;
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String[] getFlags() {
        return flags;
    }

    public void setFlags(String[] flags) {
        this.flags = flags;
    }

    public boolean isSeen() {
        return flags != null && Arrays.asList(flags).contains(MessageFlag.SEEN);
    }

    public String getRawMessageWithoutAttachments() {
        return rawMessageWithoutAttachments;
    }

    public void setRawMessageWithoutAttachments(String rawMessageWithoutAttachments) {
        this.rawMessageWithoutAttachments = rawMessageWithoutAttachments;
    }

    public boolean isMessageHasAttachment() {
        return isMessageHasAttachment;
    }

    public void setMessageHasAttachment(boolean messageHasAttachment) {
        isMessageHasAttachment = messageHasAttachment;
    }
}
