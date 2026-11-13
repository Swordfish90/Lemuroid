/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define MODULE_NAME "OboeTester"

#include <cassert>
#include <cstring>
#include <jni.h>
#include <stdint.h>
#include <sys/sysinfo.h>
#include <thread>

#include "common/AdpfWrapper.h"
#include "common/OboeDebug.h"
#include "oboe/Oboe.h"

#include "NativeAudioContext.h"
#include "TestColdStartLatency.h"
#include "TestErrorCallback.h"
#include "TestRoutingCrash.h"
#include "TestRapidCycle.h"

static NativeAudioContext engine;

/*********************************************************************************/
/**********************  JNI  Prototypes *****************************************/
/*********************************************************************************/
extern "C" {

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_openNative(JNIEnv *env, jobject,
                                                       jint nativeApi,
                                                       jint sampleRate,
                                                       jint channelCount,
                                                       jint channelMask,
                                                       jint format,
                                                       jint sharingMode,
                                                       jint performanceMode,
                                                       jint inputPreset,
                                                       jint usage,
                                                       jint contentType,
                                                       jint bufferCapacityInFrames,
                                                       jint deviceId,
                                                       jint sessionId,
                                                       jboolean channelConversionAllowed,
                                                       jboolean formatConversionAllowed,
                                                       jint rateConversionQuality,
                                                       jboolean isMMap,
                                                       jboolean isInput);
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_close(JNIEnv *env, jobject, jint);

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setThresholdInFrames(JNIEnv *env, jobject, jint, jint);
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getThresholdInFrames(JNIEnv *env, jobject, jint);
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getBufferCapacityInFrames(JNIEnv *env, jobject, jint);
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setNativeApi(JNIEnv *env, jobject, jint, jint);

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setUseCallback(JNIEnv *env, jclass type,
                                                                      jboolean useCallback);
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setCallbackReturnStop(JNIEnv *env,
                                                                             jclass type,
                                                                             jboolean b);
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setCallbackSize(JNIEnv *env, jclass type,
                                                            jint callbackSize);

// ================= OboeAudioOutputStream ================================

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_trigger(JNIEnv *env, jobject);
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_setToneType(JNIEnv *env, jobject, jint);
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_setAmplitude(JNIEnv *env, jobject, jfloat);

/*********************************************************************************/
/**********************  JNI Implementations *************************************/
/*********************************************************************************/

JNIEXPORT jboolean JNICALL
Java_com_mobileer_oboetester_NativeEngine_isMMapSupported(JNIEnv *env, jclass type) {
    return oboe::AAudioExtensions::getInstance().isMMapSupported();
}

JNIEXPORT jboolean JNICALL
Java_com_mobileer_oboetester_NativeEngine_isMMapExclusiveSupported(JNIEnv *env, jclass type) {
    return oboe::AAudioExtensions::getInstance().isMMapExclusiveSupported();
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_NativeEngine_setWorkaroundsEnabled(JNIEnv *env, jclass type,
                                                                          jboolean enabled) {
    oboe::OboeGlobals::setWorkaroundsEnabled(enabled);
}

JNIEXPORT jboolean JNICALL
Java_com_mobileer_oboetester_NativeEngine_areWorkaroundsEnabled(JNIEnv *env,
        jclass type) {
    return oboe::OboeGlobals::areWorkaroundsEnabled();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_NativeEngine_getCpuCount(JNIEnv *env, jclass type) {
    return sysconf(_SC_NPROCESSORS_CONF);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_NativeEngine_setCpuAffinityMask(JNIEnv *env,
                                                                     jclass type,
                                                                     jint mask) {
    engine.getCurrentActivity()->setCpuAffinityMask(mask);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_NativeEngine_setWorkloadReportingEnabled(JNIEnv *env,
                                                             jclass type,
                                                             jboolean enabled) {
    engine.getCurrentActivity()->setWorkloadReportingEnabled(enabled);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_openNative(
        JNIEnv *env, jobject synth,
        jint nativeApi,
        jint sampleRate,
        jint channelCount,
        jint channelMask,
        jint format,
        jint sharingMode,
        jint performanceMode,
        jint inputPreset,
        jint usage,
        jint contentType,
        jint bufferCapacityInFrames,
        jint deviceId,
        jint sessionId,
        jboolean channelConversionAllowed,
        jboolean formatConversionAllowed,
        jint rateConversionQuality,
        jboolean isMMap,
        jboolean isInput) {
    LOGD("OboeAudioStream_openNative: sampleRate = %d", sampleRate);

    return (jint) engine.getCurrentActivity()->open(nativeApi,
                                                    sampleRate,
                                                    channelCount,
                                                    channelMask,
                                                    format,
                                                    sharingMode,
                                                    performanceMode,
                                                    inputPreset,
                                                    usage,
                                                    contentType,
                                                    bufferCapacityInFrames,
                                                    deviceId,
                                                    sessionId,
                                                    channelConversionAllowed,
                                                    formatConversionAllowed,
                                                    rateConversionQuality,
                                                    isMMap,
                                                    isInput);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_startNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->start();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_pauseNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->pause();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_flushNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->flush();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_stopNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->stop();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_releaseNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->release();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_getFramesPerCallback(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->getFramesPerCallback();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_startPlaybackNative(JNIEnv *env, jobject) {
    return (jint) engine.getCurrentActivity()->startPlayback();
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_close(JNIEnv *env, jobject, jint streamIndex) {
    engine.getCurrentActivity()->close(streamIndex);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_setUseAlternativeAdpf(JNIEnv *env, jobject, jboolean enabled) {
    AdpfWrapper::setUseAlternative(enabled);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setBufferSizeInFrames(
        JNIEnv *env, jobject, jint streamIndex, jint threshold) {
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        auto result = oboeStream->setBufferSizeInFrames(threshold);
        return (!result)
               ? (jint) result.error()
               : (jint) result.value();
    }
    return (jint) oboe::Result::ErrorNull;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getBufferSizeInFrames(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getBufferSizeInFrames();
    }
    return result;
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setPerformanceHintEnabled(
        JNIEnv *env, jobject, jint streamIndex, jboolean enabled) {
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        oboeStream->setPerformanceHintEnabled(enabled);
    }
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getBufferCapacityInFrames(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getBufferCapacityInFrames();
    }
    return result;
}

static int convertAudioApiToNativeApi(oboe::AudioApi audioApi) {
    switch(audioApi) {
        case oboe::AudioApi::Unspecified:
            return NATIVE_MODE_UNSPECIFIED;
        case oboe::AudioApi::OpenSLES:
            return NATIVE_MODE_OPENSLES;
        case oboe::AudioApi::AAudio:
            return NATIVE_MODE_AAUDIO;
        default:
            return -1;
    }
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getNativeApi(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        oboe::AudioApi audioApi = oboeStream->getAudioApi();
        result = convertAudioApiToNativeApi(audioApi);
        LOGD("OboeAudioStream_getNativeApi got %d", result);
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getSampleRate(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getSampleRate();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getSharingMode(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getSharingMode();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getPerformanceMode(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getPerformanceMode();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getInputPreset(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getInputPreset();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getFramesPerBurst(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getFramesPerBurst();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getChannelCount(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getChannelCount();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getChannelMask(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getChannelMask();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getFormat(JNIEnv *env, jobject instance, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getFormat();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getHardwareChannelCount(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getHardwareChannelCount();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getHardwareFormat(JNIEnv *env, jobject instance, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getHardwareFormat();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getHardwareSampleRate(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getHardwareSampleRate();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getUsage(JNIEnv *env, jobject instance, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getUsage();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getContentType(JNIEnv *env, jobject instance, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = (jint) oboeStream->getContentType();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getDeviceId(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getDeviceId();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getSessionId(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getSessionId();
    }
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getFramesWritten(
        JNIEnv *env, jobject, jint streamIndex) {
    jlong result = (jint) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getFramesWritten();
    }
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getFramesRead(
        JNIEnv *env, jobject, jint streamIndex) {
    jlong result = (jlong) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        result = oboeStream->getFramesRead();
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getXRunCount(
        JNIEnv *env, jobject, jint streamIndex) {
    jint result = (jlong) oboe::Result::ErrorNull;
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        auto oboeResult  = oboeStream->getXRunCount();
        if (!oboeResult) {
            result = (jint) oboeResult.error();
        } else {
            result = oboeResult.value();
        }
    }
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getCallbackCount(
        JNIEnv *env, jobject) {
    return engine.getCurrentActivity()->getCallbackCount();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getLastErrorCallbackResult(
        JNIEnv *env, jobject, jint streamIndex) {
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        return (jint) oboeStream->getLastErrorCallbackResult();
    }
    return 0;
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getTimestampLatency(JNIEnv *env,
        jobject instance,
        jint streamIndex) {
    return engine.getCurrentActivity()->getTimestampLatency(streamIndex);
}

JNIEXPORT jfloat JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getCpuLoad(JNIEnv *env, jobject instance, jint streamIndex) {
    return engine.getCurrentActivity()->getCpuLoad();
}

JNIEXPORT jfloat JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getAndResetMaxCpuLoad(JNIEnv *env, jobject instance, jint streamIndex) {
    return engine.getCurrentActivity()->getAndResetMaxCpuLoad();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getAndResetCpuMask(JNIEnv *env, jobject instance, jint streamIndex) {
    return (jint) engine.getCurrentActivity()->getAndResetCpuMask();
}

JNIEXPORT jstring JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getCallbackTimeString(JNIEnv *env, jobject instance) {
    return env->NewStringUTF(engine.getCurrentActivity()->getCallbackTimeString().c_str());
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setWorkload(
        JNIEnv *env, jobject, jint workload) {
    engine.getCurrentActivity()->setWorkload(workload);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setHearWorkload(
        JNIEnv *env, jobject, jint streamIndex, jboolean enabled) {
    engine.getCurrentActivity()->setHearWorkload(enabled);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getState(JNIEnv *env, jobject instance, jint streamIndex) {
    std::shared_ptr<oboe::AudioStream> oboeStream = engine.getCurrentActivity()->getStream(streamIndex);
    if (oboeStream != nullptr) {
        auto state = oboeStream->getState();
        if (state != oboe::StreamState::Starting && state != oboe::StreamState::Started
                && state != oboe::StreamState::Disconnected) {
            oboe::Result result = oboeStream->waitForStateChange(
                    oboe::StreamState::Uninitialized,
                    &state, 0);

            if (result != oboe::Result::OK){
                if (result == oboe::Result::ErrorClosed) {
                    state = oboe::StreamState::Closed;
                } else if (result == oboe::Result::ErrorDisconnected){
                    state = oboe::StreamState::Disconnected;
                } else {
                    state = oboe::StreamState::Unknown;
                }
            }
        }
        return (jint) state;
    }
    return -1;
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_AudioInputTester_getPeakLevel(JNIEnv *env,
                                                          jobject instance,
                                                          jint index) {
    return engine.getCurrentActivity()->getPeakLevel(index);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setUseCallback(JNIEnv *env, jclass type,
                                                                      jboolean useCallback) {
    ActivityContext::mUseCallback = useCallback;
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setCallbackReturnStop(JNIEnv *env, jclass type,
                                                                      jboolean b) {
    OboeStreamCallbackProxy::setCallbackReturnStop(b);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setHangTimeMillis(JNIEnv *env, jclass type,
                                                                   jint hangTimeMillis) {
    OboeTesterStreamCallback::setHangTimeMillis(hangTimeMillis);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_setCallbackSize(JNIEnv *env, jclass type,
                                                            jint callbackSize) {
    ActivityContext::callbackSize = callbackSize;
}

JNIEXPORT jboolean JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_isMMap(JNIEnv *env, jobject instance, jint streamIndex) {
    return engine.getCurrentActivity()->isMMapUsed(streamIndex);
}

// ================= OboeAudioOutputStream ================================

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_trigger(
        JNIEnv *env, jobject) {
    engine.getCurrentActivity()->trigger();
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_setChannelEnabled(
        JNIEnv *env, jobject, jint channelIndex, jboolean enabled) {
    engine.getCurrentActivity()->setChannelEnabled(channelIndex, enabled);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_setSignalType(
        JNIEnv *env, jobject, jint signalType) {
    engine.getCurrentActivity()->setSignalType(signalType);
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_OboeAudioOutputStream_setAmplitude(JNIEnv *env, jobject, jfloat amplitude) {
    engine.getCurrentActivity()->setAmplitude(amplitude);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_OboeAudioStream_getOboeVersionNumber(JNIEnv *env,
                                                                          jclass type) {
    return OBOE_VERSION_NUMBER;
}

// ==========================================================================
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_setActivityType(JNIEnv *env,
                                                                         jobject instance,
                                                                         jint activityType) {
    engine.setActivityType(activityType);
}

// ==========================================================================
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestInputActivity_saveWaveFile(JNIEnv *env,
                                                                        jobject instance,
                                                                        jstring fileName) {
    const char *str = env->GetStringUTFChars(fileName, nullptr);
    LOGD("nativeSaveFile(%s)", str);
    jint result = engine.getCurrentActivity()->saveWaveFile(str);
    env->ReleaseStringUTFChars(fileName, str);
    return result;
}

// ==========================================================================
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_TestInputActivity_setMinimumFramesBeforeRead(JNIEnv *env,
                                                                      jobject instance,
                                                                      jint numFrames) {
    engine.getCurrentActivity()->setMinimumFramesBeforeRead(numFrames);
}

// ==========================================================================
JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_EchoActivity_setDelayTime(JNIEnv *env,
                                                                         jobject instance,
                                                                         jdouble delayTimeSeconds) {
    engine.setDelayTime(delayTimeSeconds);
}

JNIEXPORT int JNICALL
Java_com_mobileer_oboetester_EchoActivity_getColdStartInputMillis(JNIEnv *env,
        jobject instance) {
    return engine.getCurrentActivity()->getColdStartInputMillis();
}

JNIEXPORT int JNICALL
Java_com_mobileer_oboetester_EchoActivity_getColdStartOutputMillis(JNIEnv *env,
                                                                            jobject instance) {
    return engine.getCurrentActivity()->getColdStartOutputMillis();
}

// ==========================================================================
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getAnalyzerProgress(JNIEnv *env,
                                                                                    jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getProgress();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getMeasuredLatency(JNIEnv *env,
                                                                                   jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getMeasuredLatency();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getMeasuredConfidence(JNIEnv *env,
                                                                            jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getMeasuredConfidence();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getMeasuredCorrelation(JNIEnv *env,
                                                                            jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getMeasuredCorrelation();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_measureTimestampLatency(JNIEnv *env,
                                                                            jobject instance) {
    return engine.mActivityRoundTripLatency.measureTimestampLatency();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getBackgroundRMS(JNIEnv *env,
                                                                                 jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getBackgroundRMS();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_RoundTripLatencyActivity_getSignalRMS(JNIEnv *env,
                                                                                 jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getSignalRMS();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_AnalyzerActivity_getMeasuredResult(JNIEnv *env,
                                                                          jobject instance) {
    return engine.mActivityRoundTripLatency.getLatencyAnalyzer()->getResult();
}

// ==========================================================================
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_AnalyzerActivity_getAnalyzerState(JNIEnv *env,
                                                                         jobject instance) {
    return ((ActivityFullDuplex *)engine.getCurrentActivity())->getState();
}

JNIEXPORT jboolean JNICALL
Java_com_mobileer_oboetester_AnalyzerActivity_isAnalyzerDone(JNIEnv *env,
                                                                       jobject instance) {
    return ((ActivityFullDuplex *)engine.getCurrentActivity())->isAnalyzerDone();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_AnalyzerActivity_getResetCount(JNIEnv *env,
                                                                          jobject instance) {
    return ((ActivityFullDuplex *)engine.getCurrentActivity())->getResetCount();
}

// ==========================================================================
JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getGlitchCount(JNIEnv *env,
                                                           jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getGlitchCount();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getGlitchLength(JNIEnv *env,
                                                           jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getGlitchLength();
}

JNIEXPORT double JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getPhase(JNIEnv *env,
                                                           jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getPhaseOffset();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getStateFrameCount(JNIEnv *env,
                                                                     jobject instance,
                                                                     jint state) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getStateFrameCount(state);
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getSignalToNoiseDB(JNIEnv *env,
                                                                         jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getSignalToNoiseDB();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getPeakAmplitude(JNIEnv *env,
                                                                       jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getPeakAmplitude();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getSineAmplitude(JNIEnv *env,
                                                             jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getSineAmplitude();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_GlitchActivity_getSinePeriod(JNIEnv *env,
                                                             jobject instance) {
    return engine.mActivityGlitches.getGlitchAnalyzer()->getSinePeriod();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_TestDataPathsActivity_getMagnitude(JNIEnv *env,
                                                                          jobject instance) {
    return engine.mActivityDataPath.getDataPathAnalyzer()->getMagnitude();
}

JNIEXPORT jdouble JNICALL
Java_com_mobileer_oboetester_TestDataPathsActivity_getMaxMagnitude(JNIEnv *env,
                                                                          jobject instance) {
    return engine.mActivityDataPath.getDataPathAnalyzer()->getMaxMagnitude();
}

JNIEXPORT double JNICALL
Java_com_mobileer_oboetester_TestDataPathsActivity_getPhaseDataPaths(JNIEnv *env,
                                                     jobject instance) {
    return engine.mActivityDataPath.getDataPathAnalyzer()->getPhaseOffset();
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_GlitchActivity_setTolerance(JNIEnv *env,
                                                                   jobject instance,
                                                                   jfloat tolerance) {
    if (engine.mActivityGlitches.getGlitchAnalyzer()) {
        engine.mActivityGlitches.getGlitchAnalyzer()->setTolerance(tolerance);
    }
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_GlitchActivity_setForcedGlitchDuration(JNIEnv *env,
                                                                  jobject instance,
                                                                  jint frames) {
    if (engine.mActivityGlitches.getGlitchAnalyzer()) {
        engine.mActivityGlitches.getGlitchAnalyzer()->setForcedGlitchDuration(frames);
    }
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_GlitchActivity_setInputChannelNative(JNIEnv *env,
                                                                   jobject instance,
                                                                   jint channel) {
    if (engine.mActivityGlitches.getGlitchAnalyzer()) {
        engine.mActivityGlitches.getGlitchAnalyzer()->setInputChannel(channel);
    }
    if (engine.mActivityDataPath.getDataPathAnalyzer()) {
        engine.mActivityDataPath.getDataPathAnalyzer()->setInputChannel(channel);
    }
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_GlitchActivity_setOutputChannelNative(JNIEnv *env,
                                                                       jobject instance,
                                                                       jint channel) {
    if (engine.mActivityGlitches.getGlitchAnalyzer()) {
        engine.mActivityGlitches.getGlitchAnalyzer()->setOutputChannel(channel);
    }
    if (engine.mActivityDataPath.getDataPathAnalyzer()) {
        engine.mActivityDataPath.getDataPathAnalyzer()->setOutputChannel(channel);
    }
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_ManualGlitchActivity_getGlitch(JNIEnv *env, jobject instance,
                                                                      jfloatArray waveform_) {
    float *waveform = env->GetFloatArrayElements(waveform_, nullptr);
    jsize length = env->GetArrayLength(waveform_);
    jsize numSamples = 0;
    auto *analyzer = engine.mActivityGlitches.getGlitchAnalyzer();
    if (analyzer) {
        numSamples = analyzer->getLastGlitch(waveform, length);
    }

    env->ReleaseFloatArrayElements(waveform_, waveform, 0);
    return numSamples;
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_ManualGlitchActivity_getRecentSamples(JNIEnv *env, jobject instance,
                                                            jfloatArray waveform_) {
    float *waveform = env->GetFloatArrayElements(waveform_, nullptr);
    jsize length = env->GetArrayLength(waveform_);
    jsize numSamples = 0;
    auto *analyzer = engine.mActivityGlitches.getGlitchAnalyzer();
    if (analyzer) {
        numSamples = analyzer->getRecentSamples(waveform, length);
    }

    env->ReleaseFloatArrayElements(waveform_, waveform, 0);
    return numSamples;
}

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_TestAudioActivity_setDefaultAudioValues(JNIEnv *env, jclass clazz,
                                                                     jint audio_manager_sample_rate,
                                                                     jint audio_manager_frames_per_burst) {
    oboe::DefaultStreamValues::SampleRate = audio_manager_sample_rate;
    oboe::DefaultStreamValues::FramesPerBurst = audio_manager_frames_per_burst;
}

static TestErrorCallback sErrorCallbackTester;

JNIEXPORT void JNICALL
Java_com_mobileer_oboetester_TestErrorCallbackActivity_testDeleteCrash(
        JNIEnv *env, jobject instance) {
    sErrorCallbackTester.test();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestErrorCallbackActivity_getCallbackMagic(
        JNIEnv *env, jobject instance) {
    return sErrorCallbackTester.getCallbackMagic();
}

static TestRoutingCrash sRoutingCrash;

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRouteDuringCallbackActivity_startStream(
        JNIEnv *env, jobject instance,
        jboolean useInput) {
    return sRoutingCrash.start(useInput);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRouteDuringCallbackActivity_stopStream(
        JNIEnv *env, jobject instance) {
    return sRoutingCrash.stop();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRouteDuringCallbackActivity_getSleepTimeMicros(
        JNIEnv *env, jobject instance) {
    return sRoutingCrash.getSleepTimeMicros();
}

static TestColdStartLatency sColdStartLatency;

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_openStream(
        JNIEnv *env, jobject instance,
        jboolean useInput, jboolean useLowLatency, jboolean useMmap, jboolean useExclusive) {
    return sColdStartLatency.open(useInput, useLowLatency, useMmap, useExclusive);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_startStream(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.start();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_closeStream(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.close();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_getOpenTimeMicros(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.getOpenTimeMicros();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_getStartTimeMicros(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.getStartTimeMicros();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_getColdStartTimeMicros(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.getColdStartTimeMicros();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestColdStartLatencyActivity_getAudioDeviceId(
        JNIEnv *env, jobject instance) {
    return sColdStartLatency.getDeviceId();
}

static TestRapidCycle sRapidCycle;

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRapidCycleActivity_startRapidCycleTest(JNIEnv *env, jobject thiz,
                                                                        jboolean use_open_sl) {
    return sRapidCycle.start(use_open_sl);
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRapidCycleActivity_stopRapidCycleTest(JNIEnv *env, jobject thiz) {
    return sRapidCycle.stop();
}

JNIEXPORT jint JNICALL
Java_com_mobileer_oboetester_TestRapidCycleActivity_getCycleCount(JNIEnv *env, jobject thiz) {
    return sRapidCycle.getCycleCount();
}

} // extern "C"
