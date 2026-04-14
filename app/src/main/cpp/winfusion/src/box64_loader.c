#include <dlfcn.h>
#include <stdio.h>

int main(int argc, const char** argv, char** envp) {
    if (argc < 2) {
        fprintf(stderr, "Bad parameters.");
        return 1;
    }

    void* handle = dlopen(argv[1], RTLD_NOW | RTLD_GLOBAL);
    if (!handle) {
        fprintf(stderr, "dlopen failed: %s\n", dlerror());
        return 1;
    }

    int  (*box64_entry)(int, const char**, char**) = dlsym(handle, "__box64_main_entry__");
    if (!box64_entry) {
        fprintf(stderr, "dlsym failed: %s\n", dlerror());
        return 1;
    }

    int ret = box64_entry(argc - 1, argv + 1, envp);
    dlclose(handle);

    return ret;
}
