package com.winfusion.core.mslink;

import static com.winfusion.core.mslink.section.CommonNetworkRelativeLink.CommonNetworkRelativeLinkFlagsMask.ValidDevice;
import static com.winfusion.core.mslink.section.CommonNetworkRelativeLink.CommonNetworkRelativeLinkFlagsMask.ValidNetType;
import static com.winfusion.core.mslink.section.LinkInfo.LinkInfoFlagsMask.CommonNetworkRelativeLinkAndPathSuffix;
import static com.winfusion.core.mslink.section.LinkInfo.LinkInfoFlagsMask.VolumeIDAndLocalBasePath;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasArguments;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasIconLocation;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasLinkInfo;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasLinkTargetIDList;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasName;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasRelativePath;
import static com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask.HasWorkingDir;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.mslink.exception.BadLnkFormatException;
import com.winfusion.core.mslink.section.ByteData;
import com.winfusion.core.mslink.section.CommonNetworkRelativeLink;
import com.winfusion.core.mslink.section.CommonNetworkRelativeLink.CommonNetworkRelativeLinkFlagsMask;
import com.winfusion.core.mslink.section.CommonNetworkRelativeLink.WNNCNetType;
import com.winfusion.core.mslink.section.ExtraData;
import com.winfusion.core.mslink.section.FileTime;
import com.winfusion.core.mslink.section.IntFlags;
import com.winfusion.core.mslink.section.ItemID;
import com.winfusion.core.mslink.section.LinkInfo;
import com.winfusion.core.mslink.section.LinkInfo.LinkInfoFlagsMask;
import com.winfusion.core.mslink.section.LinkTargetIDList;
import com.winfusion.core.mslink.section.ShellLinkHeader;
import com.winfusion.core.mslink.section.ShellLinkHeader.LinkFlagsMask;
import com.winfusion.core.mslink.section.ShellLinkHeader.FileAttributesMask;
import com.winfusion.core.mslink.section.ShellLinkHeader.ShowCommandType;
import com.winfusion.core.mslink.section.StringData;
import com.winfusion.core.mslink.section.VolumeID;
import com.winfusion.core.mslink.section.VolumeID.DriveType;
import com.winfusion.core.mslink.section.block.RawBlock;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * ShellLinkParser class is responsible for parsing the {@link ShellLink} (Windows Shortcut) file format.
 * This class includes multiple methods to read and parse different parts of a ShellLink file.
 */
public final class ShellLinkParser {

    private ShellLinkParser() {

    }

    /**
     * Parses the {@link ShellLink} file from the specified file path.
     *
     * @param path           The file path of the ShellLink file.
     * @param defaultCharset The charset to parse string.
     * @return The parsed {@link ShellLink} object, or throws an exception if decoding fails.
     * @throws BadLnkFormatException If the byte array format is invalid or parsing fails.
     */
    @NonNull
    public static ShellLink parse(@NonNull final String path, @Nullable Charset defaultCharset)
            throws IOException, BadLnkFormatException {

        return parse(new File(path), defaultCharset);
    }

    /**
     * Parses the {@link ShellLink} file from the specified file object.
     *
     * @param file           The ShellLink file object.
     * @param defaultCharset The charset to parse string.
     * @return The parsed {@link ShellLink} object, or throws an exception if decoding fails.
     * @throws BadLnkFormatException If the byte array format is invalid or parsing fails.
     */
    @NonNull
    public static ShellLink parse(@NonNull final File file, @Nullable Charset defaultCharset)
            throws IOException, BadLnkFormatException {

        return parse(Files.readAllBytes(Paths.get(file.getAbsolutePath())), defaultCharset);
    }

    /**
     * Parses the {@link ShellLink} data from the specified byte array.
     *
     * @param bytes          The byte array containing ShellLink file data.
     * @param defaultCharset The charset to parse string.
     * @return The parsed {@link ShellLink} object, or throws an exception if decoding fails.
     * @throws BadLnkFormatException If the byte array format is invalid or parsing fails.
     */
    @NonNull
    public static ShellLink parse(@NonNull final byte[] bytes, @Nullable Charset defaultCharset)
            throws BadLnkFormatException {

        try {
            return parse(ByteBuffer.wrap(bytes), defaultCharset);
        } catch (BufferUnderflowException e) {
            throw new BadLnkFormatException("Unexpected EOF.", e);
        }
    }

    /**
     * Parses the {@link ShellLink} data from the provided {@link ByteBuffer}.
     *
     * @param buffer         The {@link ByteBuffer} containing ShellLink data.
     * @param defaultCharset The charset to parse string.
     * @return The parsed {@link ShellLink} object, or null if decoding fails.
     * @throws NullPointerException If {@code buffer} is null
     */
    @NonNull
    private static ShellLink parse(@NonNull final ByteBuffer buffer, @Nullable Charset defaultCharset)
            throws BadLnkFormatException {

        ShellLinkHeader shellLinkHeader;
        LinkTargetIDList linkTargetIDList = null;
        LinkInfo linkInfo = null;
        StringData stringData;
        ExtraData extraData;
        ShellLink shellLink;
        Position position = new Position();
        boolean hasLinkTargetIDList, hasLinkInfo;

        if (defaultCharset == null)
            defaultCharset = StandardCharsets.US_ASCII;

        position.shellLinkStartPos = buffer.position();
        position.shellLinkHeaderStartPos = position.shellLinkStartPos;

        // try to parse ShellLinkHeader
        shellLinkHeader = parseShellLinkHeader(buffer, position);
        if (shellLinkHeader == null)
            throw new BadLnkFormatException("ShellLink Header is broken.");

        hasLinkTargetIDList = shellLinkHeader.getLinkFlags().hasFlag(HasLinkTargetIDList);
        hasLinkInfo = shellLinkHeader.getLinkFlags().hasFlag(HasLinkInfo);

        // if HasLinkTargetIDList is set in LinkFlags, then the LinkTargetIDList is present.
        if (hasLinkTargetIDList) {
            position.linkTargetIDListStartPos = position.shellLinkStartPos +
                    position.shellLinkHeaderSize;
            linkTargetIDList = parseLinkTargetIDList(buffer, position, defaultCharset);
            if (linkTargetIDList == null)
                throw new BadLnkFormatException("LinkTargetIDList is broken.");
        } else {
            position.linkTargetIDListStartPos = position.shellLinkStartPos;
            position.linkTargetIDListSize = position.shellLinkHeaderSize;
        }

        // if HasLinkInfo is set in LinkFlags, then the LinkInfo is present
        if (hasLinkInfo) {
            position.linkInfoStartPos = position.linkTargetIDListStartPos +
                    position.linkTargetIDListSize;
            linkInfo = parseLinkInfo(buffer, position, defaultCharset);
            if (linkInfo == null)
                throw new BadLnkFormatException("LinkInfo is broken.");
        } else {
            position.linkInfoStartPos = position.linkTargetIDListStartPos;
            position.linkInfoSize = position.linkTargetIDListSize;
        }

        // try to parse StringData
        position.stringDataStartPos = position.linkInfoStartPos + position.linkInfoSize;
        stringData = parseStringData(
                buffer,
                position,
                shellLinkHeader.getLinkFlags().hasFlag(HasName),
                shellLinkHeader.getLinkFlags().hasFlag(HasRelativePath),
                shellLinkHeader.getLinkFlags().hasFlag(HasWorkingDir),
                shellLinkHeader.getLinkFlags().hasFlag(HasArguments),
                shellLinkHeader.getLinkFlags().hasFlag(HasIconLocation)
        );

        // try to parse ExtraData
        position.extraDataStartPos = position.stringDataStartPos + position.stringDataSize;
        extraData = parseExtraData(buffer, position);
        if (extraData == null)
            throw new BadLnkFormatException("ExtraData is broken");

        shellLink = new ShellLink();
        shellLink.setHeader(shellLinkHeader);
        shellLink.setLinkTargetIDList(linkTargetIDList);
        shellLink.setLinkInfo(linkInfo);
        shellLink.setStringData(stringData);
        shellLink.setExtraData(extraData);

        return shellLink;
    }

    /**
     * Parses the {@link ShellLinkHeader} from the given {@link ByteBuffer}.
     *
     * @param buffer   The {@link ByteBuffer} containing ShellLink data.
     * @param position The current {@link Position} of the buffer and the parsed data positions.
     * @return The parsed {@link ShellLinkHeader} object, or null if the header is invalid.
     */
    @Nullable
    private static ShellLinkHeader parseShellLinkHeader(@NonNull final ByteBuffer buffer,
                                                        @NonNull final Position position) {

        int headerSize, fileSize, iconIndex, showCommandValue, reserved2, reserved3;
        short hotKey, reserved1;
        long[] linkCLSID;
        IntFlags<LinkFlagsMask> linkFlags;
        IntFlags<FileAttributesMask> fileAttributes;
        FileTime creationTime, accessTime, writeTime;
        ShowCommandType showCommand;
        ShellLinkHeader shellLinkHeader;

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(position.shellLinkHeaderStartPos);

        headerSize = buffer.getInt();
        position.shellLinkHeaderSize = headerSize;

        // read GUID: aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee (int-short-short-char[8])
        linkCLSID = new long[2];
        linkCLSID[0] = (long) buffer.getInt() << 32;
        linkCLSID[0] += buffer.getShort() << 16;
        linkCLSID[0] += buffer.getShort();
        buffer.order(ByteOrder.BIG_ENDIAN);
        linkCLSID[1] = buffer.getLong();
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        linkFlags = new IntFlags<>(buffer.getInt());
        fileAttributes = new IntFlags<>(buffer.getInt());
        creationTime = new FileTime(buffer.getLong());
        accessTime = new FileTime(buffer.getLong());
        writeTime = new FileTime(buffer.getLong());
        fileSize = buffer.getInt();
        iconIndex = buffer.getInt();
        showCommandValue = buffer.getInt();
        showCommand = ShowCommandType.fromValue(showCommandValue);
        hotKey = buffer.getShort();

        reserved1 = buffer.getShort();
        reserved2 = buffer.getInt();
        reserved3 = buffer.getInt();

        // ShowCommand must be non-null
        if (showCommand == null)
            return null;

        // all reserved values must be 0
        if (reserved1 != 0 || reserved2 != 0 || reserved3 != 0)
            return null;

        // HeaderSize and LinkCLSID are fixed in lnk files,
        // so they can be used to check if the files is in ShellLink format
        if (headerSize != ShellLinkHeader.HeaderSize ||
                linkCLSID[0] != ShellLinkHeader.LinkCLSID[0] ||
                linkCLSID[1] != ShellLinkHeader.LinkCLSID[1]) {
            return null;
        }

        shellLinkHeader = new ShellLinkHeader();
        shellLinkHeader.setLinkFlags(linkFlags);
        shellLinkHeader.setFileAttributes(fileAttributes);
        shellLinkHeader.setCreationTime(creationTime);
        shellLinkHeader.setAccessTime(accessTime);
        shellLinkHeader.setWriteTime(writeTime);
        shellLinkHeader.setFileSize(fileSize);
        shellLinkHeader.setIconIndex(iconIndex);
        shellLinkHeader.setShowCommand(showCommand);
        shellLinkHeader.setHotKey(hotKey);

        return shellLinkHeader;
    }

    /**
     * Parses the {@link LinkTargetIDList} section of the {@link ShellLink} file.
     *
     * @param buffer   The {@link ByteBuffer} containing ShellLink data.
     * @param position The current {@link Position} of the buffer and the parsed data positions.
     * @return The parsed {@link LinkTargetIDList} object, or null if parsing fails.
     */
    @Nullable
    private static LinkTargetIDList parseLinkTargetIDList(@NonNull final ByteBuffer buffer,
                                                          @NonNull final Position position,
                                                          @NonNull final Charset defaultCharset) {

        int startPos, readSize;
        short idListSize, terminalID;
        LinkTargetIDList linkTargetIDList;
        ArrayList<ItemID> itemIDs;

        buffer.position(position.linkTargetIDListStartPos);
        idListSize = buffer.getShort();
        position.linkTargetIDListSize = idListSize + 2;

        // IDListSize must be greater than 4 (IDListSize[2 bytes] it self and terminalID[2 bytes])
        if (idListSize <= 4)
            return null;

        itemIDs = new ArrayList<>();
        readSize = 0;
        do {
            ItemID itemID;
            startPos = buffer.position();
            short itemIDSize = buffer.getShort();
            byte[] data = new byte[itemIDSize - 2];

            buffer.get(data, 0, data.length);

            itemID = new ItemID();
            itemID.setData(new ByteData(defaultCharset, data));
            itemIDs.add(itemID);

            readSize += buffer.position() - startPos;
        } while (readSize < idListSize - 2);

        // terminalID must be 0
        terminalID = buffer.getShort();
        if (terminalID != 0)
            return null;

        linkTargetIDList = new LinkTargetIDList();
        linkTargetIDList.addItem(itemIDs);

        return linkTargetIDList;
    }

    /**
     * Parses the {@link LinkInfo} section of the {@link ShellLink} file.
     *
     * @param buffer   The {@link ByteBuffer} containing ShellLink data.
     * @param position The current {@link Position} of the buffer and the parsed data positions.
     * @return The parsed {@link LinkInfo} object, or null if parsing fails.
     */
    @Nullable
    private static LinkInfo parseLinkInfo(@NonNull final ByteBuffer buffer,
                                          @NonNull final Position position,
                                          @NonNull final Charset defaultCharset) {

        int linkInfoSize, linkInfoHeaderSize, volumeIDOffset, localBasePathOffset,
                commonNetworkRelativeLinkOffset, commonPathSuffixOffset, localBasePathOffsetUnicode = 0,
                commonPathSuffixOffsetUnicode = 0;
        boolean supportUnicode, hasVolumeIDAndLocalBasePath, hasCommonNetworkRelativeLinkAndPathSuffix;
        IntFlags<LinkInfoFlagsMask> linkInfoFlags;
        VolumeID volumeID = null;
        ByteData localBasePath = null, commonPathSuffix, localBasePathUnicode = null,
                commonPathSuffixUnicode = null;
        CommonNetworkRelativeLink commonNetworkRelativeLink = null;
        LinkInfo linkInfo;

        buffer.position(position.linkInfoStartPos);
        linkInfoSize = buffer.getInt();
        linkInfoHeaderSize = buffer.getInt();
        linkInfoFlags = new IntFlags<>(buffer.getInt());
        volumeIDOffset = buffer.getInt();
        localBasePathOffset = buffer.getInt();
        commonNetworkRelativeLinkOffset = buffer.getInt();
        commonPathSuffixOffset = buffer.getInt();
        supportUnicode = (linkInfoHeaderSize >= 0x00000024);

        hasVolumeIDAndLocalBasePath = linkInfoFlags.hasFlag(VolumeIDAndLocalBasePath);
        hasCommonNetworkRelativeLinkAndPathSuffix =
                linkInfoFlags.hasFlag(CommonNetworkRelativeLinkAndPathSuffix);
        position.linkInfoSize = linkInfoSize;

        // if HasVolumeIDAndLocalBasePath is not set, VolumeIDOffset and LocalBasePathOffset must be 0
        if (!hasVolumeIDAndLocalBasePath && (volumeIDOffset != 0 || localBasePathOffset != 0))
            return null;

        // if HasCommonNetworkRelativeLinkAndPathSuffix is not set, CommonNetworkRelativeLinkOffset must be 0
        if (!hasCommonNetworkRelativeLinkAndPathSuffix && commonNetworkRelativeLinkOffset != 0)
            return null;

        // all offsets in this section must be less than LinkInfoSize
        if (
                volumeIDOffset >= linkInfoSize ||
                        localBasePathOffset >= linkInfoSize ||
                        commonNetworkRelativeLinkOffset >= linkInfoSize ||
                        commonPathSuffixOffset >= linkInfoSize
        ) {
            return null;
        }

        // LocalBasePathOffsetUnicode and CommonPathSuffixOffsetUnicode are present
        // only when LinkInfoHeaderSize is greater than 0x00000024
        if (supportUnicode) {
            localBasePathOffsetUnicode = buffer.getInt();
            commonPathSuffixOffsetUnicode = buffer.getInt();

            if (!hasVolumeIDAndLocalBasePath && localBasePathOffsetUnicode != 0)
                return null;

            if (localBasePathOffsetUnicode > linkInfoSize ||
                    commonPathSuffixOffsetUnicode > linkInfoSize) {
                return null;
            }
        }

        // LocalBasePath is present only when HasVolumeIDAndLocalBasePath is set
        if (hasVolumeIDAndLocalBasePath) {
            buffer.position(position.linkInfoStartPos + volumeIDOffset);
            volumeID = parseVolumeID(buffer, defaultCharset);
            if (volumeID == null)
                return null;

            buffer.position(position.linkInfoStartPos + localBasePathOffset);
            localBasePath = new ByteData(defaultCharset, parseStringToNullForASCII(buffer));
        }

        // CommonNetworkRelativeLink is present only when HasCommonNetworkRelativeLinkAndPathSuffix is set
        if (hasCommonNetworkRelativeLinkAndPathSuffix) {
            buffer.position(position.linkInfoStartPos + commonNetworkRelativeLinkOffset);
            commonNetworkRelativeLink = parseCommonNetworkRelativeLink(buffer, defaultCharset);
            if (commonNetworkRelativeLink == null)
                return null;
        }

        buffer.position(position.linkInfoStartPos + commonPathSuffixOffset);
        commonPathSuffix = new ByteData(defaultCharset, parseStringToNullForASCII(buffer));

        if (supportUnicode) {
            if (hasVolumeIDAndLocalBasePath) {
                buffer.position(position.linkInfoStartPos + localBasePathOffsetUnicode);
                localBasePathUnicode = new ByteData(StandardCharsets.UTF_16LE,
                        parseStringToNullForUnicode(buffer));
            }

            buffer.position(position.linkInfoStartPos + commonPathSuffixOffsetUnicode);
            commonPathSuffixUnicode = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringToNullForUnicode(buffer));
        }

        linkInfo = new LinkInfo();
        linkInfo.setLinkInfoFlags(linkInfoFlags);
        linkInfo.setVolumeID(volumeID);
        linkInfo.setCommonNetworkRelativeLink(commonNetworkRelativeLink);
        linkInfo.setLocalBasePath(localBasePath);
        linkInfo.setLocalBasePathUnicode(localBasePathUnicode);
        linkInfo.setCommonPathSuffix(commonPathSuffix);
        linkInfo.setCommonPathSuffixUnicode(commonPathSuffixUnicode);

        return linkInfo;
    }

    /**
     * Parses the {@link VolumeID} section of the {@link LinkInfo} part.
     *
     * @param buffer The {@link ByteBuffer} containing ShellLink data.
     * @return The parsed {@link VolumeID} object, or null if parsing fails.
     */
    @Nullable
    private static VolumeID parseVolumeID(@NonNull final ByteBuffer buffer,
                                          @NonNull final Charset defaultCharset) {

        int volumeStartPos, volumeIDSize, driveTypeValue, driveSerialNumber, volumeLabelOffset,
                volumeLabelOffsetUnicode;
        boolean useUnicode;
        DriveType driveType;
        ByteData data;
        VolumeID volumeID;

        volumeStartPos = buffer.position();
        volumeIDSize = buffer.getInt();
        driveTypeValue = buffer.getInt();
        driveType = DriveType.fromValue(driveTypeValue);
        driveSerialNumber = buffer.getInt();
        volumeLabelOffset = buffer.getInt();
        useUnicode = volumeLabelOffset == 0x00000014;

        // DriveType must be non-null
        if (driveType == null) {
            return null;
        }

        // if VolumeLabelOffset equals 0x00000014, data is defined by Unicode characters.
        // Otherwise, data is defined by ASCII characters.
        if (useUnicode) {
            volumeLabelOffsetUnicode = buffer.getInt();
            buffer.position(volumeStartPos + volumeLabelOffsetUnicode);
            data = new ByteData(StandardCharsets.UTF_16LE, parseStringToNullForUnicode(buffer));
        } else {
            buffer.position(volumeStartPos + volumeLabelOffset);
            data = new ByteData(defaultCharset, parseStringToNullForASCII(buffer));
        }

        volumeID = new VolumeID();
        volumeID.setDriveType(driveType);
        volumeID.setDriveSerialNumber(driveSerialNumber);
        volumeID.setData(data);

        return volumeID;
    }

    /**
     * Parses the {@link CommonNetworkRelativeLink} section of the {@link LinkInfo} part.
     *
     * @param buffer The {@link ByteBuffer} containing ShellLink data.
     * @return The parsed {@link CommonNetworkRelativeLink} object, or null if parsing fails.
     */
    @Nullable
    private static CommonNetworkRelativeLink parseCommonNetworkRelativeLink(
            @NonNull final ByteBuffer buffer, @NonNull final Charset defaultCharset) {

        int commonNetworkRelativeLinkStartPos, commonNetworkRelativeLinkSize, netNameOffset,
                deviceNameOffset, networkProviderTypeValue, netNameOffsetUnicode,
                deviceNameOffsetUnicode;
        IntFlags<CommonNetworkRelativeLinkFlagsMask> commonNetworkRelativeLinkFlags;
        ByteData netName, deviceName = null, netNameUnicode = null, deviceNameUnicode = null;
        boolean supportUnicode, hasValidDevice, hasValidNetType;
        WNNCNetType networkProviderType;
        CommonNetworkRelativeLink commonNetworkRelativeLink;

        commonNetworkRelativeLinkStartPos = buffer.position();
        commonNetworkRelativeLinkSize = buffer.getInt();
        commonNetworkRelativeLinkFlags = new IntFlags<>(buffer.getInt());
        netNameOffset = buffer.getInt();
        deviceNameOffset = buffer.getInt();
        networkProviderTypeValue = buffer.getInt();
        networkProviderType = WNNCNetType.fromValue(networkProviderTypeValue);
        supportUnicode = netNameOffset > 0x00000014;
        hasValidDevice = commonNetworkRelativeLinkFlags.hasFlag(ValidDevice);
        hasValidNetType = commonNetworkRelativeLinkFlags.hasFlag(ValidNetType);

        // NetworkProviderType must not be 0
        if (networkProviderType == null)
            return null;

        // if HasValidDevice is not set, DeviceNameOffset must be 0
        if (!hasValidDevice && deviceNameOffset != 0)
            return null;

        // if HasValidNetType is not set, NetworkProviderTypeValue must be 0
        if (!hasValidNetType && networkProviderTypeValue != 0)
            return null;

        // NetNameOffset and DeviceNameOffset must be less than CommonNetworkRelativeLinkSize
        if (netNameOffset >= commonNetworkRelativeLinkSize ||
                deviceNameOffset >= commonNetworkRelativeLinkSize) {
            return null;
        }

        buffer.position(commonNetworkRelativeLinkStartPos + netNameOffset);
        netName = new ByteData(defaultCharset, parseStringToNullForASCII(buffer));

        // DeviceName is present only when HasValidDevice is set
        if (hasValidDevice) {
            buffer.position(commonNetworkRelativeLinkStartPos + deviceNameOffset);
            deviceName = new ByteData(defaultCharset, parseStringToNullForASCII(buffer));
        }

        // NetNameOffsetUnicode and DeviceNameOffsetUnicode are present
        // only when NetNameOffset is greater than 0x00000014
        if (supportUnicode) {
            netNameOffsetUnicode = buffer.getInt();
            deviceNameOffsetUnicode = buffer.getInt();

            if (netNameOffsetUnicode >= commonNetworkRelativeLinkSize ||
                    deviceNameOffsetUnicode >= commonNetworkRelativeLinkSize) {
                return null;
            }

            buffer.position(commonNetworkRelativeLinkStartPos + netNameOffsetUnicode);
            netNameUnicode = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringToNullForUnicode(buffer));

            buffer.position(commonNetworkRelativeLinkStartPos + deviceNameOffsetUnicode);
            deviceNameUnicode = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringToNullForUnicode(buffer));
        }

        commonNetworkRelativeLink = new CommonNetworkRelativeLink();
        commonNetworkRelativeLink.setNetworkProviderType(networkProviderTypeValue);
        commonNetworkRelativeLink.setCommonNetworkRelativeLinkFlags(commonNetworkRelativeLinkFlags);
        commonNetworkRelativeLink.setNetName(netName);
        commonNetworkRelativeLink.setDeviceName(deviceName);
        commonNetworkRelativeLink.setNetNameUnicode(netNameUnicode);
        commonNetworkRelativeLink.setDeviceNameUnicode(deviceNameUnicode);

        return commonNetworkRelativeLink;
    }

    /**
     * Parses the {@link StringData} section of the {@link ShellLink} file.
     *
     * @param buffer          The {@link ByteBuffer} containing ShellLink data.
     * @param position        The current {@link Position} of the buffer and the parsed data positions.
     * @param hasName         Whether the name is present.
     * @param hasRelativePath Whether the relative path is present.
     * @param hasWorkingDir   Whether the working directory is present.
     * @param hasArguments    Whether the command line arguments are present.
     * @param hasIconLocation Whether the icon location is present.
     * @return The parsed {@link StringData} object, or null if parsing fails.
     */
    @NonNull
    private static StringData parseStringData(@NonNull final ByteBuffer buffer,
                                              @NonNull final Position position,
                                              final boolean hasName, final boolean hasRelativePath,
                                              final boolean hasWorkingDir, final boolean hasArguments,
                                              final boolean hasIconLocation) {

        StringData stringData;
        int nameSize, relativePathSize, workingDirSize, argumentsSize, iconLocationSize;
        ByteData nameString = null, relativePath = null, workingDir = null, commandLineArguments = null,
                iconLocation = null;

        buffer.position(position.stringDataStartPos);
        position.stringDataSize = 0;

        if (hasName) {
            nameSize = buffer.getShort() * 2;
            nameString = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringBySize(buffer, nameSize));
            position.stringDataSize += (nameSize + 2);
        }

        if (hasRelativePath) {
            relativePathSize = buffer.getShort() * 2;
            relativePath = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringBySize(buffer, relativePathSize));
            position.stringDataSize += (relativePathSize + 2);
        }

        if (hasWorkingDir) {
            workingDirSize = buffer.getShort() * 2;
            workingDir = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringBySize(buffer, workingDirSize));
            position.stringDataSize += (workingDirSize + 2);
        }

        if (hasArguments) {
            argumentsSize = buffer.getShort() * 2;
            commandLineArguments = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringBySize(buffer, argumentsSize));
            position.stringDataSize += (argumentsSize + 2);
        }

        if (hasIconLocation) {
            iconLocationSize = buffer.getShort() * 2;
            iconLocation = new ByteData(StandardCharsets.UTF_16LE,
                    parseStringBySize(buffer, iconLocationSize));
            position.stringDataSize += (iconLocationSize + 2);
        }

        stringData = new StringData();
        stringData.setNameString(nameString);
        stringData.setRelativePath(relativePath);
        stringData.setWorkingDir(workingDir);
        stringData.setCommandLineArguments(commandLineArguments);
        stringData.setIconLocation(iconLocation);

        return stringData;
    }

    /**
     * Parses the {@link ExtraData} section of the {@link ShellLink} file.
     *
     * @param buffer   The {@link ByteBuffer} containing ShellLink data.
     * @param position The current {@link Position} of the buffer and the parsed data positions.
     * @return The parsed {@link ExtraData} object, or null if parsing fails.
     */
    @Nullable
    private static ExtraData parseExtraData(@NonNull final ByteBuffer buffer,
                                            @NonNull final Position position) {

        int rawBlockSize;
        byte[] bytes;
        ExtraData extraData;
        RawBlock rawBlock;
        int terminalBlock;

        // TODO: Get standard blocks instead of raw data.

        position.extraDataSize = 0;
        buffer.position(buffer.limit() - 4);

        terminalBlock = buffer.getInt();
        if (terminalBlock >= 0x00000004)
            return null;

        buffer.position(position.extraDataStartPos);
        rawBlockSize = buffer.limit() - 4 - position.extraDataStartPos;
        bytes = new byte[rawBlockSize];
        buffer.get(bytes, 0, rawBlockSize);

        rawBlock = new RawBlock(bytes);
        extraData = new ExtraData();
        extraData.addItem(rawBlock);

        return extraData;
    }

    /**
     * Parses a null-terminated ASCII string from the given {@link ByteBuffer}.
     *
     * @param buffer The {@link ByteBuffer} from which the ASCII string is to be parsed.
     *               The buffer's position will be advanced during parsing.
     * @return A non-null byte array containing the parsed ASCII string, excluding the null terminator.
     */
    @NonNull
    private static byte[] parseStringToNullForASCII(@NonNull final ByteBuffer buffer) {
        ArrayList<Byte> bytes = new ArrayList<>();
        byte[] ret;

        while (true) {
            byte b = buffer.get();
            if (b != 0x00)
                bytes.add(b);
            else
                break;
        }

        ret = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            ret[i] = bytes.get(i);

        return ret;
    }

    /**
     * Parses a null-terminated Unicode string from the given {@link ByteBuffer}.
     *
     * @param buffer The {@link ByteBuffer} from which the Unicode string is to be parsed.
     *               The buffer's position will be advanced during parsing.
     * @return A non-null byte array containing the parsed Unicode string in UTF-16 encoding,
     * excluding the null terminator.
     */
    @NonNull
    private static byte[] parseStringToNullForUnicode(@NonNull final ByteBuffer buffer) {
        ArrayList<Character> chars = new ArrayList<>();
        byte[] ret;

        while (true) {
            char c = buffer.getChar();
            if (c != 0x00)
                chars.add(c);
            else
                break;
        }

        ret = new byte[chars.size() * 2];
        for (int i = 0; i < chars.size(); i++) {
            ret[i * 2] = (byte) (chars.get(i) >> 8);
            ret[i * 2 + 1] = (byte) (chars.get(i) & 0xFF);
        }

        return ret;
    }

    /**
     * Parses a string of the specified size from the given {@link ByteBuffer}.
     *
     * @param buffer The {@link ByteBuffer} from which the string is to be parsed.
     *               The buffer's position will be advanced by the specified size during parsing.
     * @param size   The number of bytes to read from the buffer.
     * @return A non-null byte array containing the parsed string of the specified size.
     */
    @NonNull
    private static byte[] parseStringBySize(@NonNull final ByteBuffer buffer, int size) {
        byte[] ret = new byte[size];
        buffer.get(ret, 0, size);
        return ret;
    }

    /**
     * A class used to store some necessary variables.
     */
    private static class Position {
        public int shellLinkStartPos;
        public int shellLinkHeaderStartPos;
        public int shellLinkHeaderSize;
        public int linkTargetIDListStartPos;
        public int linkTargetIDListSize;
        public int linkInfoStartPos;
        public int linkInfoSize;
        public int stringDataStartPos;
        public int stringDataSize;
        public int extraDataStartPos;
        public int extraDataSize;
    }
}
