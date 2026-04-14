#include <cerrno>
#include <jni.h>
#include <android/log.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>

#include "patchelf.h"
#include "elf.h"

#define TAG "jni_patchelf"
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

#define elfFilePtr elfFileMap[(void*) handle].get()
#define stringWrapper(str, jstr) \
    std::string str;                                                \
    do {                                                            \
        const char *tmp = env->GetStringUTFChars(jstr, nullptr);    \
        str.assign(tmp);                                            \
        env->ReleaseStringUTFChars(jstr, tmp);                      \
    } while (0)
#define exceptionCatcher(code) \
    try code catch (std::runtime_error &e) { internalThrowELFException(env, e); } do { } while (0)

static FileContents internalReadFile(const std::string &);
static bool internalWriteFile(const std::string &, const FileContents &);
static bool internalGetElfType(const FileContents &, ElfType &);
static jlong internalCreateElfFile(bool, FileContents &);
static jobjectArray internalConvertStrSetToStrJArray(JNIEnv *, const std::set<std::string> &);
static void internalThrowELFException(JNIEnv *, const std::runtime_error &);

std::map<void*, std::shared_ptr<BaseElfFile>> elfFileMap;

static FileContents internalReadFile(const std::string &fileName) {
    struct stat st = {0};
    size_t cutOff = std::numeric_limits<size_t>::max();

    if (stat(fileName.c_str(), &st) != 0) {
        LOGE("Failed to getting info about '%s'", fileName.c_str());
        return nullptr;
    }

    if (static_cast<uint64_t>(st.st_size) >
        static_cast<uint64_t>(std::numeric_limits<size_t>::max())) {
        LOGE("Cannot read file of size %ld into memory", st.st_size);
        return nullptr;
    }

    size_t size = std::min(cutOff, static_cast<size_t>(st.st_size));

    FileContents contents = std::make_shared<std::vector<unsigned char>>(size);

    int fd = open(fileName.c_str(), O_RDONLY);
    if (fd == -1) {
        LOGE("Failed to open %s", fileName.c_str());
        return nullptr;
    }

    size_t bytesRead = 0;
    ssize_t portion;
    while ((portion = read(fd, contents->data() + bytesRead, size - bytesRead)) > 0)
        bytesRead += portion;

    close(fd);

    if (bytesRead != size) {
        LOGE("Failed to read %s", fileName.c_str());
    }

    return contents;
}

static bool internalWriteFile(const std::string &fileName, const FileContents &contents) {
    LOGD("Writing %s", fileName.c_str());

    int fd = open(fileName.c_str(), O_CREAT | O_EXCL | O_WRONLY, 0777);
    if (fd == -1) {
        LOGE("Failed to open %s", fileName.c_str());
        return false;
    }

    size_t bytesWritten = 0;
    ssize_t portion;
    while (bytesWritten < contents->size()) {
        if ((portion = write(fd, contents->data() + bytesWritten,
                             contents->size() - bytesWritten)) < 0) {
            int currentErrno = errno;
            if (currentErrno == EINTR)
                continue;
            LOGE("Failed to write %s %s", fileName.c_str(), strerror(currentErrno));
            return false;
        }
        bytesWritten += portion;
    }

    if (close(fd) >= 0)
        return true;

    /*
     * Just ignore EINTR; a retry loop is the wrong thing to do.
     *
     * http://lkml.indiana.edu/hypermail/linux/kernel/0509.1/0877.html
     * https://bugzilla.gnome.org/show_bug.cgi?id=682819
     * http://utcc.utoronto.ca/~cks/space/blog/unix/CloseEINTR
     * https://sites.google.com/site/michaelsafyan/software-engineering/checkforeintrwheninvokingclosethinkagain
     */
    if (errno == EINTR)
        return false;

    return false;
}

static bool internalGetElfType(const FileContents &fileContents, ElfType &elfType) {
    /* Check the ELF header for basic validity. */
    if (fileContents->size() < sizeof(Elf32_Ehdr)) {
        LOGE("Missing ELF header");
        return false;
    }

    auto contents = fileContents->data();

    if (memcmp(contents, ELFMAG, SELFMAG) != 0) {
        LOGE("Not an ELF executable");
        return false;
    }

    if (contents[EI_VERSION] != EV_CURRENT) {
        LOGE("Unsupported ELF version: %d", contents[EI_VERSION]);
        return false;
    }

    if (contents[EI_CLASS] != ELFCLASS32 && contents[EI_CLASS] != ELFCLASS64) {
        LOGE("ELF executable is not 32 or 64 bit");
        return false;
    }

    bool is32Bit = contents[EI_CLASS] == ELFCLASS32;

    elfType.is32Bit = is32Bit;
    elfType.machine = is32Bit ? (reinterpret_cast<Elf32_Ehdr *>(contents))->e_machine :
                      (reinterpret_cast<Elf64_Ehdr *>(contents))->e_machine;

    return true;
}

static jlong internalCreateElfFile(bool is32Bit, FileContents &fileContents) {
    std::shared_ptr<BaseElfFile> ptr;

    if (is32Bit)
        ptr = std::make_shared<Elf32File>(fileContents);
    else
        ptr = std::make_shared<Elf64File>(fileContents);

    elfFileMap.insert({(void*) ptr.get(), ptr});
    return (jlong) ptr.get();
}

static jobjectArray internalConvertStrSetToStrJArray(JNIEnv *env, const std::set<std::string> & set) {
    auto size = static_cast<jsize>(set.size());

    jclass stringClass = env->FindClass("java/lang/String");
    if (!stringClass)
        return nullptr;

    jobjectArray stringArray = env->NewObjectArray(size, stringClass, nullptr);
    if (!stringArray)
        return nullptr;

    jsize index = 0;
    for (const auto& str : set) {
        jstring javaString = env->NewStringUTF(str.c_str());
        if (!javaString)
            return nullptr;

        env->SetObjectArrayElement(stringArray, index++, javaString);
        env->DeleteLocalRef(javaString);
    }

    return stringArray;
}

static void internalThrowELFException(JNIEnv *env, const std::runtime_error &exception) {
    jclass exceptionClass = env->FindClass("com/winfusion/core/elf/exceptions/ELFException");
    if (exceptionClass == nullptr)
        return;

    env->ThrowNew(exceptionClass, exception.what());
    env->DeleteLocalRef(exceptionClass);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_winfusion_core_elf_ElfFile_createElfFile(JNIEnv *env, jobject thiz, jstring path) {
    stringWrapper(str, path);

    exceptionCatcher(
            {
                auto fileContents = internalReadFile(str);

                if (!fileContents)
                    return 0;

                ElfType elfType = {0};
                if (!internalGetElfType(fileContents, elfType))
                    return 0;

                return internalCreateElfFile(elfType.is32Bit, fileContents);
            }
    );

    return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_winfusion_core_elf_ElfFile_writeToFile(JNIEnv *env, jobject thiz, jlong handle,
                                                jstring path) {
    stringWrapper(newPath, path);
    return internalWriteFile(newPath, elfFilePtr->getFileContents());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_winfusion_core_elf_ElfFile_getElfType(JNIEnv *env, jobject thiz, jlong handle) {
    return elfFilePtr->getEType();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_winfusion_core_elf_ElfFile_isChanged(JNIEnv *env, jobject thiz, jlong handle) {
    return elfFilePtr->isChanged();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_setInterpreter(JNIEnv *env, jobject thiz, jlong handle,
                                                   jstring interpreter) {
    stringWrapper(newInterpreter, interpreter);
    exceptionCatcher(
            {
                elfFilePtr->setInterpreter(newInterpreter);
            }
    );
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_winfusion_core_elf_ElfFile_getInterpreter(JNIEnv *env, jobject thiz, jlong handle) {
    exceptionCatcher(
            {
                return env->NewStringUTF(elfFilePtr->getInterpreter().c_str());
            }
    );
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_setSoName(JNIEnv *env, jobject thiz, jlong handle,
                                              jstring soname) {
    stringWrapper(newSoName, soname);
    exceptionCatcher(
            {
                elfFilePtr->setSoName(newSoName);
            }
    );
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_winfusion_core_elf_ElfFile_getSoName(JNIEnv *env, jobject thiz, jlong handle) {
    exceptionCatcher(
            {
                return env->NewStringUTF(elfFilePtr->getSoName().c_str());
            }
    );
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_setRPath(JNIEnv *env, jobject thiz, jlong handle,
                                             jstring rpath) {
    stringWrapper(newRPath, rpath);
    exceptionCatcher(
            {
                elfFilePtr->setRPath(newRPath);
            }
    );
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_winfusion_core_elf_ElfFile_getRPath(JNIEnv *env, jobject thiz, jlong handle) {
    exceptionCatcher(
            {
                return env->NewStringUTF(elfFilePtr->getRPath().c_str());
            }
    );
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_addNeeded(JNIEnv *env, jobject thiz, jlong handle,
                                              jstring needed) {
    stringWrapper(newNeeded, needed);
    exceptionCatcher(
            {
                elfFilePtr->addNeeded(newNeeded);
            }
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_removeNeeded(JNIEnv *env, jobject thiz, jlong handle,
                                                 jstring needed) {
    stringWrapper(rNeeded, needed);
    exceptionCatcher(
            {
                elfFilePtr->removeNeeded(rNeeded);
            }
    );
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_winfusion_core_elf_ElfFile_getNeeded(JNIEnv *env, jobject thiz, jlong handle) {
    std::set<std::string> libs = elfFilePtr->getNeeded();
    exceptionCatcher(
            {
                return internalConvertStrSetToStrJArray(env, libs);
            }
    );
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_winfusion_core_elf_ElfFile_releaseHandle(JNIEnv *env, jobject thiz, jlong handle) {
    elfFileMap.erase((void*) handle);
}
