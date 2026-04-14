package com.winfusion.core.mslink.section.block;

public class KnownFolderDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA000000B;
    private static final int BlockSize = 0x0000001C;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
