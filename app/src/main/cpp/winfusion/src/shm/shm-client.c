#include <shm/shm-client.h>

#define RequestPtr(type, offset) ((type *) (request + offset))

CLIENT_REQUEST_SIZE_TYPE get_request_size(const void *request) {
    if (request)
        return *RequestPtr(CLIENT_REQUEST_SIZE_TYPE, CLIENT_REQUEST_SIZE_OFFSET);
    return CLIENT_REQUEST_SIZE_INVALID;
}

void set_request_size(void* request, CLIENT_REQUEST_SIZE_TYPE size) {
    if (request)
        *RequestPtr(CLIENT_REQUEST_SIZE_TYPE, CLIENT_REQUEST_SIZE_OFFSET) = size;
}

CLIENT_REQUEST_VERSION_TYPE get_request_version(const void* request) {
    if (request)
        return *RequestPtr(CLIENT_REQUEST_VERSION_TYPE, CLIENT_REQUEST_VERSION_OFFSET);
    return CLIENT_REQUEST_VERSION_INVALID;
}

void set_request_version(void* request, CLIENT_REQUEST_VERSION_TYPE version) {
    if (request)
        *RequestPtr(CLIENT_REQUEST_VERSION_TYPE, CLIENT_REQUEST_VERSION_OFFSET) = version;
}

CLIENT_REQUEST_TYPE_TYPE get_request_type(const void *request) {
    if (request)
        return *RequestPtr(CLIENT_REQUEST_TYPE_TYPE, CLIENT_REQUEST_TYPE_OFFSET);
    return CLIENT_REQUEST_TYPE_INVALID;
}

void set_request_type(void* request, CLIENT_REQUEST_TYPE_TYPE type) {
    if (request)
        *RequestPtr(CLIENT_REQUEST_TYPE_TYPE, CLIENT_REQUEST_TYPE_OFFSET) = type;
}
