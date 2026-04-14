package com.winfusion.core.mslink.section.block;

public class SpecialFolderDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000005;
    private static final int BlockSize = 0x00000010;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
