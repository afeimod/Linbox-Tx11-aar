package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the Shell Link Header section of a Shell Link file.
 */
public class ShellLinkHeader {

    /**
     * The fixed size of the Shell Link Header in bytes.
     */
    public static final int HeaderSize = 0x0000004C;

    /**
     * The LinkCLSID, which uniquely identifies a Shell Link object.
     */
    public static final long[] LinkCLSID = {0x0002140100000000L, 0xC000000000000046L};


    /**
     * Represents the show command types for the linked application, such as normal, maximized, or minimized.
     */
    public enum ShowCommandType implements Valuable<Integer> {
        ShowNormal(0x00000001),
        ShowMaximized(0x00000003),
        ShowMinNoAcvive(0x00000007);

        public final int value;

        ShowCommandType(int value) {
            this.value = value;
        }

        public Integer toValue() {
            return value;
        }

        @Nullable
        public static ShowCommandType fromValue(int value) {
            for (ShowCommandType type : ShowCommandType.values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }


    /**
     * Represents the flags for the Shell Link, indicating which sections or properties are present.
     */
    public enum LinkFlagsMask implements IntMaskable {
        HasLinkTargetIDList,
        HasLinkInfo,
        HasName,
        HasRelativePath,
        HasWorkingDir,
        HasArguments,
        HasIconLocation,
        IsUnicode,
        ForceNoLinkInfo,
        HasExpString,
        RunInSeparateProcess,
        Unused1,
        HasDarwinID,
        RunAsUser,
        HasExpIcon,
        NoPidlAlias,
        Unused2,
        RunWithShimLayer,
        ForceNoLinkTrack,
        EnableTargetMetadata,
        DisableLinkPathTracking,
        DisableKnownFolderTracking,
        DisableKnownFolderAlias,
        AllowLinkToLink,
        UnaliasOnSave,
        PreferEnvironmentPath,
        KeepLocalIDListForUNCTarget;

        @Override
        public int toMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Represents the file attributes for the linked file or directory.
     */
    public enum FileAttributesMask implements IntMaskable {
        FILE_ATTRIBUTE_READONLY,
        FILE_ATTRIBUTE_HIDDEN,
        FILE_ATTRIBUTE_SYSTEM,
        Reserved1,
        FILE_ATTRIBUTE_DIRECTORY,
        FILE_ATTRIBUTE_ARCHIVE,
        Reserved2,
        FILE_ATTRIBUTE_NORMAL,
        FILE_ATTRIBUTE_TEMPORARY,
        FILE_ATTRIBUTE_SPARSE_FILE,
        FILE_ATTRIBUTE_REPARSE_POINT,
        FILE_ATTRIBUTE_COMPRESSED,
        FILE_ATTRIBUTE_OFFLINE,
        FILE_ATTRIBUTE_NOT_CONTENT_INDEXED,
        FILE_ATTRIBUTE_ENCRYPTED;

        @Override
        public int toMask() {
            return 1 << ordinal();
        }
    }

    /**
     * The flags that indicate which sections or properties are present in the Shell Link.
     */
    private IntFlags<LinkFlagsMask> linkFlags;

    /**
     * The file attributes of the linked file or directory.
     */
    private IntFlags<FileAttributesMask> fileAttributes;

    /**
     * The creation time of the linked file or directory.
     */
    private FileTime creationTime;

    /**
     * The last access time of the linked file or directory.
     */
    private FileTime accessTime;

    /**
     * The last write time of the linked file or directory.
     */
    private FileTime writeTime;

    /**
     * The size of the linked file, in bytes.
     */
    private int fileSize;

    /**
     * The index of the icon for the linked file.
     */
    private int iconIndex;

    /**
     * The show command type for the linked application.
     */
    private ShowCommandType showCommand;

    /**
     * The hotkey associated with the link.
     */
    private short hotKey;

    public ShellLinkHeader() {
        linkFlags = new IntFlags<>();
        fileAttributes = new IntFlags<>();
        creationTime = new FileTime();
        accessTime = new FileTime();
        writeTime = new FileTime();
    }

    @NonNull
    public IntFlags<LinkFlagsMask> getLinkFlags() {
        return linkFlags;
    }

    @NonNull
    public IntFlags<FileAttributesMask> getFileAttributes() {
        return fileAttributes;
    }

    @NonNull
    public FileTime getCreationTime() {
        return creationTime;
    }

    @NonNull
    public FileTime getAccessTime() {
        return accessTime;
    }

    @NonNull
    public FileTime getWriteTime() {
        return writeTime;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    @NonNull
    public ShowCommandType getShowCommand() {
        return showCommand;
    }

    public short getHotKey() {
        return hotKey;
    }

    public void setLinkFlags(@NonNull IntFlags<LinkFlagsMask> linkFlags) {
        this.linkFlags = linkFlags;
    }

    public void setFileAttributes(@NonNull IntFlags<FileAttributesMask> fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    public void setCreationTime(@NonNull FileTime creationTime) {
        this.creationTime = creationTime;
    }

    public void setAccessTime(@NonNull FileTime accessTime) {
        this.accessTime = accessTime;
    }

    public void setWriteTime(@NonNull FileTime writeTime) {
        this.writeTime = writeTime;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }

    public void setShowCommand(@NonNull ShowCommandType showCommand) {
        this.showCommand = showCommand;
    }

    public void setHotKey(short hotKey) {
        this.hotKey = hotKey;
    }
}
