package com.winfusion.core.mslink.section.block;

import androidx.annotation.NonNull;

public class RawBlock extends BaseExtraDataBlock {

    private final byte[] bytes;

    public RawBlock(byte[] bytes) {
        this.bytes = bytes;
    }

    @NonNull
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int getBlockSignature() {
        return 0;
    }

    @Override
    public int getBlockSize() {
        return 0;
    }
}
