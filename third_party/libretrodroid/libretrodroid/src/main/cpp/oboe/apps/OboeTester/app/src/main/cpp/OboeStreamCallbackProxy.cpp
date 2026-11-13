/*
 * Copyright 2017 The Android Open Source Project
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

#include "common/OboeDebug.h"
#include "OboeStreamCallbackProxy.h"

bool OboeStreamCallbackProxy::mCallbackReturnStop = false;

oboe::DataCallbackResult OboeStreamCallbackProxy::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int numFrames) {
    oboe::DataCallbackResult callbackResult = oboe::DataCallbackResult::Stop;
    int64_t startTimeNanos = getNanoseconds();

    // Record which CPU this is running on.
    orCurrentCpuMask(sched_getcpu());

    // Tell ADPF in advance what our workload will be.
    if (mWorkloadReportingEnabled) {
        audioStream->reportWorkload(mNumWorkloadVoices);
    }

    // Change affinity if app requested a change.
    uint32_t mask = mCpuAffinityMask;
    if (mask != mPreviousMask) {
        int err = applyCpuAffinityMask(mask);
        if (err != 0) {
        }
        mPreviousMask = mask;
    }

    mCallbackCount++;
    mFramesPerCallback = numFrames;

    if (mCallbackReturnStop) {
        return oboe::DataCallbackResult::Stop;
    }

    if (mCallback != nullptr) {
        callbackResult = mCallback->onAudioReady(audioStream, audioData, numFrames);
    }

    mSynthWorkload.onCallback(mNumWorkloadVoices);
    if (mNumWorkloadVoices > 0) {
        // Render into the buffer or discard the synth voices.
        float *buffer = (audioStream->getChannelCount() == 2 && mHearWorkload)
                        ? static_cast<float *>(audioData) : nullptr;
        mSynthWorkload.renderStereo(buffer, numFrames);
    }

    // Measure CPU load.
    int64_t currentTimeNanos = getNanoseconds();
    // Sometimes we get a short callback when doing sample rate conversion.
    // Just ignore those to avoid noise.
    if (numFrames > (getFramesPerCallback() / 2)) {
        int64_t calculationTime = currentTimeNanos - startTimeNanos;
        float currentCpuLoad = calculationTime * 0.000000001f * audioStream->getSampleRate() / numFrames;
        mCpuLoad = (mCpuLoad * 0.95f) + (currentCpuLoad * 0.05f); // simple low pass filter
        mMaxCpuLoad = std::max(currentCpuLoad, mMaxCpuLoad.load());
    }

    if (mPreviousCallbackTimeNs != 0) {
        mStatistics.add((currentTimeNanos - mPreviousCallbackTimeNs) * kNsToMsScaler);
    }
    mPreviousCallbackTimeNs = currentTimeNanos;

    return callbackResult;
}

int OboeStreamCallbackProxy::applyCpuAffinityMask(uint32_t mask) {
    int err = 0;
    // Capture original CPU set so we can restore it.
    if (!mIsOriginalCpuSetValid) {
        err = sched_getaffinity((pid_t) 0,
                                sizeof(mOriginalCpuSet),
                                &mOriginalCpuSet);
        if (err) {
            LOGE("%s(0x%02X) - sched_getaffinity(), errno = %d\n", __func__, mask, errno);
            return -errno;
        }
        mIsOriginalCpuSetValid = true;
    }
    if (mask) {
        cpu_set_t cpu_set;
        CPU_ZERO(&cpu_set);
        int cpuCount = sysconf(_SC_NPROCESSORS_CONF);
        for (int cpuIndex = 0; cpuIndex < cpuCount; cpuIndex++) {
            if (mask & (1 << cpuIndex)) {
                CPU_SET(cpuIndex, &cpu_set);
            }
        }
        err = sched_setaffinity((pid_t) 0, sizeof(cpu_set_t), &cpu_set);
    } else {
        // Restore original mask.
        err = sched_setaffinity((pid_t) 0, sizeof(mOriginalCpuSet), &mOriginalCpuSet);
    }
    if (err) {
        LOGE("%s(0x%02X) - sched_setaffinity(), errno = %d\n", __func__, mask, errno);
        return -errno;
    }
    return 0;
}
