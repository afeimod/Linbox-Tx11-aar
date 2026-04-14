package com.winfusion.core.shm.driver;

import androidx.annotation.NonNull;

public interface DriverRuntimeCallback {

    void onRuntimeFatal(@NonNull BaseSHMDriver driver, @NonNull Exception e);
}
