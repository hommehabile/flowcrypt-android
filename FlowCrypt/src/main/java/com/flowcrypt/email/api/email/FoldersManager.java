package com.flowcrypt.email.api.email;

import android.text.TextUtils;

import com.sun.mail.imap.IMAPFolder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

/**
 * The {@link FoldersManager} describes a logic of work with remote folders. This class helps as
 * resolve problems with localized names of Gmail labels.
 *
 * @author DenBond7
 *         Date: 07.06.2017
 *         Time: 14:37
 *         E-mail: DenBond7@gmail.com
 */

public class FoldersManager {
    private LinkedHashMap<String, Folder> folders;

    public FoldersManager() {
        this.folders = new LinkedHashMap<>();
    }

    public Folder getFolderInbox() {
        return folders.get(FolderType.INBOX.getValue());
    }

    public Folder getFolderArchive() {
        return folders.get(FolderType.All.getValue());
    }

    public Folder getFolderDrafts() {
        return folders.get(FolderType.DRAFTS.getValue());
    }

    public Folder getFolderStarred() {
        return folders.get(FolderType.STARRED.getValue());
    }

    public Folder getFolderSpam() {
        return folders.get(FolderType.SPAM.getValue());
    }

    public Folder getFolderSent() {
        return folders.get(FolderType.SENT.getValue());
    }

    public Folder getFolderTrash() {
        return folders.get(FolderType.TRASH.getValue());
    }

    public Folder getFolderAll() {
        return folders.get(FolderType.All.getValue());
    }

    public Folder getFolderImportant() {
        return folders.get(FolderType.IMPORTANT.getValue());
    }

    /**
     * Add a new folder to {@link FoldersManager} to manage it.
     *
     * @param imapFolder  The {@link IMAPFolder} object which contains an information about a
     *                    remote folder.
     * @param folderAlias The folder alias.
     * @throws MessagingException
     */
    public void addFolder(IMAPFolder imapFolder, String folderAlias) throws MessagingException {
        if (imapFolder != null && !TextUtils.isEmpty(imapFolder.getFullName())
                && !folders.containsKey(imapFolder.getFullName())) {
            this.folders.put(prepareFolderKey(imapFolder), new Folder(folderAlias, imapFolder
                    .getFullName(),
                    isCustomLabels(imapFolder)));
        }
    }

    /**
     * Get {@link Folder} by the alias name.
     *
     * @param folderAlias The folder alias name.
     * @return {@link Folder}.
     */
    public Folder getFolderByAlias(String folderAlias) {
        for (Map.Entry<String, Folder> entry : folders.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getFolderAlias().equals(folderAlias)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public Collection<Folder> getAllFolders() {
        return folders.values();
    }

    /**
     * Get a list of all available custom labels.
     *
     * @return List of custom labels({@link Folder}).
     */
    public List<Folder> getCustomLabels() {
        List<Folder> customFolders = new LinkedList<>();

        for (Map.Entry<String, Folder> entry : folders.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isCustomLabel()) {
                customFolders.add(entry.getValue());
            }
        }

        return customFolders;
    }

    /**
     * Get a list of original server {@link Folder} objects.
     *
     * @return a list of original server {@link Folder} objects.
     */
    public List<Folder> getServerFolders() {
        List<Folder> serverFolders = new LinkedList<>();

        for (Map.Entry<String, Folder> entry : folders.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isCustomLabel()) {
                serverFolders.add(entry.getValue());
            }
        }

        return serverFolders;
    }

    private String prepareFolderKey(IMAPFolder imapFolder) throws MessagingException {
        FolderType folderType = getFolderTypeForImapFodler(imapFolder);
        if (folderType == null) {
            return imapFolder.getFullName();
        } else {
            return folderType.value;
        }

    }

    private boolean isCustomLabels(IMAPFolder folder) throws MessagingException {
        String[] attr = folder.getAttributes();
        FolderType[] folderTypes = FolderType.values();

        for (String attribute : attr) {
            for (FolderType folderType : folderTypes) {
                if (folderType.getValue().equals(attribute)) {
                    return false;
                }
            }
        }

        return !FolderType.INBOX.getValue().equals(folder.getFullName());

    }

    private FolderType getFolderTypeForImapFodler(IMAPFolder folder) throws MessagingException {
        String[] attr = folder.getAttributes();
        FolderType[] folderTypes = FolderType.values();

        for (String attribute : attr) {
            for (FolderType folderType : folderTypes) {
                if (folderType.getValue().equals(attribute)) {
                    return folderType;
                }
            }
        }

        return null;
    }

    /**
     * This class contains an information about all servers folders types.
     */
    public enum FolderType {
        INBOX("INBOX"),
        All("\\All"),
        ARCHIVE("\\Archive"),
        DRAFTS("\\Drafts"),
        STARRED("\\Flagged"),
        SPAM("\\Junk"),
        SENT("\\Sent"),
        TRASH("\\Trash"),
        IMPORTANT("\\Important");


        private String value;

        FolderType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}