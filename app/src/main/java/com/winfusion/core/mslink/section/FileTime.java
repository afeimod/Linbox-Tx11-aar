package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Represents a Windows FILETIME, which is the number of 100-nanosecond intervals
 * since January 1, 1601 (UTC). Used for time information in Windows files.
 */
public class FileTime implements Comparable<FileTime> {

    /**
     * Constant to convert FILETIME to Unix epoch in milliseconds
     */
    private static final long FILETIME_TO_UNIX_EPOCH_MILLIS = 11644473600000L;

    /**
     * Constant to convert time units to hundred-nanosecond intervals
     */
    private static final long HUNDRED_NANOSECONDS = 10000;

    /**
     * The internal representation of file time in 100-nanosecond intervals
     */
    private long fileTime;

    /**
     * Constructs a FileTime object with a default time of 0.
     */
    public FileTime() {
        setTime(0);
    }

    /**
     * Constructs a FileTime object from a specified file time (in 100-nanosecond intervals).
     *
     * @param fileTime The file time in 100-nanosecond intervals.
     */
    public FileTime(long fileTime) {
        setTime(fileTime);
    }

    /**
     * Constructs a FileTime object from a {@link Date} object.
     *
     * @param date The Date object to convert to file time.
     */
    public FileTime(Date date) {
        setTime(date);
    }

    /**
     * Sets the internal file time in 100-nanosecond intervals.
     *
     * @param time The file time in 100-nanosecond intervals.
     */
    public void setTime(long time) {
        fileTime = time;
    }

    /**
     * Sets the internal file time based on a Date object.
     * Converts the date to 100-nanosecond intervals since January 1, 1601 (UTC).
     *
     * @param date The Date object to convert to file time.
     */
    public void setTime(@NonNull Date date) {
        fileTime = (date.getTime() + FILETIME_TO_UNIX_EPOCH_MILLIS) * HUNDRED_NANOSECONDS;
    }

    /**
     * Retrieves the file time in 100-nanosecond intervals.
     *
     * @return The internal file time in 100-nanosecond intervals.
     */
    public long getTime() {
        return fileTime;
    }

    /**
     * Converts the file time to a {@link Date} object.
     *
     * @return The Date object corresponding to the file time.
     */
    public Date getDate() {
        return new Date((fileTime / HUNDRED_NANOSECONDS) - FILETIME_TO_UNIX_EPOCH_MILLIS);
    }

    @Override
    public int compareTo(FileTime f) {
        return Long.compare(fileTime, f.fileTime);
    }
}
