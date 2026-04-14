#ifndef WINFUSION_SHM_CLIENT_H
#define WINFUSION_SHM_CLIENT_H

#define CLIENT_REQUEST_BUFFER_SIZE 16

#define CLIENT_REQUEST_SIZE_TYPE unsigned short
#define CLIENT_REQUEST_SIZE_INVALID 0
#define CLIENT_REQUEST_SIZE_1 5
#define CLIENT_REQUEST_SIZE_OFFSET 0

#define CLIENT_REQUEST_VERSION_TYPE unsigned char
#define CLIENT_REQUEST_VERSION_INVALID 0
#define CLIENT_REQUEST_VERSION_1 1
#define CLIENT_REQUEST_VERSION_OFFSET 2

#define CLIENT_REQUEST_TYPE_TYPE unsigned short
#define CLIENT_REQUEST_TYPE_INVALID 0
#define CLIENT_REQUEST_TYPE_OFFSET 3

CLIENT_REQUEST_SIZE_TYPE get_request_size(const void *request);
void set_request_size(void* request, CLIENT_REQUEST_SIZE_TYPE size);

CLIENT_REQUEST_VERSION_TYPE get_request_version(const void* request);
void set_request_version(void* request, CLIENT_REQUEST_VERSION_TYPE version);

CLIENT_REQUEST_TYPE_TYPE get_request_type(const void *request);
void set_request_type(void* request, CLIENT_REQUEST_TYPE_TYPE type);

#endif //WINFUSION_SHM_CLIENT_H
