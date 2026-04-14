package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 注册表项中数据的基类，不同类型由子类实现。
 */
public abstract class RegistryData {

    private final DataType dataType;

    public RegistryData(@NonNull DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * 判断当前对象的数据类型是否为指定的数据类型。
     *
     * @param dataType 待判断的数据类型
     * @return 类型匹配则返回 true，否则返回 false
     */
    public boolean isData(@NonNull DataType dataType) {
        return this.dataType == dataType;
    }

    /**
     * 将当前对象强制转换为 {@link NoneData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public NoneData getAsNone() {
        if (isData(DataType.REG_NONE))
            return (NoneData) this;
        throw new UnsupportedOperationException("Not a none: " + this);
    }


    /**
     * 将当前对象强制转换为 {@link StringData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public StringData getAsString() {
        if (isData(DataType.REG_SZ))
            return (StringData) this;
        throw new UnsupportedOperationException("Not a string: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link ExpandStringData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public ExpandStringData getAsExpandString() {
        if (isData(DataType.REG_EXPAND_SZ))
            return (ExpandStringData) this;
        throw new UnsupportedOperationException("Not a expand string: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link BinaryData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public BinaryData getAsBinary() {
        if (isData(DataType.REG_BINARY))
            return (BinaryData) this;
        throw new UnsupportedOperationException("Not a binary: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link DoubleWordData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public DoubleWordData getAsDword() {
        if (isData(DataType.REG_DWORD))
            return (DoubleWordData) this;
        throw new UnsupportedOperationException("Not a dword: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link DoubleWordBigEndianData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public DoubleWordBigEndianData getAsDwordBigEndian() {
        if (isData(DataType.REG_DWORD_BIG_ENDIAN))
            return (DoubleWordBigEndianData) this;
        throw new UnsupportedOperationException("Not a dowrd big endian: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link LinkData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public LinkData getAsLink() {
        if (isData(DataType.REG_LINK))
            return (LinkData) this;
        throw new UnsupportedOperationException("Not a symbolic link: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link MultiStringData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public MultiStringData getAsMultiString() {
        if (isData(DataType.REG_MULTI_SZ))
            return (MultiStringData) this;
        throw new UnsupportedOperationException("Not a multi string: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link ResourceListData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public ResourceListData getAsResourceList() {
        if (isData(DataType.REG_RESOURCE_LIST))
            return (ResourceListData) this;
        throw new UnsupportedOperationException("Not a resource list: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link FullResourceDescriptorData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public FullResourceDescriptorData getAsFullResourceDescriptor() {
        if (isData(DataType.REG_FULL_RESOURCE_DESCRIPTOR))
            return (FullResourceDescriptorData) this;
        throw new UnsupportedOperationException("Not a full resource descriptor: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link ResourceRequirementsListData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public ResourceRequirementsListData getAsResourceRequirementsList() {
        if (isData(DataType.REG_RESOURCE_REQUIREMENTS_LIST))
            return (ResourceRequirementsListData) this;
        throw new UnsupportedOperationException("Not a resource requirements list: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link QuadWordData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public QuadWordData getAsQword() {
        if (isData(DataType.REG_QWORD))
            return (QuadWordData) this;
        throw new UnsupportedOperationException("Not a qword: " + this);
    }

    /**
     * 将当前对象强制转换为 {@link RawData} 类型。
     *
     * @return 转换后的对象
     * @throws UnsupportedOperationException 如果数据类型不匹配
     */
    @NonNull
    public RawData getAsRaw() {
        if (isData(DataType.REG_RAW))
            return (RawData) this;
        throw new UnsupportedOperationException("Not a raw: " + this);
    }

    /**
     * 获取当前对象的数据类型。
     *
     * @return 数据类型
     */
    @NonNull
    public DataType getDataType() {
        return dataType;
    }

    /**
     * 获取当前对象数据的字节数组表示，具体实现由子类提供。
     *
     * @return 对象数据的字节数组表示
     */
    @NonNull
    public abstract byte[] toBytes();

    /**
     * 判断当前对象与目标对象是否相等，只比较引用和类型，具体字段比较由子类实现。
     *
     * @param obj 目标对象
     * @return 是否相等
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        return obj != null && obj.getClass() == getClass();
    }
}
