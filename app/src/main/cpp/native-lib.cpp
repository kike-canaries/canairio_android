#include <string>
#include <jni.h>

extern "C" JNIEXPORT jstring

JNICALL
Java_hpsaturn_pollutionreporter_BaseActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
  std::string hello = "Hello from C++";
  return env->NewStringUTF(hello.c_str());
}
