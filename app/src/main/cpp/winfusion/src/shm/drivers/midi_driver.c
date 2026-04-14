#include <fluidsynth.h>
#include <ashmem.h>
#include <jni.h>

JNIEXPORT jlong JNICALL
Java_com_winfusion_core_shm_drivers_MIDIDriver_setupMIDISynth(JNIEnv *env, jobject thiz,
                                                              jstring sf_path) {

}

JNIEXPORT jint JNICALL
Java_com_winfusion_core_shm_drivers_MIDIDriver_setupSharedMemory(JNIEnv *env, jobject thiz,
                                                                 jlong handle) {

}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_drivers_MIDIDriver_startDriverLoop(JNIEnv *env, jobject thiz,
                                                               jlong handle) {

}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_drivers_MIDIDriver_stopDriverLoop(JNIEnv *env, jobject thiz,
                                                              jlong handle) {

}

JNIEXPORT void JNICALL
Java_com_winfusion_core_shm_drivers_MIDIDriver_doClean(JNIEnv *env, jobject thiz, jlong handle) {

}