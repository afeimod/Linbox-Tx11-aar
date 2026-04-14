package com.winfusion.core.mslink.section.block;

/**
 * Base class for ExtraDataBlock.
 */
public abstract class BaseExtraDataBlock {

    /**
     * Gets the signature of the block.
     * Each block should have a unique signature identifying it.
     *
     * @return the block signature.
     */
    public abstract int getBlockSignature();

    /**
     * Gets the size of the block.
     * The size can vary depending on the block type and its data.
     *
     * @return the size of the block in bytes.
     */
    public abstract int getBlockSize();
}
