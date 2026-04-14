package com.winfusion.feature.content.model;

import androidx.annotation.NonNull;

import com.winfusion.core.soundfont.SoundFontInfo;

public class SoundfontModel extends BaseContentModel {

    private final String fileName;
    private final SoundFontInfo info;

    public SoundfontModel(@NonNull String fileName, @NonNull SoundFontInfo info) {
        this.fileName = fileName;
        this.info = info;
    }

    @NonNull
    public String getFileName() {
        return fileName;
    }

    @NonNull
    public SoundFontInfo getInfo() {
        return info;
    }
}
