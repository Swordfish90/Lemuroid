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

#include "javautils.h"
#include "jnistring.h"

namespace libretrodroid {

jint JavaUtils::throwRetroException(JNIEnv* env, int errorCode) {
    const char *className = "com/swordfish/libretrodroid/RetroException";
    jclass clazz = env->FindClass(className);
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(I)V");
    auto exception = (jthrowable) env->NewObject(clazz, constructor, errorCode);
    return env->Throw(exception);
}

Variable JavaUtils::variableFromJava(JNIEnv *env, jobject obj) {
    jclass variableClass = env->FindClass("com/swordfish/libretrodroid/Variable");

    jfieldID jKeyField = env->GetFieldID(variableClass, "key", "Ljava/lang/String;");
    jfieldID jValueField = env->GetFieldID(variableClass, "value", "Ljava/lang/String;");

    auto key = JniString(env, (jstring) env->GetObjectField(obj, jKeyField));
    auto value = JniString(env, (jstring) env->GetObjectField(obj, jValueField));

    return Variable { key.stdString(), value.stdString() };
}

void JavaUtils::forEachOnJavaIterable(
    JNIEnv *env,
    jobject jList,
    const std::function<void(jobject)> &lambda
) {
    auto jIterableClass = env->GetObjectClass(jList);

    jobject jIterator = env->CallObjectMethod(
        jList,
        env->GetMethodID(jIterableClass, "iterator", "()Ljava/util/Iterator;")
    );
    jclass jIteratorClass = env->GetObjectClass(jIterator);

    jmethodID nextMethodID = env->GetMethodID(jIteratorClass, "next", "()Ljava/lang/Object;");
    jmethodID hasNextMethodID = env->GetMethodID(jIteratorClass, "hasNext", "()Z");

    while (env->CallBooleanMethod(jIterator, hasNextMethodID)) {
        lambda(env->CallObjectMethod(jIterator, nextMethodID));
    }
}

std::unordered_map<std::string, std::string> JavaUtils::stringMapFromJava(JNIEnv *env, jobject jMap) {
    jclass jMapClass = env->GetObjectClass(jMap);

    jmethodID keySetMethodId = env->GetMethodID(jMapClass, "keySet", "()Ljava/util/Set;");
    jmethodID getMethodId = env->GetMethodID (
        jMapClass,
        "get",
        "(Ljava/lang/Object;)Ljava/lang/Object;"
    );

    jobject jKeySet = env->CallObjectMethod(jMap, keySetMethodId);

    std::unordered_map<std::string, std::string> result;

    forEachOnJavaIterable(
        env, jKeySet, [&](jobject jKey) {
            jobject objValue = env->CallObjectMethod(jMap, getMethodId, jKey);

            std::string key = JniString(env, (jstring) jKey).stdString();
            std::string value = JniString(env, (jstring) objValue).stdString();
            result[key] = value;
        }
    );

    return result;
}

ShaderManager::Config JavaUtils::shaderFromJava(JNIEnv *env, jobject obj) {
    jclass jShaderClass = env->FindClass("com/swordfish/libretrodroid/GLRetroShader");

    jfieldID jTypeField = env->GetFieldID(jShaderClass, "type", "I");
    jfieldID jParamsField = env->GetFieldID(jShaderClass, "params", "Ljava/util/Map;");

    jobject jParams = env->GetObjectField(obj, jParamsField);

    int type = env->GetIntField(obj, jTypeField);
    std::unordered_map<std::string, std::string> params = stringMapFromJava(env, jParams);

    return ShaderManager::Config { ShaderManager::Type(type), params };
}

} //namespace libretrodroid
