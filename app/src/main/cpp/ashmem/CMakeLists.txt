cmake_minimum_required(VERSION 3.22.1)
project(ashmem)

set(ASHMEM_PACKAGE "libashmem")
set(ASHMEM_VERSION "9.0.0-r61")

set(ASHMEM_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set(ASHMEM_SRC_DIR ${ASHMEM_DIR}/src)
set(ASHMEM_AUTOGEN_DIR ${ASHMEM_DIR}/autogen)


file(MAKE_DIRECTORY ${ASHMEM_AUTOGEN_DIR})
file(MAKE_DIRECTORY ${ASHMEM_AUTOGEN_DIR}/include)

set(ASHMEM_HEADERS
        ${ASHMEM_SRC_DIR}/ashmem.h
)

foreach (HEADER_FILE IN LISTS ASHMEM_HEADERS)
    configure_file(
            ${HEADER_FILE}
            ${ASHMEM_AUTOGEN_DIR}/include
            COPYONLY
    )
endforeach ()

add_compile_options(
        -O2
)

add_library(
        ashmem SHARED
        ${ASHMEM_SRC_DIR}/ashmem-dev.cpp
)

target_include_directories(
        ashmem PUBLIC
        ${ASHMEM_AUTOGEN_DIR}/include
)

target_link_libraries(
        ashmem PUBLIC
        log
)
