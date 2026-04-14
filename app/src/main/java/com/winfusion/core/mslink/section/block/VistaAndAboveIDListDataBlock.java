package com.winfusion.core.mslink.section.block;

public class VistaAndAboveIDListDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA000000C;
    private static final int BlockSize = 0x0000000A;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
