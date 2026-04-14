/* jni_icoutils.c - Jni wrapper for wrestool
 *
 * Copyright (C) 1998 Oskar Liljeblad
 * Copyright (C) 2025 Junyu Long
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <errno.h>
#include <jni.h>
#include <android/log.h>

#include "common/error.h"
#include "common/intutil.h"
#include "common/io-utils.h"
#include "common/string-utils.h"
#include "wrestool.h"
#include "gettext.h"    /* Gnulib */

#define _(s) gettext(s)
#define N_(s) gettext_noop(s)

static void storeBuffer(JNIEnv *env, jclass clazz, jobject list, jobject buffer);

void extract_icon_resource_callback(WinLibrary *fi, WinResource *wr,
                                    WinResource *type_wr, WinResource *name_wr,
                                    WinResource *lang_wr, const void *data) {
    bool free_it;
    size_t size;
    void *memory;

    memory = extract_resource(
            fi,
            wr,
            &size,
            &free_it, type_wr->id,
            (lang_wr == NULL ? NULL : lang_wr->id),
            false
    );

    if (memory && data) {
        MethodParams *params = (MethodParams *) data;
        jobject buffer;
        buffer = (*params->env)->NewDirectByteBuffer(params->env, memory, (jlong) size);

        if (!buffer)
            free(memory);

        storeBuffer(params->env, params->clazz, params->list, buffer);
    }
}

static void storeBuffer(JNIEnv *env, jclass clazz, jobject list, jobject buffer) {
    jmethodID methodId = (*env)->GetStaticMethodID(env, clazz, "JNIAddByteBufferToList",
                                                   "(Ljava/nio/ByteBuffer;Ljava/util/List;)V");
    if (!methodId) {
        warn(_("JNIAddByteBufferToList method not found."));
        return;
    }

    (*env)->CallStaticVoidMethod(env, clazz, methodId, buffer, list);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_pe_IconParser_extractIconFromPE(JNIEnv *env, jclass clazz, jstring path,
                                                        jobject buffers, jboolean logo_only) {
    WinLibrary fi;
    const char *cPath = (*env)->GetStringUTFChars(env, path, NULL);
    MethodParams params = {};

    /* initiate stuff */
    fi.file = NULL;
    fi.memory = NULL;

    /* get file size */
    fi.name = cPath;
    fi.total_size = file_size(fi.name);
    if (fi.total_size == -1) {
        warn_errno("%s", fi.name);
        goto cleanup;
    }
    if (fi.total_size == 0) {
        warn(_("%s: file has a size of 0"), fi.name);
        goto cleanup;
    }

    /* open file */
    fi.file = fopen(fi.name, "rb");
    if (fi.file == NULL) {
        warn_errno("%s", fi.name);
        goto cleanup;
    }

    /* read all of file */
    fi.memory = malloc(fi.total_size);
    if (fread(fi.memory, fi.total_size, 1, fi.file) != 1) {
        warn_errno("%s", fi.name);
        goto cleanup;
    }

    /* identify file and find resource table */
    if (!read_library(&fi)) {
        /* error reported by read_library */
        goto cleanup;
    }

    /* create method params */
    params.env = env;
    params.clazz = clazz;
    params.list = buffers;
    params.logoOnly = logo_only;

    /* do the extract icon command */
    do_resources(&fi, "14", NULL, NULL, extract_icon_resource_callback, &params);

    /* free stuff and close file */
    cleanup:
    if (fi.file != NULL)
        fclose(fi.file);
    if (fi.memory != NULL)
        free(fi.memory);
    (*env)->ReleaseStringUTFChars(env, path, cPath);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_pe_IconParser_releaseMemory(JNIEnv *env, jclass clazz, jobject buffer) {
    void *nativeBuffer = (*env)->GetDirectBufferAddress(env, buffer);
    if (nativeBuffer != NULL)
        free(nativeBuffer);
}
