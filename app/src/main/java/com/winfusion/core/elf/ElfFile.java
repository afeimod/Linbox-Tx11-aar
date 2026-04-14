package com.winfusion.core.elf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.elf.exception.ELFException;

import java.nio.file.Path;

/**
 * ELF 文件类，用于解析和编辑 ELF 文件。
 */
public class ElfFile {

    static {
        System.loadLibrary("patchelf");
    }

    /**
     * 表示文件是一个可执行文件。
     */
    public static final int ET_EXEC = 2;

    /**
     * 表示文件是一个共享对象文件。
     */
    public static final int ET_DYN = 3;

    private static final int NullPtr = 0;
    private static final String RPathSplit = ":";

    private final Path filePath;
    private final long jniHandle;

    /**
     * 构造函数。
     *
     * @param filePath 文件路径
     * @throws ELFException 如果文件不是 elf 文件
     */
    public ElfFile(@NonNull Path filePath) throws ELFException {
        this.filePath = filePath;
        jniHandle = createElfFile(filePath.toAbsolutePath().toString());
        if (jniHandle == NullPtr)
            throw new ELFException("Failed to parse elf.");
    }

    /**
     * 获取文件路径。
     *
     * @return 文件路径
     */
    @NonNull
    public Path getFilePath() {
        return filePath;
    }

    /**
     * 写入当前 ELF 文件到目标路径。
     *
     * @param target 目标路径
     * @return 如果成功则返回 true，否则返回 false
     */
    public boolean write(@NonNull Path target) {
        return writeToFile(jniHandle, target.toAbsolutePath().toString());
    }

    /**
     * 获取 ELF 类型。
     * 返回值只可能是 {@link #ET_DYN} 或 {@link  #ET_EXEC}。
     *
     * @return ELF 类型
     */
    public int getElfType() {
        return getElfType(jniHandle);
    }

    /**
     * 判断当前 ELF 是否发生了更改。
     *
     * @return 如果发生了更改则返回 true，否则返回 false
     */
    public boolean isChanged() {
        return isChanged(jniHandle);
    }

    /**
     * 设置解释器路径。
     *
     * @param interpreter 解释器路径
     * @throws ELFException 如果解释器路径不合法或者 ELF 不支持解释器
     */
    public void setInterpreter(@NonNull String interpreter) throws ELFException {
        setInterpreter(jniHandle, interpreter);
    }

    /**
     * 获取解释器路径。
     *
     * @return 解释器路径
     * @throws ELFException 如果 ELF 不支持解释器
     */
    @NonNull
    public String getInterpreter() throws ELFException {
        return getInterpreter(jniHandle);
    }

    /**
     * 设置链接库名称。
     *
     * @param soName 链接库名称
     * @return 如果成功则返回 true，否则返回 false
     * @throws ELFException 如果发生错误
     */
    public boolean setSoName(@NonNull String soName) throws ELFException {
        if (getElfType() == ET_DYN) {
            setSoName(jniHandle, soName);
            return true;
        }
        return false;
    }

    /**
     * 获取链接库名称。
     *
     * @return 链接库名称
     * @throws ELFException 如果发生错误
     */
    @Nullable
    public String getSoName() throws ELFException {
        if (getElfType() == ET_DYN)
            return getSoName(jniHandle);
        return null;
    }

    /**
     * 设置运行时共享库路径。
     *
     * @param rpath 共享库路径
     * @throws ELFException 如果发生错误
     */
    public void setRPath(@NonNull String[] rpath) throws ELFException {
        setRPath(jniHandle, String.join(RPathSplit, rpath));
    }

    /**
     * 获取运行时共享库路径。
     *
     * @return 共享库路径
     * @throws ELFException 如果发生错误
     */
    @NonNull
    public String[] getRPath() throws ELFException {
        return getRPath(jniHandle).split(RPathSplit);
    }

    /**
     * 添加依赖库。
     *
     * @param needed 依赖库
     * @throws ELFException 如果发生错误
     */
    public void addNeeded(@NonNull String needed) throws ELFException {
        addNeeded(jniHandle, needed);
    }

    /**
     * 删除依赖库。
     *
     * @param needed 依赖库
     * @throws ELFException 如果发生错误
     */
    public void removeNeeded(@NonNull String needed) throws ELFException {
        removeNeeded(jniHandle, needed);
    }

    /**
     * 销毁这个 ELF 对象。
     */
    public void destroy() {
        if (jniHandle != NullPtr)
            releaseHandle(jniHandle);
    }

    /**
     * 判断该对象是否已经被销毁。
     *
     * @return 如果已经被销毁则返回 true，否则返回 false
     */
    public boolean isDestroyed() {
        return jniHandle == NullPtr;
    }

    /**
     * 获取全部依赖库。
     *
     * @return 依赖库数组
     * @throws ELFException 如果发生错误
     */
    @NonNull
    public String[] getNeeded() throws ELFException {
        String[] libs = getNeeded(jniHandle);
        return libs == null ? new String[0] : libs;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    private native long createElfFile(@NonNull String path) throws ELFException;

    private native boolean writeToFile(long handle, @NonNull String path);

    private native int getElfType(long handle);

    private native boolean isChanged(long handle);

    private native void setInterpreter(long handle, @NonNull String interpreter) throws ELFException;

    @NonNull
    private native String getInterpreter(long handle) throws ELFException;

    private native void setSoName(long handle, @NonNull String soname) throws ELFException;

    @NonNull
    private native String getSoName(long handle) throws ELFException;

    private native void setRPath(long handle, @NonNull String rpath) throws ELFException;

    @NonNull
    private native String getRPath(long handle) throws ELFException;

    private native void addNeeded(long handle, @NonNull String needed) throws ELFException;

    private native void removeNeeded(long handle, @NonNull String needed) throws ELFException;

    @Nullable
    private native String[] getNeeded(long handle) throws ELFException;

    private native void releaseHandle(long handle);
}
