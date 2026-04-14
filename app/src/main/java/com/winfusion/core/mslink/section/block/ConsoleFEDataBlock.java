package com.winfusion.core.mslink.section.block;

public class ConsoleFEDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000004;
    private static final int BlockSize = 0x0000000C;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
