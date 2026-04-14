package com.winfusion.core.mslink.section.block;

public class TrackerDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000003;
    private static final int BlockSize = 0x00000060;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
