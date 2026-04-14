package com.winfusion.core.mslink.section.block;

public class ConsoleDataBlock extends BaseExtraDataBlock {

    private static final int BlockSignature = 0xA0000002;
    private static final int BlockSize = 0x000000CC;

    @Override
    public int getBlockSignature() {
        return BlockSignature;
    }

    @Override
    public int getBlockSize() {
        return BlockSize;
    }
}
