#ifndef WINFUSION_LOG_H
#define WINFUSION_LOG_H

#include <jni.h>
#include <android/log.h>

void throwJavaException(JNIEnv *env, const char *className, const char *msg, ...);
void throwJavaExceptionWithErrno(JNIEnv *env, const char *className, const char *msg, ...);

int LOGW(const char *, const char *, ...);
int LOGE(const char *, const char *, ...);
int LOGD(const char *, const char *, ...);

int LOGW_ERRNO(const char *, const char *, ...);
int LOGE_ERRNO(const char *, const char *, ...);
int LOGD_ERRNO(const char *, const char *, ...);

int LOGW_AP(const char *, const char *, va_list);
int LOGE_AP(const char *, const char *, va_list);
int LOGD_AP(const char *, const char *, va_list);

#endif //WINFUSION_LOG_H
