cmake_minimum_required(VERSION 3.22.1)
project(icoutils)

set(ICOUTILS_PACKAGE "libicoutils")
set(ICOUTILS_VERSION "0.32.3")

set(ICOUTILS_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set(ICOUTILS_SRC_DIR ${ICOUTILS_DIR}/src)

add_compile_options(
        -O2
)

add_library(
        icoutils SHARED
        ${ICOUTILS_SRC_DIR}/common/error.c
        ${ICOUTILS_SRC_DIR}/common/hmap.c
        ${ICOUTILS_SRC_DIR}/common/intutil.c
        ${ICOUTILS_SRC_DIR}/common/io-utils.c
        ${ICOUTILS_SRC_DIR}/common/llist.c
        ${ICOUTILS_SRC_DIR}/common/strbuf.c
        ${ICOUTILS_SRC_DIR}/common/string-utils.c
        ${ICOUTILS_SRC_DIR}/common/tmap.c
        ${ICOUTILS_SRC_DIR}/lib/xalloc-die.c
        ${ICOUTILS_SRC_DIR}/lib/xasprintf.c
        ${ICOUTILS_SRC_DIR}/lib/xmalloc.c
        ${ICOUTILS_SRC_DIR}/lib/xsize.c
        ${ICOUTILS_SRC_DIR}/lib/xvasprintf.c
        ${ICOUTILS_SRC_DIR}/extract.c
        ${ICOUTILS_SRC_DIR}/fileread.c
        ${ICOUTILS_SRC_DIR}/restable.c
        ${ICOUTILS_SRC_DIR}/win32-endian.c
        ${ICOUTILS_SRC_DIR}/jni_icoutils.c
)

target_include_directories(
        icoutils PRIVATE
        ${ICOUTILS_SRC_DIR}
        ${ICOUTILS_SRC_DIR}/lib
)

target_link_libraries(
        icoutils PUBLIC
        log
)
