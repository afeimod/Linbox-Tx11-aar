#ifndef WINFUSION_SHM_SERVER_H
#define WINFUSION_SHM_SERVER_H

#include <sys/un.h>

#define MAX_EPOLL_EVENT 16
#define EPOLL_CTRL_SHUTDOWN 1

typedef struct SHMServerData_s {
    char socketPath[UNIX_PATH_MAX];
    int server_fd;
    int epoll_fd;
    int epoll_ctrl_fd;
} SHMServerData;

#endif //WINFUSION_SHM_SERVER_H
