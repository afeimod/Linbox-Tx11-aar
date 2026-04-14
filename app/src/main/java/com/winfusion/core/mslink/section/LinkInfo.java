package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the LinkInfo section of a Shell Link file.
 */
public class LinkInfo {

    /**
     * Defines the bitmask flags for LinkInfo properties.
     */
    public enum LinkInfoFlagsMask implements IntMaskable{
        VolumeIDAndLocalBasePath,
        CommonNetworkRelativeLinkAndPathSuffix;

        @Override
        public int toMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Flags for LinkInfo fields.
     */
    private IntFlags<LinkInfoFlagsMask> linkInfoFlags;

    /**
     * Information about the volume containing the linked item.
     */
    private VolumeID volumeID;

    /**
     * The local base path of the linked item.
     */
    private ByteData localBasePath;

    /**
     * Network-related information.
     */
    private CommonNetworkRelativeLink commonNetworkRelativeLink;

    /**
     * The common path suffix for the linked item.
     */
    private ByteData commonPathSuffix;

    /**
     * Unicode version of the local base path.
     */
    private ByteData localBasePathUnicode;

    /**
     * Unicode version of the common path suffix.
     */
    private ByteData commonPathSuffixUnicode;

    public LinkInfo() {
        linkInfoFlags = new IntFlags<>();
        commonNetworkRelativeLink = new CommonNetworkRelativeLink();
        commonPathSuffix = new ByteData();
    }

    @NonNull
    public IntFlags<LinkInfoFlagsMask> getLinkInfoFlags() {
        return linkInfoFlags;
    }

    @Nullable
    public VolumeID getVolumeID() {
        return volumeID;
    }

    @Nullable
    public ByteData getLocalBasePath() {
        return localBasePath;
    }

    @Nullable
    public CommonNetworkRelativeLink getCommonNetworkRelativeLink() {
        return commonNetworkRelativeLink;
    }

    @NonNull
    public ByteData getCommonPathSuffix() {
        return commonPathSuffix;
    }

    @Nullable
    public ByteData getLocalBasePathUnicode() {
        return localBasePathUnicode;
    }

    @Nullable
    public ByteData getCommonPathSuffixUnicode() {
        return commonPathSuffixUnicode;
    }

    public void setLinkInfoFlags(@NonNull IntFlags<LinkInfoFlagsMask> linkInfoFlags) {
        this.linkInfoFlags = linkInfoFlags;
    }

    public void setVolumeID(@Nullable VolumeID volumeID) {
        this.volumeID = volumeID;
    }

    public void setLocalBasePath(@Nullable ByteData localBasePath) {
        this.localBasePath = localBasePath;
    }

    public void setCommonNetworkRelativeLink(@Nullable CommonNetworkRelativeLink commonNetworkRelativeLink) {
        this.commonNetworkRelativeLink = commonNetworkRelativeLink;
    }

    public void setCommonPathSuffix(@NonNull ByteData commonPathSuffix) {
        this.commonPathSuffix = commonPathSuffix;
    }

    public void setLocalBasePathUnicode(@Nullable ByteData localBasePathUnicode) {
        this.localBasePathUnicode = localBasePathUnicode;
    }

    public void setCommonPathSuffixUnicode(@Nullable ByteData commonPathSuffixUnicode) {
        this.commonPathSuffixUnicode = commonPathSuffixUnicode;
    }
}
