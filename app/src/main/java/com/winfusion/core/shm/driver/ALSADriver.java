package com.winfusion.core.shm.driver;

import androidx.annotation.NonNull;

import com.winfusion.core.shm.exception.SHMDriverException;

public class ALSADriver implements BaseSHMDriver {

    @NonNull
    @Override
    public String name() {
        return "WinfusionAlsaDriver";
    }

    @Override
    public boolean attach() throws SHMDriverException {
        return false;
    }

    @Override
    public void detach() {

    }

    @Override
    public int getSharedMemoryFd() {
        return 0;
    }
}
