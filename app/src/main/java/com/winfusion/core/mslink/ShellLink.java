package com.winfusion.core.mslink;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.mslink.section.ExtraData;
import com.winfusion.core.mslink.section.LinkInfo;
import com.winfusion.core.mslink.section.LinkTargetIDList;
import com.winfusion.core.mslink.section.ShellLinkHeader;
import com.winfusion.core.mslink.section.StringData;

/**
 * Represents a Shell Link (shortcut) file structure, containing various sections that define
 * its properties and metadata.
 */
public class ShellLink {

    /**
     * Header section of Shell Link
     */
    private ShellLinkHeader header;

    /**
     * LinkTargetIDList section of Shell Link
     */
    private LinkTargetIDList linkTargetIDList;

    /**
     * LinkInfo section of Shell Link
     */
    private LinkInfo linkInfo;

    /**
     * StringData section of Shell Link
     */
    private StringData stringData;

    /**
     * ExtraData section of Shell Link
     */
    private ExtraData extraData;

    public ShellLink() {
        header = new ShellLinkHeader();
        stringData = new StringData();
    }

    @NonNull
    public ShellLinkHeader getHeader() {
        return header;
    }

    @Nullable
    public LinkTargetIDList getLinkTargetIDList() {
        return linkTargetIDList;
    }

    @Nullable
    public LinkInfo getLinkInfo() {
        return linkInfo;
    }

    @NonNull
    public StringData getStringData() {
        return stringData;
    }

    @Nullable
    public ExtraData getExtraData() {
        return extraData;
    }

    public void setHeader(@NonNull ShellLinkHeader header) {
        this.header = header;
    }

    public void setLinkTargetIDList(@Nullable LinkTargetIDList linkTargetIDList) {
        this.linkTargetIDList = linkTargetIDList;
    }

    public void setLinkInfo(@Nullable LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }

    public void setStringData(@NonNull StringData stringData) {
        this.stringData = stringData;
    }

    public void setExtraData(@Nullable ExtraData extraData) {
        this.extraData = extraData;
    }
}
