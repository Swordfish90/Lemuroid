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

#ifndef NATIVEOBOE_OBOESTREAMCALLBACKPROXY_H
#define NATIVEOBOE_OBOESTREAMCALLBACKPROXY_H

#include <unistd.h>
#include <sys/types.h>
#include <sys/sysinfo.h>

#include "oboe/Oboe.h"
#include "synth/Synthesizer.h"
#include "synth/SynthTools.h"
#include "OboeTesterStreamCallback.h"

class DoubleStatistics {
public:
    void add(double statistic) {
        if (skipCount < kNumberStatisticsToSkip) {
            skipCount++;
        } else {
            if (statistic <= 0.0) return;
            sum = statistic + sum;
            count++;
            minimum = std::min(statistic, minimum.load());
            maximum = std::max(statistic, maximum.load());
        }
    }

    double getAverage() const {
        return sum / count;
    }

    std::string dump() const {
        if (count == 0) return "?";
        char buff[100];
        snprintf(buff, sizeof(buff), "%3.1f/%3.1f/%3.1f ms", minimum.load(), getAverage(), maximum.load());
        std::string buffAsStr = buff;
        return buffAsStr;
    }

    void clear() {
        skipCount = 0;
        sum = 0;
        count = 0;
        minimum = DBL_MAX;
        maximum = 0;
    }

private:
    static constexpr double kNumberStatisticsToSkip = 5; // Skip the first 5 frames
    std::atomic<int> skipCount { 0 };
    std::atomic<double> sum { 0 };
    std::atomic<int> count { 0 };
    std::atomic<double> minimum { DBL_MAX };
    std::atomic<double> maximum { 0 };
};

/**
 * Manage the synthesizer workload that burdens the CPU.
 * Adjust the number of voices according to the requested workload.
 * Trigger noteOn and noteOff messages.
 */
class SynthWorkload {
public:
    SynthWorkload() {
        mSynth.setup(marksynth::kSynthmarkSampleRate, marksynth::kSynthmarkMaxVoices);
    }

    void onCallback(double workload) {
        // If workload changes then restart notes.
        if (workload != mPreviousWorkload) {
            mSynth.allNotesOff();
            mAreNotesOn = false;
            mCountdown = 0; // trigger notes on
            mPreviousWorkload = workload;
        }
        if (mCountdown <= 0) {
            if (mAreNotesOn) {
                mSynth.allNotesOff();
                mAreNotesOn = false;
                mCountdown = mOffFrames;
            } else {
                mSynth.notesOn((int)mPreviousWorkload);
                mAreNotesOn = true;
                mCountdown = mOnFrames;
            }
        }
    }

    /**
     * Render the notes into a stereo buffer.
     * Passing a nullptr will cause the calculated results to be discarded.
     * The workload should be the same.
     * @param buffer a real stereo buffer or nullptr
     * @param numFrames
     */
    void renderStereo(float *buffer, int numFrames) {
        if (buffer == nullptr) {
            int framesLeft = numFrames;
            while (framesLeft > 0) {
                int framesThisTime = std::min(kDummyBufferSizeInFrames, framesLeft);
                // Do the work then throw it away.
                mSynth.renderStereo(&mDummyStereoBuffer[0], framesThisTime);
                framesLeft -= framesThisTime;
            }
        } else {
            mSynth.renderStereo(buffer, numFrames);
        }
        mCountdown -= numFrames;
    }

private:
    marksynth::Synthesizer   mSynth;
    static constexpr int     kDummyBufferSizeInFrames = 32;
    float                    mDummyStereoBuffer[kDummyBufferSizeInFrames * 2];
    double                   mPreviousWorkload = 1.0;
    bool                     mAreNotesOn = false;
    int                      mCountdown = 0;
    int                      mOnFrames = (int) (0.2 * 48000);
    int                      mOffFrames = (int) (0.3 * 48000);
};

class OboeStreamCallbackProxy : public OboeTesterStreamCallback {
public:

    void setDataCallback(oboe::AudioStreamDataCallback *callback) {
        mCallback = callback;
        setCallbackCount(0);
        mStatistics.clear();
        mPreviousMask = 0;
    }

    static void setCallbackReturnStop(bool b) {
        mCallbackReturnStop = b;
    }

    int64_t getCallbackCount() {
        return mCallbackCount;
    }

    void setCallbackCount(int64_t count) {
        mCallbackCount = count;
    }

    int32_t getFramesPerCallback() {
        return mFramesPerCallback.load();
    }

    /**
     * Called when the stream is ready to process audio.
     */
    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int numFrames) override;

    /**
     * Specify the amount of artificial workload that will waste CPU cycles
     * and increase the CPU load.
     * @param workload typically ranges from 0 to 400
     */
    void setWorkload(int32_t workload) {
        mNumWorkloadVoices = std::max(0, workload);
    }

    int32_t getWorkload() const {
        return mNumWorkloadVoices;
    }

    void setHearWorkload(bool enabled) {
        mHearWorkload = enabled;
    }

    /**
     * This is the callback duration relative to the real-time equivalent.
     * So it may be higher than 1.0.
     * @return low pass filtered value for the fractional CPU load
     */
    float getCpuLoad() const {
        return mCpuLoad;
    }

    /**
     * Calling this will atomically reset the max to zero so only call
     * this from one client.
     *
     * @return last value of the maximum unfiltered CPU load.
     */
    float getAndResetMaxCpuLoad() {
        return mMaxCpuLoad.exchange(0.0f);
    }

    std::string getCallbackTimeString() const {
        return mStatistics.dump();
    }

    /**
     * @return mask of the CPUs used since the last reset
     */
    uint32_t getAndResetCpuMask() {
        return mCpuMask.exchange(0);
    }
    void orCurrentCpuMask(int cpuIndex) {
        mCpuMask |= (1 << cpuIndex);
    }

    /**
     * @param cpuIndex
     * @return 0 on success or a negative errno
     */
    int setCpuAffinity(int cpuIndex) {
        cpu_set_t cpu_set;
        CPU_ZERO(&cpu_set);
        CPU_SET(cpuIndex, &cpu_set);
        int err = sched_setaffinity((pid_t) 0, sizeof(cpu_set_t), &cpu_set);
        return err == 0 ? 0 : -errno;
    }

    /**
     *
     * @param mask bits for each CPU or zero for all
     * @return
     */
    int applyCpuAffinityMask(uint32_t mask);

    void setCpuAffinityMask(uint32_t mask) {
        mCpuAffinityMask = mask;
    }

    void setWorkloadReportingEnabled(bool enabled) {
        mWorkloadReportingEnabled = enabled;
    }

private:
    static constexpr double    kNsToMsScaler = 0.000001;
    std::atomic<float>         mCpuLoad{0.0f};
    std::atomic<float>         mMaxCpuLoad{0.0f};
    int64_t                    mPreviousCallbackTimeNs = 0;
    DoubleStatistics           mStatistics;
    int32_t                    mNumWorkloadVoices = 0;
    SynthWorkload              mSynthWorkload;
    bool                       mHearWorkload = false;
    bool                       mWorkloadReportingEnabled = false;

    oboe::AudioStreamDataCallback *mCallback = nullptr;
    static bool                mCallbackReturnStop;

    int64_t                    mCallbackCount = 0;
    std::atomic<int32_t>       mFramesPerCallback{0};

    std::atomic<uint32_t>      mCpuAffinityMask{0};
    std::atomic<uint32_t>      mPreviousMask{0};
    std::atomic<uint32_t>      mCpuMask{0};
    cpu_set_t                  mOriginalCpuSet;
    bool                       mIsOriginalCpuSetValid = false;

};

#endif //NATIVEOBOE_OBOESTREAMCALLBACKPROXY_H
