package com.winfusion.core.wfp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class Wfp {

    public enum Source {
        external,
        builtin
    }

    public static final int VERSION_1 = 1;

    private String wfpHome;
    private WfpType wfpType = WfpType.Unknown;
    private Source source = Source.builtin;
    private int schemaVersion;

    // version 1
    private String name;
    private String author;
    private String comment;
    private String packageCopyright;
    private String packageLicense;
    private String libraryCopyright;
    private String libraryLicense;
    private String details;
    private Map<String, String> property = new TreeMap<>();

    @Nullable
    public String getWfpHome() {
        return wfpHome;
    }

    @NonNull
    public WfpType getWfpType() {
        return wfpType;
    }

    @NonNull
    public Source getSource() {
        return source;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @Nullable
    public String getPackageCopyright() {
        return packageCopyright;
    }

    @Nullable
    public String getPackageLicense() {
        return packageLicense;
    }

    @Nullable
    public String getLibraryCopyright() {
        return libraryCopyright;
    }

    @Nullable
    public String getLibraryLicense() {
        return libraryLicense;
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    @NonNull
    public Map<String, String> getProperty() {
        return property;
    }

    public void setWfpHome(@Nullable String wfpHome) {
        this.wfpHome = wfpHome;
    }

    public void setWfpType(@NonNull WfpType wfpType) {
        this.wfpType = wfpType;
    }

    public void setSource(@NonNull Source source) {
        this.source = source;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    public void setPackageCopyright(@Nullable String packageCopyright) {
        this.packageCopyright = packageCopyright;
    }

    public void setPackageLicense(@Nullable String packageLicense) {
        this.packageLicense = packageLicense;
    }

    public void setLibraryCopyright(@Nullable String libraryCopyright) {
        this.libraryCopyright = libraryCopyright;
    }

    public void setLibraryLicense(@Nullable String libraryLicense) {
        this.libraryLicense = libraryLicense;
    }

    public void setDetails(@NonNull String details) {
        this.details = details;
    }

    public void setProperty(@NonNull Map<String, String> property) {
        this.property = property;
    }

    @NonNull
    public String toIdentifier() {
        return name;
    }
}
