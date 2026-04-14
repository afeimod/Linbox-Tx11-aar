package com.winfusion.core.shm.driver;

import android.util.Log;

import androidx.annotation.NonNull;

import com.winfusion.core.shm.exception.SHMDriverException;
import com.winfusion.core.shm.exception.SHMServerException;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MIDIDriver implements BaseSHMDriver {

    private static final long NullPtr = 0;
    private static final int InvalidFd = -1;
    private long jniHandle = NullPtr;
    private final ExecutorService executorService;
    private final String sfPath;
    private Future<?> future;
    private int memFD = InvalidFd;
    private final Runnable loop;

    public MIDIDriver(@NonNull String sfPath, @NonNull ExecutorService executorService,
                      @NonNull DriverRuntimeCallback callback) {
        this.sfPath = sfPath;
        this.executorService = executorService;
        loop = () -> {
            try {
                startDriverLoop(jniHandle);
            } catch (SHMServerException e) {
                callback.onRuntimeFatal(this, e);
            }
        };
    }

    public MIDIDriver(@NonNull File sfFile, @NonNull ExecutorService executorService,
                      @NonNull DriverRuntimeCallback callback) {
        this(sfFile.getAbsolutePath(), executorService, callback);
    }

    @NonNull
    @Override
    public String name() {
        return "WinfusionMIDIDriver";
    }

    @Override
    public boolean attach() throws SHMDriverException {
        if (jniHandle != NullPtr)
            return false;

        if ((jniHandle = setupMIDISynth(sfPath)) == NullPtr)
            return false;

        if ((memFD = setupSharedMemory(jniHandle)) < 0) {
            detach();
            return false;
        }

        future = executorService.submit(loop);

        return true;
    }

    @Override
    public void detach() {
        if (jniHandle == NullPtr)
            return;

        if (future != null && !future.isDone()) {
            stopDriverLoop(jniHandle);
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                Log.d("MIDIDriver", "Driver loop interrupted.", e);
            }
        }

        future = null;
        doClean(jniHandle);
        memFD = InvalidFd;
        jniHandle = NullPtr;
    }

    @Override
    public int getSharedMemoryFd() {
        return memFD;
    }

    private native long setupMIDISynth(String sfPath) throws SHMDriverException;

    private native int setupSharedMemory(long handle) throws SHMDriverException;

    private native void startDriverLoop(long handle) throws SHMServerException;

    private native void stopDriverLoop(long handle);

    private native void doClean(long handle);
}
