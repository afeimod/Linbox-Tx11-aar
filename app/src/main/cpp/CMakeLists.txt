cmake_minimum_required(VERSION 3.22.1)
project(WinFusion)

set(CMAKE_BUILD_TYPE Debug)
set(SCRIPTS_BIN_DIR ${CMAKE_CURRENT_SOURCE_DIR}/scripts/bin)

include(scripts/utils.cmake)
include(scripts/wayland-scanner.cmake)

# 查找 EGL 库 (在 Android NDK 中包含在 OpenGL 包中)
find_package(OpenGL REQUIRED)
if(OpenGL_EGL_FOUND)
    # 创建 EGL 导入目标以便与 weston 的 CMakeLists.txt 兼容
    if(NOT TARGET EGL::EGL)
        add_library(EGL::EGL SHARED IMPORTED)
        set_target_properties(EGL::EGL PROPERTIES
            IMPORTED_LOCATION ${OpenGL_EGL_LIBRARY}
            INTERFACE_INCLUDE_DIRECTORIES ${OpenGL_INCLUDE_DIR}
        )
        message(STATUS "Found EGL library: ${OpenGL_EGL_LIBRARY}")
    endif()
    # 同时创建别名目标 'egl' 指向 'EGL::EGL'
    # 这样 weston CMakeLists.txt 中的 target_link_libraries(... egl) 也能正常工作
    if(NOT TARGET egl)
        add_library(egl ALIAS EGL::EGL)
        message(STATUS "Created 'egl' alias for 'EGL::EGL'")
    endif()
else()
    # 如果 OpenGL_EGL_FOUND 为 FALSE，尝试直接查找 EGL 库
    find_library(EGL_LIBRARY NAMES EGL)
    if(EGL_LIBRARY)
        message(STATUS "Found EGL library directly: ${EGL_LIBRARY}")
        if(NOT TARGET EGL::EGL)
            add_library(EGL::EGL SHARED IMPORTED)
            set_target_properties(EGL::EGL PROPERTIES
                IMPORTED_LOCATION ${EGL_LIBRARY}
            )
        endif()
        if(NOT TARGET egl)
            add_library(egl ALIAS EGL::EGL)
        endif()
    else()
        message(FATAL_ERROR "Could not find EGL library")
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
