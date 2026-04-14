package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the CommonNetworkRelativeLink section of a LinkInfo section.
 */
public class CommonNetworkRelativeLink {

    /**
     * Enum representing the network provider types (WNNCNetType).
     */
    public enum WNNCNetType implements Valuable<Integer> {
        AVID(0x001A0000),
        DOCUSPACE(0x001B0000),
        MANGOSOFT(0x001C0000),
        SERNET(0x001D0000),
        RIVERFRONT1(0x001E0000),
        RIVERFRONT2(0x001F0000),
        DECORB(0x00200000),
        PROTSTOR(0x00210000),
        FJ_REDIR(0x00220000),
        DISTINCT(0x00230000),
        TWINS(0x00240000),
        RDR2SAMPLE(0x00250000),
        CSC(0x00260000),
        THREE_IN_ONE(0x00270000),
        EXTENDNET(0x00290000),
        STAC(0x002A0000),
        FOXBAT(0x002B0000),
        YAHOO(0x002C0000),
        EXIFS(0x002D0000),
        DAV(0x002E0000),
        KNOWARE(0x002F0000),
        OBJECT_DIRE(0x00300000),
        MASFAX(0x00310000),
        HOB_NFS(0x00320000),
        SHIVA(0x00330000),
        IBMAL(0x00340000),
        LOCK(0x00350000),
        TERMSRV(0x00360000),
        SRT(0x00370000),
        QUINCY(0x00380000),
        OPENAFS(0x00390000),
        AVID1(0x003A0000),
        DFS(0x003B0000),
        KWNP(0x003C0000),
        ZENWORKS(0x003D0000),
        DRIVEONWEB(0x003E0000),
        VMWARE(0x003F0000),
        RSFX(0x00400000),
        MFILES(0x00410000),
        MS_NFS(0x00420000),
        GOOGLE(0x00430000);

        private final int value;

        WNNCNetType(int value) {
            this.value = value;
        }

        public Integer toValue() {
            return value;
        }

        @Nullable
        public static WNNCNetType fromValue(int value) {
            for (WNNCNetType net : values()) {
                if (net.value == value)
                    return net;
            }
            return null;
        }
    }

    /**
     * Enum representing the flags used in CommonNetworkRelativeLink.
     */
    public enum CommonNetworkRelativeLinkFlagsMask implements IntMaskable {
        ValidDevice,
        ValidNetType;

        @Override
        public int toMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Flags of the CommonNetworkRelativeLink
     */
    private IntFlags<CommonNetworkRelativeLinkFlagsMask> commonNetworkRelativeLinkFlags;

    /**
     * Type of the Network Provider
     */
    private int networkProviderType;

    /**
     * Name of the Network
     */
    private ByteData netName;

    /**
     * Name of the Network Device
     */
    private ByteData deviceName;

    /**
     * Name of the Network defined by Unicode Characters
     */
    private ByteData netNameUnicode;

    /*
     * Name of the Network Device defined by Unicode characters
     */
    private ByteData deviceNameUnicode;

    public CommonNetworkRelativeLink() {
        commonNetworkRelativeLinkFlags = new IntFlags<>();
        netName = new ByteData();
        deviceName = new ByteData();
    }

    @NonNull
    public IntFlags<CommonNetworkRelativeLinkFlagsMask> getCommonNetworkRelativeLinkFlags() {
        return commonNetworkRelativeLinkFlags;
    }

    public int getNetworkProviderType() {
        return networkProviderType;
    }

    @NonNull
    public ByteData getNetName() {
        return netName;
    }

    @Nullable
    public ByteData getDeviceName() {
        return deviceName;
    }

    @Nullable
    public ByteData getNetNameUnicode() {
        return netNameUnicode;
    }

    @Nullable
    public ByteData getDeviceNameUnicode() {
        return deviceNameUnicode;
    }

    public void setCommonNetworkRelativeLinkFlags(@NonNull IntFlags<CommonNetworkRelativeLinkFlagsMask> commonNetworkRelativeLinkFlags) {
        this.commonNetworkRelativeLinkFlags = commonNetworkRelativeLinkFlags;
    }

    public void setNetworkProviderType(int networkProviderType) {
        this.networkProviderType = networkProviderType;
    }

    public void setNetName(@NonNull ByteData netName) {
        this.netName = netName;
    }

    public void setDeviceName(@Nullable ByteData deviceName) {
        this.deviceName = deviceName;
    }

    public void setNetNameUnicode(@Nullable ByteData netNameUnicode) {
        this.netNameUnicode = netNameUnicode;
    }

    public void setDeviceNameUnicode(@Nullable ByteData deviceNameUnicode) {
        this.deviceNameUnicode = deviceNameUnicode;
    }
}
