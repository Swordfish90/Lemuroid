/*
 * Copyright (C) 2020 The Android Open Source Project
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

#ifndef ANALYZER_DATA_PATH_ANALYZER_H
#define ANALYZER_DATA_PATH_ANALYZER_H

#include <algorithm>
#include <cctype>
#include <iomanip>
#include <iostream>
#include <math.h>

#include "BaseSineAnalyzer.h"
#include "InfiniteRecording.h"
#include "LatencyAnalyzer.h"

/**
 * Output a steady sine wave and analyze the return signal.
 *
 * Use a cosine transform to measure the predicted magnitude and relative phase of the
 * looped back sine wave.
 */
class DataPathAnalyzer : public BaseSineAnalyzer {
public:

    DataPathAnalyzer() : BaseSineAnalyzer() {
        // Add a little bit of noise to reduce blockage by speaker protection and DRC.
        setNoiseAmplitude(0.02);
    }

    double calculatePhaseError(double p1, double p2) {
        double diff = p1 - p2;
        // Wrap around the circle.
        while (diff > M_PI) {
            diff -= (2 * M_PI);
        }
        while (diff < -M_PI) {
            diff += (2 * M_PI);
        }
        return diff;
    }

    /**
     * @param frameData contains microphone data with sine signal feedback
     * @param channelCount
     */
    result_code processInputFrame(const float *frameData, int /* channelCount */) override {
        result_code result = RESULT_OK;

        float sample = frameData[getInputChannel()];
        mInfiniteRecording.write(sample);

        if (transformSample(sample)) {
            // Analyze magnitude and phase on every period.
            if (mPhaseOffset != kPhaseInvalid) {
                double diff = fabs(calculatePhaseError(mPhaseOffset, mPreviousPhaseOffset));
                if (diff < mPhaseTolerance) {
                    mMaxMagnitude = std::max(mMagnitude, mMaxMagnitude);
                }
                mPreviousPhaseOffset = mPhaseOffset;
            }
        }
        return result;
    }

    std::string analyze() override {
        std::stringstream report;
        report << "DataPathAnalyzer ------------------\n";
        report << LOOPBACK_RESULT_TAG "sine.magnitude     = " << std::setw(8)
               << mMagnitude << "\n";
        report << LOOPBACK_RESULT_TAG "frames.accumulated = " << std::setw(8)
               << mFramesAccumulated << "\n";
        report << LOOPBACK_RESULT_TAG "sine.period        = " << std::setw(8)
               << mSinePeriod << "\n";
        return report.str();
    }

    void reset() override {
        BaseSineAnalyzer::reset();
        mPreviousPhaseOffset = 999.0; // Arbitrary high offset to prevent early lock.
        mMaxMagnitude = 0.0;
    }

    double getMaxMagnitude() {
        return mMaxMagnitude;
    }

private:
    double  mPreviousPhaseOffset = 0.0;
    double  mPhaseTolerance = 2 * M_PI  / 48;
    double  mMaxMagnitude = 0.0;
};
#endif // ANALYZER_DATA_PATH_ANALYZER_H
