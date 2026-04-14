#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <common/log.h>

#define LOG_BUFFER_SIZE 512

static void internalFormatMsg(char [LOG_BUFFER_SIZE], const char *, va_list);
static void internalFormatMsgWithErrno(char [LOG_BUFFER_SIZE], const char *, va_list);

static void internalThrowJavaException(JNIEnv *, const char *, const char *);

static int internalLogAp(int, const char *, const char *, va_list);
static int internalLogErrno(int, const char *,const char *, va_list);

void throwJavaException(JNIEnv *env, const char *className, const char *msg, ...) {
    char buf[LOG_BUFFER_SIZE];
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    internalFormatMsg(buf, msg, args_cy);
    internalThrowJavaException(env, className, buf);

    va_end(args_cy);
    va_end(args);
}

void throwJavaExceptionWithErrno(JNIEnv *env, const char *className, const char *msg, ...) {
    char buf[LOG_BUFFER_SIZE];
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    internalFormatMsgWithErrno(buf, msg, args);
    internalThrowJavaException(env, className, buf);

    va_end(args_cy);
    va_end(args);
}

int LOGW_AP(const char *tag, const char *msg, va_list args) {
    return internalLogAp(ANDROID_LOG_WARN, tag, msg, args);
}

int LOGE_AP(const char *tag, const char *msg, va_list args) {
    return internalLogAp(ANDROID_LOG_ERROR, tag, msg, args);
}

int LOGD_AP(const char *tag, const char *msg, va_list args) {
    return internalLogAp(ANDROID_LOG_DEBUG, tag, msg, args);
}

int LOGW(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = LOGW_AP(tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

int LOGE(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = LOGE_AP(tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

int LOGD(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = LOGD_AP(tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

int LOGW_ERRNO(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = internalLogErrno(ANDROID_LOG_WARN, tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

int LOGE_ERRNO(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = internalLogErrno(ANDROID_LOG_ERROR, tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

int LOGD_ERRNO(const char *tag, const char * msg, ...) {
    va_list args, args_cy;

    va_start(args, msg);
    va_copy(args_cy, args);

    int ret = internalLogErrno(ANDROID_LOG_DEBUG, tag, msg, args);

    va_end(args_cy);
    va_end(args);
    return ret;
}

static void internalFormatMsg(char dest[LOG_BUFFER_SIZE], const char *msg, va_list args) {
    vsnprintf(dest, LOG_BUFFER_SIZE, msg, args);
}

static void internalFormatMsgWithErrno(char dest[LOG_BUFFER_SIZE], const char * msg, va_list args) {
    int err = errno;
    char buf[2][LOG_BUFFER_SIZE];

    internalFormatMsg(buf[0], msg, args);
    strerror_r(err, buf[1], LOG_BUFFER_SIZE);
    snprintf(dest, LOG_BUFFER_SIZE, "%s (errno:%d %s)", buf[0], err, buf[1]);
}

static void internalThrowJavaException(JNIEnv *env, const char *className, const char *realMsg) {
    jclass exceptionClass = (*env)->FindClass(env, className);

    if (!exceptionClass)
        return;

    (*env)->ThrowNew(env, exceptionClass, realMsg);
    (*env)->DeleteLocalRef(env, exceptionClass);
}

static int internalLogAp(int logLevel, const char *tag, const char *msg, va_list args) {
    char buf[LOG_BUFFER_SIZE];
    internalFormatMsg(buf, msg, args);
    return __android_log_print(logLevel, tag, buf, NULL);
}

static int internalLogErrno(int logLevel, const char *tag, const char *msg, va_list args) {
    char buf[LOG_BUFFER_SIZE];
    internalFormatMsgWithErrno(buf, msg, args);
    return __android_log_print(logLevel, tag, buf, NULL);
}
