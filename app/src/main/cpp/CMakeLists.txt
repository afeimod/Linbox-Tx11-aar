cmake_minimum_required(VERSION 3.22.1)
project(WinFusion)

set(CMAKE_BUILD_TYPE Debug)
set(SCRIPTS_BIN_DIR ${CMAKE_CURRENT_SOURCE_DIR}/scripts/bin)

include(scripts/utils.cmake)
include(scripts/wayland-scanner.cmake)

# 对于 Android NDK 交叉编译，EGL 和 OpenGL ES 是由 NDK 提供的系统库
# CMake 的 FindOpenGL 模块不适用于 Android，需要使用 NDK 的库路径
if(CMAKE_SYSTEM_NAME STREQUAL "Android" AND ANDROID_NDK)
    # Android NDK 环境
    # 获取 NDK 的 sysroot 和 triple 信息
    execute_process(
        COMMAND ${CMAKE_CXX_COMPILER} -print-sysroot
        OUTPUT_VARIABLE ANDROID_NDK_SYSROOT
        OUTPUT_STRIP_TRAILING_WHITESPACE
    )
    
    # 确定 NDK triple
    if(NOT ANDROID_LLVM_TRIPLE)
        if(ANDROID_ABI STREQUAL "arm64-v8a")
            set(ANDROID_LLVM_TRIPLE "aarch64-linux-android")
        elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
            set(ANDROID_LLVM_TRIPLE "arm-linux-androideabi")
        elseif(ANDROID_ABI STREQUAL "x86_64")
            set(ANDROID_LLVM_TRIPLE "x86_64-linux-android")
        elseif(ANDROID_ABI STREQUAL "x86")
            set(ANDROID_LLVM_TRIPLE "i686-linux-android")
        endif()
    endif()
    
    set(EGL_NDK_PATH "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib/${ANDROID_LLVM_TRIPLE}")
    set(EGL_INCLUDE_PATH "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include")
    
    message(STATUS "Android NDK EGL: Using ${EGL_NDK_PATH}/libEGL.so")
    
    # 创建 EGL::EGL 导入目标
    if(NOT TARGET EGL::EGL)
        add_library(EGL::EGL SHARED IMPORTED)
        set_target_properties(EGL::EGL PROPERTIES
            IMPORTED_LOCATION "${EGL_NDK_PATH}/libEGL.so"
            INTERFACE_INCLUDE_DIRECTORIES "${EGL_INCLUDE_PATH}"
        )
        message(STATUS "Created EGL::EGL target for Android")
    endif()
    
    # 创建 'egl' 别名（兼容 weston CMakeLists.txt）
    if(NOT TARGET egl)
        add_library(egl ALIAS EGL::EGL)
        message(STATUS "Created 'egl' alias for EGL::EGL")
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
