package com.winfusion.core.shm.driver;

import androidx.annotation.NonNull;

import com.winfusion.core.shm.exception.SHMDriverException;

public interface BaseSHMDriver {

    @NonNull
    String name();

    boolean attach() throws SHMDriverException;

    void detach();

    int getSharedMemoryFd();
}
