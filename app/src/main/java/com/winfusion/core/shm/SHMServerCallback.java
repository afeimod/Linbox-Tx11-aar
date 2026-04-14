package com.winfusion.core.shm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.shm.driver.BaseSHMDriver;
import com.winfusion.core.shm.exception.SHMServerException;

public interface SHMServerCallback {

    void onRuntimeFatal(SHMServerException e);

    @Nullable
    BaseSHMDriver onRequireDriver(int clientType);

    void onDriverAttached(@NonNull BaseSHMDriver driver);

    void onDriverDetached(@NonNull BaseSHMDriver driver);
}
