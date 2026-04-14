cmake_minimum_required(VERSION 3.22.1)
project(patchelf)

set(PATCHELF_PACKAGE "libpatchelf")
set(PATCHELF_VERSION "0.18.0")

set(PATCHELF_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set(PATCHELF_SRC_DIR ${PATCHELF_DIR}/src)
set(PATCHELF_AUTOGEN_DIR ${PATCHELF_DIR}/autogen)

file(MAKE_DIRECTORY ${PATCHELF_AUTOGEN_DIR})
file(MAKE_DIRECTORY ${PATCHELF_AUTOGEN_DIR}/include)

set(PATCHELF_HEADERS
        ${PATCHELF_SRC_DIR}/elf.h
        ${PATCHELF_SRC_DIR}/patchelf.h
        ${PATCHELF_SRC_DIR}/patchelf.tpp
)

foreach (HEADER_FILE IN LISTS PATCHELF_HEADERS)
    configure_file(
            ${HEADER_FILE}
            ${PATCHELF_AUTOGEN_DIR}/include
            COPYONLY
    )
endforeach ()

add_compile_options(
        -O2
        -std=c++17
        -D_FILE_OFFSET_BITS=64
)

add_library(
        patchelf SHARED
        ${PATCHELF_SRC_DIR}/jni_patchelf.cpp
)

target_include_directories(
        patchelf PUBLIC
        ${PATCHELF_AUTOGEN_DIR}/include
)

target_link_libraries(
        patchelf PUBLIC
        log
)
