#include <jni.h>
#include <string>
#include <android/log.h>
#include <malloc.h>

extern "C" {
#include "librtmp/rtmp.h"
}
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"DDDDDD",__VA_ARGS__)
typedef struct {
    int16_t sps_len;
    int16_t pps_len;
    int8_t *sps;
    int8_t *pps;
    RTMP *rtmp;
} Live;
Live *live = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_lecture_rtmtscreenlive_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lecture_rtmtscreenlive_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {

    // 首先 Java 的转成 C 的字符串，不然无法使用
    const char *url = env->GetStringUTFChars(url_, 0);
    int ret;
    do {
        live = (Live *) malloc(sizeof(Live));
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();// Rtmp 申请内存
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 10;// 设置 rtmp 初始化参数，比如超时时间、url
        LOGI("connect %s", url);
        if (!(ret = RTMP_SetupURL(live->rtmp, (char *) url))) break;
        RTMP_EnableWrite(live->rtmp);// 开启 Rtmp 写入
        LOGI("RTMP_Connect");
        if (!(ret = RTMP_Connect(live->rtmp, 0))) break;
        LOGI("RTMP_ConnectStream ");
        if (!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;
        LOGI("connect success");
    } while (0);
    if (!ret && live) {
        free(live);
        live = nullptr;
    }

    env->ReleaseStringUTFChars(url_, url);
    return ret;

}