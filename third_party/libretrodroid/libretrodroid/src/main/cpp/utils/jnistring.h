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

#ifndef LIBRETRODROID_JNISTRING_H
#define LIBRETRODROID_JNISTRING_H

#include <jni.h>
#include <string>

class JniString {
public:
    JniString(JNIEnv *env, jstring javaString);
    ~JniString();

    const char* cString();
    std::string stdString();

    JniString(const JniString& other) = delete;
    JniString& operator=(const JniString& other) = delete;

private:
    JNIEnv* env;
    jstring javaString;
    const char* nativeString;
};


#endif //LIBRETRODROID_JNISTRING_H
