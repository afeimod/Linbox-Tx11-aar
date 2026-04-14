package com.winfusion.core.mslink.section.block;

public class PropertyStoreDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000009;
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
