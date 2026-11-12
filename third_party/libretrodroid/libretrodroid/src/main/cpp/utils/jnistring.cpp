/*
 *     Copyright (C) 2021  Filippo Scognamiglio
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

#include "jnistring.h"

JniString::JniString(JNIEnv *env, jstring javaString) :
    env(env), javaString(javaString) {
    nativeString = env->GetStringUTFChars(javaString, 0);
}

JniString::~JniString() {
    env->ReleaseStringUTFChars(javaString, nativeString);
}

const char* JniString::cString() {
    return strdup(nativeString);
}

std::string JniString::stdString() {
    return std::string(nativeString);
}
