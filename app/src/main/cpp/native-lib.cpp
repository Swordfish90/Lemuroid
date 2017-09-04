#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_codebutler_odyssey_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

struct struct1 {
    float  f1;
    double d1;
};

extern "C" void testDouble(struct struct1 *s1) {
    memset(s1,0,sizeof(struct1));

    s1->f1 = 42;
    s1->d1 = 32040;
}
