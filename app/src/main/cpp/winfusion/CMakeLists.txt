cmake_minimum_required(VERSION 3.22.1)
project(winfusion)

set(WINFUSION_PACKAGE "libwinfusion")

set(WINFUSION_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set(WINFUSION_SRC_DIR ${WINFUSION_DIR}/src)
set(WINFUSION_INCLUDE_DIR ${WINFUSION_DIR}/include)

add_compile_options(
        -O2
        -D__WINFUSION__
)

# memfd-fallback
add_library(
        memfd-fallback STATIC
        ${WINFUSION_SRC_DIR}/fallback/memfd.c
)

target_include_directories(
        memfd-fallback PUBLIC
        ${WINFUSION_INCLUDE_DIR}/fallback/memfd
)

# box64-loader
add_executable(
        box64-loader
        ${WINFUSION_SRC_DIR}/box64_loader.c
)

set_target_properties(box64-loader PROPERTIES OUTPUT_NAME "libbox64-loader.so")

# dlopen-preload
add_library(
        dlopen-preload SHARED
        ${WINFUSION_SRC_DIR}/dlopen_preload.c
)

# common-log
add_library(
        common-log SHARED
        ${WINFUSION_SRC_DIR}/common/log.c
)

target_include_directories(
        common-log PRIVATE
        ${WINFUSION_INCLUDE_DIR}
)

target_link_libraries(
        common-log PRIVATE
        log
)

# common-list
add_library(
        common-list SHARED
        ${WINFUSION_SRC_DIR}/common/list.c
)

target_include_directories(
        common-list PRIVATE
        ${WINFUSION_INCLUDE_DIR}
)

# egl-shim
add_library(
        egl-shim SHARED
        ${WINFUSION_SRC_DIR}/egl-shim/egldisplay.c
        ${WINFUSION_SRC_DIR}/egl-shim/eglapi.c
)

target_include_directories(
        egl-shim PRIVATE
        ${WINFUSION_INCLUDE_DIR}
)

target_link_libraries(
        egl-shim PRIVATE
        wayland-egl
        common-list
)

# winfusion
add_library(
        winfusion SHARED
        ${WINFUSION_SRC_DIR}/shm/shm-server.c
        ${WINFUSION_SRC_DIR}/shm/shm-client.c
        ${WINFUSION_SRC_DIR}/shm/drivers/midi_driver.c
        ${WINFUSION_SRC_DIR}/wayland/weston.c
        ${WINFUSION_SRC_DIR}/wayland/weston-config.c
        ${WINFUSION_SRC_DIR}/wayland/weston-input.c
)

target_include_directories(
        winfusion PRIVATE
        ${WINFUSION_INCLUDE_DIR}
)

target_link_libraries(
        winfusion PRIVATE
        common-log
        ashmem
        android
        fluidsynth
        weston
        pixman
)
