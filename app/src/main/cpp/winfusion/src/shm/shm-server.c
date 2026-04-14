#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <stdbool.h>
#include <sys/socket.h>
#include <sys/epoll.h>
#include <sys/eventfd.h>
#include <shm/shm-server.h>
#include <shm/shm-client.h>
#include <common/log.h>

#define TAG "JNIShmServer"
#define SHMExceptionClassName "com/winfusion/core/shm/exception/SHMServerException"

#define ThrowSHMException(msg, ...) \
    throwJavaException(env, SHMExceptionClassName, msg, ##__VA_ARGS__)

#define ThrowSHMExceptionWithErrno(msg, ...) \
    throwJavaExceptionWithErrno(env, SHMExceptionClassName, msg, ##__VA_ARGS__)

static bool callJavaOnClientRequireDriver(JNIEnv *, jobject, int, int);
static bool callJavaOnClientClosed(JNIEnv *, jobject, int);

static bool callJavaOnClientRequireDriver(JNIEnv *env, jobject thiz, int client_fd, int client_type) {
    jclass clazz;
    jmethodID methodId;

    if (!(clazz = (*env)->GetObjectClass(env, thiz))) {
        LOGE(TAG, "Failed to find class of this instance.");
        return false;
    }

    if (!(methodId = (*env)->GetMethodID(env, clazz, "JNIOnClientRequireDriver", "(II)V"))) {
        LOGE(TAG, "Failed to find OnClientRequireDriver method of this instance.");
        return false;
    }

    (*env)->CallVoidMethod(env, thiz, methodId, client_fd, client_type);

    return true;
}

static bool callJavaOnClientClosed(JNIEnv *env, jobject thiz, int client_fd) {
    jclass clazz;
    jmethodID methodId;

    if (!(clazz = (*env)->GetObjectClass(env, thiz))) {
        LOGE(TAG, "Failed to find class of this instance.");
        return false;
    }

    if (!(methodId = (*env)->GetMethodID(env, clazz, "JNIOnClientClosed", "(I)V"))) {
        LOGE(TAG, "Failed to find OnClientClosed method of this instance.");
        return false;
    }

    (*env)->CallVoidMethod(env, thiz, methodId, client_fd);

    return true;
}

JNIEXPORT jlong JNICALL
Java_com_winfusion_core_shm_SHMServer_createServerSocket(JNIEnv *env, jobject thiz, jstring path) {
    SHMServerData *data = NULL;
    struct sockaddr_un serverAddr;
    int server_fd = -1;
    const char *socketPath = NULL;

    if ((*env)->GetStringUTFLength(env, path) > UNIX_PATH_MAX) {
        ThrowSHMException("Socket path is too long.");
        goto error_clean;
    }

    if (!(data = (SHMServerData *) malloc(sizeof(SHMServerData)))) {
        ThrowSHMExceptionWithErrno("Failed to allocate memory for ShmServerData");
        goto error_clean;
    }

    if ((server_fd = socket(AF_UNIX, SOCK_STREAM, 0)) == -1) {
        ThrowSHMExceptionWithErrno("Failed to create server socket.");
        goto error_clean;
    }

    socketPath = (*env)->GetStringUTFChars(env, path, NULL);
    memset(&serverAddr, 0, sizeof(struct sockaddr_un));
    serverAddr.sun_family = AF_UNIX;
    strncpy(serverAddr.sun_path, socketPath, sizeof(serverAddr.sun_path) - 1);
    unlink(socketPath);

    if (bind(server_fd, (struct sockaddr *) &serverAddr, sizeof(serverAddr)) == -1) {
        ThrowSHMExceptionWithErrno("Failed to bind server socket.");
        goto error_clean;
    }

    strncpy(data->socketPath, socketPath, sizeof(data->socketPath)- 1);
    (*env)->ReleaseStringUTFChars(env, path, socketPath);

    data->server_fd = server_fd;
    return (jlong) data;

error_clean:
    if (data)
        free(data);

    if (server_fd != -1)
        close(server_fd);

    if (socketPath)
        (*env)->ReleaseStringUTFChars(env, path, socketPath);

    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_winfusion_core_shm_SHMServer_createEpollAndCtrl(JNIEnv *env, jobject thiz, jlong handle) {
    SHMServerData *shmServerData = (SHMServerData *) handle;
    int epoll_fd = -1, epoll_ctrl_fd = -1;
    struct epoll_event ev;

    if (!shmServerData || shmServerData->server_fd < 0) {
        ThrowSHMException("Bad SHMServerData handle.");
        goto error_clean;
    }

    if ((epoll_fd = epoll_create1(0)) == -1) {
        ThrowSHMExceptionWithErrno("Failed to create epoll.");
        goto error_clean;
    }

    if ((epoll_ctrl_fd = eventfd(0, 0)) == -1) {
        ThrowSHMExceptionWithErrno("Failed to create epoll_ctrl.");
        goto error_clean;
    }

    ev.events = EPOLLIN;
    ev.data.fd = epoll_ctrl_fd;
    if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, epoll_ctrl_fd, &ev) == -1) {
        ThrowSHMExceptionWithErrno("Failed to add epoll_ctrl to epoll.");
        goto error_clean;
    }

    ev.data.fd = shmServerData->server_fd;
    if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, shmServerData->server_fd, &ev) == -1) {
        ThrowSHMExceptionWithErrno("Failed to add server_fd to epoll.");
        goto error_clean;
    }

    shmServerData->epoll_fd = epoll_fd;
    shmServerData->epoll_ctrl_fd = epoll_ctrl_fd;
    return true;

error_clean:
    if (epoll_fd)
        close(epoll_fd);

    if (epoll_ctrl_fd)
        close(epoll_ctrl_fd);

    return false;
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_SHMServer_epollLoop(JNIEnv *env, jobject thiz, jlong handle) {
    SHMServerData *shmServerData = (SHMServerData *) handle;
    struct epoll_event ev, events[MAX_EPOLL_EVENT], *current_event;
    int server_fd, epoll_fd, epoll_ctrl_fd, client_fd, current_fd, n_fds;

    if (!shmServerData) {
        ThrowSHMException("Bad SHMServerData handle.");
        return;
    }

    server_fd = shmServerData->server_fd;
    epoll_fd = shmServerData->epoll_fd;
    epoll_ctrl_fd = shmServerData->epoll_ctrl_fd;
    if (server_fd < 0 || epoll_fd < 0 || epoll_ctrl_fd < 0) {
        ThrowSHMException("Bad SHMServerData handle.");
        return;
    }

    if (listen(shmServerData->server_fd, 10) == -1) {
        ThrowSHMException("Failed to listen server_fd.");
        return;
    }

    while (1) {
        n_fds = epoll_wait(epoll_fd, events, 32, -1);

        if (n_fds == -1) {
            if (errno == EINTR)
                continue;
            ThrowSHMExceptionWithErrno("Failed to wait fd.");
            goto quit;
        }

        for (int i = 0; i < n_fds; i++) {
            current_event = &events[i];
            current_fd = current_event->data.fd;

            if (current_fd == server_fd) {
                client_fd = accept(server_fd, NULL, NULL);
                if (client_fd == -1) {
                    LOGE(TAG, "Received a invalid client_fd: -1");
                    goto quit;
                }

                ev.events = EPOLLIN | EPOLLET;
                ev.data.fd = client_fd;
                if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &ev) == -1) {
                    ThrowSHMExceptionWithErrno("Failed to add client_fd to epoll.");
                    close(client_fd);
                    goto quit;
                }

                LOGD(TAG, "A client connected: %d", client_fd);
            } else if (current_fd == epoll_ctrl_fd) {
                int value;
                read(epoll_ctrl_fd, &value, sizeof(value));
                LOGD(TAG, "Received shutdown ctrl: %d", current_fd);
                goto quit;
            } else {
                client_fd = current_event->data.fd;
                char request[CLIENT_REQUEST_BUFFER_SIZE];
                ssize_t len = read(client_fd, request, sizeof(request));

                if (len == 0) {
                    if (epoll_ctl(epoll_fd, EPOLL_CTL_DEL, client_fd, NULL) == -1)
                        LOGE(TAG, "Failed to remove client_fd from epoll.");

                    if (!callJavaOnClientClosed(env, thiz, client_fd)) {
                        ThrowSHMException("Failed to call java method JNIOnClientClosed.");
                        goto quit;
                    }

                    close(client_fd);
                    LOGD(TAG, "A client disconnected: %d", client_fd);
                } else if (len > 0) {
                    CLIENT_REQUEST_SIZE_TYPE request_size = get_request_size(request);
                    if (request_size > sizeof(request)) {
                        LOGE(TAG, "Bad Request format, size is too large.");
                        continue;
                    }

                    CLIENT_REQUEST_VERSION_TYPE request_version = get_request_version(request);
                    if (request_version != CLIENT_REQUEST_VERSION_1) {
                        LOGE(TAG, "Unsupported client version: %d", request_version);
                        continue;
                    }

                    CLIENT_REQUEST_TYPE_TYPE request_type = get_request_type(request);
                    if (request_type == CLIENT_REQUEST_TYPE_INVALID) {
                        LOGE(TAG, "Invalid client type: %d", request_type);
                        continue;
                    }

                    if (!callJavaOnClientRequireDriver(env, thiz, client_fd, request_type)) {
                        ThrowSHMException("Failed to call java method JNIOnClientRequireDriver.");
                        goto quit;
                    }
                } else {
                    ThrowSHMExceptionWithErrno("Failed to read from client_fd: %d", client_fd);
                    close(client_fd);
                    goto quit;
                }
            }
        }
    }

quit:
    LOGD(TAG, "Epoll loop end.");
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_SHMServer_sendEpollShutDown(JNIEnv *env, jobject thiz, jlong handle) {
    SHMServerData *shmServerData = (SHMServerData *) handle;
    uint64_t value = EPOLL_CTRL_SHUTDOWN;

    if (!shmServerData || shmServerData->epoll_ctrl_fd < 0)
        return;

    if (write(shmServerData->epoll_ctrl_fd, &value, sizeof(value)) != sizeof(value))
        LOGE(TAG, "Failed to send epoll shutdown.");
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_SHMServer_doClean(JNIEnv *env, jobject thiz, jlong handle) {
    SHMServerData *shmServerData = (SHMServerData *) handle;

    if (shmServerData->epoll_fd > 0)
        close(shmServerData->epoll_fd);

    if (shmServerData->epoll_ctrl_fd > 0)
        close(shmServerData->epoll_ctrl_fd);

    if (shmServerData->server_fd > 0)
        close(shmServerData->server_fd);

    if (shmServerData->socketPath[0] != '\0')
        unlink(shmServerData->socketPath);

    free(shmServerData);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_SHMServer_sendSharedMemoryToClient(JNIEnv *env, jobject thiz,
                                                               jint client_fd, jint mem_fd) {
    struct msghdr msg = {0};
    struct iovec iov[1];
    struct cmsghdr *cmsg;
    char buf[CMSG_SPACE(sizeof(mem_fd))];
    char dummy_data = 'A';

    iov[0].iov_base = &dummy_data;
    iov[0].iov_len = sizeof(dummy_data);

    msg.msg_iov = iov;
    msg.msg_iovlen = 1;
    msg.msg_control = buf;
    msg.msg_controllen = sizeof(buf);

    if (!(cmsg = CMSG_FIRSTHDR(&msg)))
        goto throw_errno;

    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    cmsg->cmsg_len = CMSG_LEN(sizeof(mem_fd));

    memcpy(CMSG_DATA(cmsg), &mem_fd, sizeof(mem_fd));

    if (sendmsg(client_fd, &msg, 0) == -1)
        goto throw_errno;

    return;

    throw_errno:
    ThrowSHMExceptionWithErrno("Failed to send mem_fd:%d to client:%d.", mem_fd, client_fd);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_SHMServer_closeClientFd(JNIEnv *env, jobject thiz, jint client_fd) {
    close(client_fd);
}
