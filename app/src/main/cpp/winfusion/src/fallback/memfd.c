#include <sys/mman.h>

#if __ANDROID_API__ < 30
#include <unistd.h>
#include <sys/syscall.h>
int memfd_create(const char* _Nonnull __name, unsigned __flags) {
    return (int) syscall(__NR_memfd_create, __name, __flags);
}
#endif
