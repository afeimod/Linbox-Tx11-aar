cmake_minimum_required(VERSION 3.22.1)
project(WinFusion)

set(CMAKE_BUILD_TYPE Debug)
set(SCRIPTS_BIN_DIR ${CMAKE_CURRENT_SOURCE_DIR}/scripts/bin)

include(scripts/utils.cmake)
include(scripts/wayland-scanner.cmake)

# 对于 Android NDK，EGL 和 OpenGL ES 是由系统提供的库
# 需要显式找到它们以便 CMake 能够正确链接
if(CMAKE_SYSTEM_NAME STREQUAL "Android" AND ANDROID_NDK)
    # 设置 NDK sysroot 路径
    set(ANDROID_NDK_SYSROOT "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot")
    
    # 设置库搜索路径
    if(ANDROID_LLVM_TRIPLE)
        set(_EGL_LIB_PATH "${ANDROID_NDK_SYSROOT}/usr/lib/${ANDROID_LLVM_TRIPLE}")
    else()
        set(_EGL_LIB_PATH "${ANDROID_NDK_SYSROOT}/usr/lib")
    endif()
    
    # 找到 EGL 库
    find_library(EGL_LIBRARY EGL PATHS "${_EGL_LIB_PATH}" NO_DEFAULT_PATH)
    if(EGL_LIBRARY)
        message(STATUS "Found EGL library: ${EGL_LIBRARY}")
        if(NOT TARGET EGL::EGL)
            add_library(EGL::EGL SHARED IMPORTED)
            set_target_properties(EGL::EGL PROPERTIES
                IMPORTED_LOCATION "${EGL_LIBRARY}"
                INTERFACE_INCLUDE_DIRECTORIES "${ANDROID_NDK_SYSROOT}/usr/include"
            )
        endif()
    endif()
    
    # 找到 GLESv2 库
    find_library(GLESV2_LIBRARY GLESv2 PATHS "${_EGL_LIB_PATH}" NO_DEFAULT_PATH)
    if(GLESV2_LIBRARY)
        message(STATUS "Found GLESv2 library: ${GLESV2_LIBRARY}")
        if(NOT TARGET GLESv2::GLESv2)
            add_library(GLESv2::GLESv2 SHARED IMPORTED)
            set_target_properties(GLESv2::GLESv2 PROPERTIES
                IMPORTED_LOCATION "${GLESV2_LIBRARY}"
                INTERFACE_INCLUDE_DIRECTORIES "${ANDROID_NDK_SYSROOT}/usr/include"
            )
        endif()
    endif()
    
    # 找到 GLESv3 库
    find_library(GLESV3_LIBRARY GLESv3 PATHS "${_EGL_LIB_PATH}" NO_DEFAULT_PATH)
    if(GLESV3_LIBRARY)
        message(STATUS "Found GLESv3 library: ${GLESV3_LIBRARY}")
        if(NOT TARGET GLESv3::GLESv3)
            add_library(GLESv3::GLESv3 SHARED IMPORTED)
            set_target_properties(GLESv3::GLESv3 PROPERTIES
                IMPORTED_LOCATION "${GLESV3_LIBRARY}"
                INTERFACE_INCLUDE_DIRECTORIES "${ANDROID_NDK_SYSROOT}/usr/include"
            )
        endif()
    endif()
endif()

add_subdirectory(pcre2)
add_subdirectory(glib)
add_subdirectory(png)
add_subdirectory(ashmem)
add_subdirectory(fluidsynth)
add_subdirectory(icoutils)
add_subdirectory(patchelf)
add_subdirectory(cairo)
add_subdirectory(ffi)
add_subdirectory(pixman)
add_subdirectory(xkbcommon)
add_subdirectory(wayland-protocols)
add_subdirectory(wayland)
add_subdirectory(weston)
add_subdirectory(winfusion)
