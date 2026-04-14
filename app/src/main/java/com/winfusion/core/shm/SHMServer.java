package com.winfusion.core.shm;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.shm.driver.BaseSHMDriver;
import com.winfusion.core.shm.exception.SHMDriverException;
import com.winfusion.core.shm.exception.SHMServerException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SHMServer {

    static {
        System.loadLibrary("winfusion");
    }

    private static final String TAG = "SHMServer";
    private static final long NullPtr = 0;

    private final String unixSocketPath;
    private Future<?> mFuture;
    private long jniHandle = NullPtr;
    private final SHMServerCallback callback;
    private final ConcurrentHashMap<Integer, BaseSHMDriver> clientDriverMap;
    private final Runnable epoll_loop;

    public SHMServer(@NonNull String unixSocketPath, @NonNull SHMServerCallback callback) {
        this.unixSocketPath = unixSocketPath;
        this.callback = callback;
        clientDriverMap = new ConcurrentHashMap<>();
        epoll_loop = () -> {
            try {
                epollLoop(jniHandle);
            } catch (SHMServerException e) {
                callback.onRuntimeFatal(e);
            }
        };
    }

    @NonNull
    public String getUnixSocketPath() {
        return unixSocketPath;
    }

    public void start(@NonNull ExecutorService service) throws SHMServerException {
        if (jniHandle == NullPtr) {
            jniHandle = createServerSocket(unixSocketPath);

            if (jniHandle == NullPtr)
                throw new SHMServerException("Failed to start ShmServer.");

            if (!createEpollAndCtrl(jniHandle)) {
                doClean(jniHandle);
                jniHandle = NullPtr;
                throw new SHMServerException("Failed to create Epoll for ShmServer.");
            }

            mFuture = service.submit(epoll_loop);
        }
    }

    public void stop() {
        if (jniHandle != NullPtr) {
            // stop epoll loop and wait it
            // JNIOnClientRequireDriver and JNIOnClientClosed won't be called anymore.
            sendEpollShutDown(jniHandle);

            try {
                mFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                Log.d(TAG, "Epoll loop thread has been interrupted.");
            }

            // close all clients and detach all drivers
            for (Map.Entry<Integer, BaseSHMDriver> entry : clientDriverMap.entrySet()) {
                closeClientFd(entry.getKey());
                entry.getValue().detach();
                callback.onDriverDetached(entry.getValue());
            }

            // clean jni side
            doClean(jniHandle);

            // clean map
            clientDriverMap.clear();
            jniHandle = NullPtr;
            mFuture = null;
        }
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("unused")
    private void JNIOnClientRequireDriver(int clientFd, int clientType) {
        if (clientDriverMap.containsKey(clientFd)) {
            callback.onRuntimeFatal(new SHMServerException(
                    String.format("Client should require driver only one time. (fd:%d, type:%d)",
                            clientFd, clientType)
            ));
            return;
        }

        if (!setupDriverToClient(callback.onRequireDriver(clientType), clientFd)) {
            callback.onRuntimeFatal(new SHMServerException(
                    String.format("Failed to get or detach driver. (fd:%d, type:%d)",
                            clientFd, clientType)
            ));
        }
    }

    @SuppressWarnings("unused")
    private void JNIOnClientClosed(int clientFd) {
        removeDriverFromClient(clientFd);
    }

    private boolean setupDriverToClient(@Nullable BaseSHMDriver driver, int clientFd) {
        try {
            if (driver != null && driver.attach()) {
                sendSharedMemoryToClient(clientFd, driver.getSharedMemoryFd());
                clientDriverMap.put(clientFd, driver);
                callback.onDriverAttached(driver);
                return true;
            }
        } catch (SHMDriverException e) {
            callback.onRuntimeFatal(new SHMServerException(e));
        } catch (SHMServerException e) {
            Log.d(TAG, "Failed to send mem_fd.", e);
            callback.onRuntimeFatal(e);
        }
        return false;
    }

    private void removeDriverFromClient(int clientFd) {
        BaseSHMDriver driver = clientDriverMap.get(clientFd);
        clientDriverMap.remove(clientFd);
        if (driver != null) {
            driver.detach();
            callback.onDriverDetached(driver);
        }
    }

    private native long createServerSocket(@NonNull String path) throws SHMServerException;

    private native boolean createEpollAndCtrl(long handle) throws SHMServerException;

    private native void epollLoop(long handle) throws SHMServerException;

    private native void sendEpollShutDown(long handle);

    private native void doClean(long handle);

    private native void sendSharedMemoryToClient(int client_fd, int mem_fd) throws SHMServerException;

    private native void closeClientFd(int client_fd);
}
