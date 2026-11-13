/*
 *     Copyright (C) 2019  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#ifndef LIBRETRODROID_LIBRETRODROIDJNI_H
#define LIBRETRODROID_LIBRETRODROIDJNI_H

#include <jni.h>

extern "C" {

JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_reset(JNIEnv* env, jclass obj);
JNIEXPORT jbyteArray JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_serializeState(JNIEnv* env, jclass obj);
JNIEXPORT jboolean JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_unserializeState(JNIEnv* env, jclass obj, jbyteArray data);
JNIEXPORT jbyteArray JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_serializeSRAM(JNIEnv* env, jclass obj);
JNIEXPORT jboolean JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_unserializeSRAM(JNIEnv* env, jclass obj, jbyteArray data);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onSurfaceCreated(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onSurfaceChanged(JNIEnv* env, jclass obj, jint width, jint height);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_pause(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_resume(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_step(JNIEnv* env, jclass obj, jobject glRetroView);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_create(JNIEnv* env, jclass obj, jint GLESVersion, jstring coreFilePath, jstring systemDir, jstring savesDir, jobjectArray variables, jobject shaderConfig, jfloat refreshRate, jboolean preferLowLatencyAudio, jboolean enableVirtualFileSystem, jboolean enableMicrophone, jboolean skipDuplicateFrames, jobject immersiveMode, jstring language);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_loadGameFromPath(JNIEnv* env, jclass obj, jstring gameFilePath);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_loadGameFromBytes(JNIEnv* env, jclass obj, jbyteArray gameFileBytes);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_destroy(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onKeyEvent(JNIEnv* env, jclass obj, jint port, jint action, jint keyCode);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onMotionEvent(JNIEnv* env, jclass obj, jint port, jint source, jfloat xAxis, jfloat yAxis);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_onTouchEvent(JNIEnv* env, jclass obj, jfloat xAxis, jfloat yAxis);
JNIEXPORT jfloat JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_getAspectRatio(JNIEnv* env, jclass obj);
JNIEXPORT jobjectArray JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_getVariables(JNIEnv* env, jclass obj);
JNIEXPORT jobjectArray JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_getControllers(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_setControllerType(JNIEnv* env, jclass obj, jint port, jint type);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_updateVariable(JNIEnv* env, jclass obj, jobject variable);
JNIEXPORT jint JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_availableDisks(JNIEnv* env, jclass obj);
JNIEXPORT jint JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_currentDisk(JNIEnv* env, jclass obj);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_changeDisk(JNIEnv* env, jclass obj, jint index);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_setRumbleEnabled(JNIEnv* env, jclass obj, jboolean enabled);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_setFrameSpeed(JNIEnv* env, jclass obj, jint speed);
JNIEXPORT void JNICALL Java_com_swordfish_libretrodroid_LibretroDroid_setAudioEnabled(JNIEnv* env, jclass obj, jboolean enabled);

}

#endif //LIBRETRODROID_LIBRETRODROIDJNI_H
