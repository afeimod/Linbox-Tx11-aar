package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the volume ID information used in a Shell Link file.
 */
public class VolumeID {

    /**
     * Enum representing the drive type for the volume.
     */
    public enum DriveType implements Valuable<Integer> {
        Unknown(0x00000000),
        NoRootDir(0x00000001),
        Removable(0x00000002),
        Fixed(0x00000003),
        Remote(0x00000004),
        CDRom(0x00000005),
        RamDisk(0x00000006);

        public final int value;

        DriveType(int value) {
            this.value = value;
        }

        public Integer toValue() {
            return value;
        }

        @Nullable
        public static DriveType fromValue(int value) {
            for (DriveType type : DriveType.values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }

    /**
     * The type of the drive
     */
    private DriveType driveType;

    /**
     * The serial number of the drive
     */
    private int driveSerialNumber;

    /**
     * Additional data associated with the volume
     */
    private ByteData data;

    public VolumeID() {
        this.data = new ByteData();
    }

    @NonNull
    public DriveType getDriveType() {
        return driveType;
    }

    public int getDriveSerialNumber() {
        return driveSerialNumber;
    }

    @NonNull
    public ByteData getData() {
        return data;
    }

    public void setDriveType(@NonNull DriveType driveType) {
        this.driveType = driveType;
    }

    public void setDriveSerialNumber(int driveSerialNumber) {
        this.driveSerialNumber = driveSerialNumber;
    }

    public void setData(@NonNull ByteData data) {
        this.data = data;
    }
}
