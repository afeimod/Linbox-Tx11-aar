package com.winfusion.core.mslink.section.block;

public class ShimDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000008;
    private static final int BlockSize = 0x00000088;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
