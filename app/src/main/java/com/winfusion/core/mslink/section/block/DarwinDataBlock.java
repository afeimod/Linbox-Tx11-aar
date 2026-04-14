package com.winfusion.core.mslink.section.block;

public class DarwinDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000006;
    private static final int BlockSize = 0x00000314;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
