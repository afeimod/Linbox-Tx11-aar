package com.winfusion.core.mslink.section;

import androidx.annotation.Nullable;

public class StringData {

    private ByteData nameString;
    private ByteData relativePath;
    private ByteData workingDir;
    private ByteData commandLineArguments;
    private ByteData iconLocation;

    public boolean isEmpty() {
        return nameString == null && relativePath == null && workingDir == null &&
                commandLineArguments == null && iconLocation == null;
    }

    @Nullable
    public ByteData getNameString() {
        return nameString;
    }

    @Nullable
    public ByteData getRelativePath() {
        return relativePath;
    }

    @Nullable
    public ByteData getWorkingDir() {
        return workingDir;
    }

    @Nullable
    public ByteData getCommandLineArguments() {
        return commandLineArguments;
    }

    @Nullable
    public ByteData getIconLocation() {
        return iconLocation;
    }

    public void setNameString(@Nullable ByteData nameString) {
        this.nameString = nameString;
    }

    public void setRelativePath(@Nullable ByteData relativePath) {
        this.relativePath = relativePath;
    }

    public void setWorkingDir(@Nullable ByteData workingDir) {
        this.workingDir = workingDir;
    }

    public void setCommandLineArguments(@Nullable ByteData commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }

    public void setIconLocation(@Nullable ByteData iconLocation) {
        this.iconLocation = iconLocation;
    }
}
