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
package com.mobileer.oboetester;

import java.util.ArrayList;

/**
 * Analyze a recording and extract edges for latency analysis.
 */
public class TapLatencyAnalyser {
    public static final int TYPE_TAP = 0;
    float[] mHighPassBuffer;

    private float mDroop = 0.995f;
    private static final float EDGE_THRESHOLD = 0.01f;
    private static final float LOW_FRACTION = 0.5f;

    public static class TapLatencyEvent {
        public int type;
        public int sampleIndex;
        public TapLatencyEvent(int type, int sampleIndex) {
            this.type = type;
            this.sampleIndex = sampleIndex;
        }
    }

    public TapLatencyEvent[] analyze(float[] buffer, int offset, int numSamples) {
        // Use high pass filter to remove rumble from air conditioners.
        mHighPassBuffer = new float[numSamples];
        highPassFilter(buffer, offset, numSamples, mHighPassBuffer);
        // Apply envelope follower.
        float[] peakBuffer = new float[numSamples];
        fillPeakBuffer(mHighPassBuffer, 0, numSamples, peakBuffer);
        // Look for two attacks.
        return scanForEdges(peakBuffer, numSamples);
    }

    public float[] getFilteredBuffer() {
        return mHighPassBuffer;
    }

    // Based on https://en.wikipedia.org/wiki/High-pass_filter
    private void highPassFilter(float[] buffer, int offset, int numSamples, float[] highPassBuffer) {
        float xn1 = 0.0f;
        float yn1 = 0.0f;
        final float alpha = 0.8f;
        for (int i = 0; i < numSamples; i++) {
            float xn = buffer[i + offset];
            float yn = alpha * (yn1 + xn - xn1);
            highPassBuffer[i] = yn;
            xn1 = xn;
            yn1 = yn;
        }
    }

    private TapLatencyEvent[] scanForEdges(float[] peakBuffer, int numSamples) {
        ArrayList<TapLatencyEvent> events = new ArrayList<TapLatencyEvent>();
        float slow = 0.0f;
        float fast = 0.0f;
        final float slowCoefficient = 0.01f;
        final float fastCoefficient = 0.10f;
        float lowThreshold = EDGE_THRESHOLD;
        boolean armed = true;
        int sampleIndex = 0;
        for (float level : peakBuffer) {
            slow = slow + (level - slow) * slowCoefficient; // low pass filter
            fast = fast + (level - fast) * fastCoefficient; // low pass filter
            if (armed && (fast > EDGE_THRESHOLD) && (fast > (2.0 * slow))) {
                //System.out.println("edge at " + sampleIndex + ", slow " + slow + ", fast " + fast);
                events.add(new TapLatencyEvent(TYPE_TAP, sampleIndex));
                armed = false;
                lowThreshold = fast * LOW_FRACTION;
            }
            // Use hysteresis when rearming.
            if (!armed && (fast < lowThreshold)) {
                armed = true;
                // slow = fast; // This seems unnecessary.
                //events.add(new TapLatencyEvent(TYPE_TAP, sampleIndex));
            }
            sampleIndex++;
        }
        return events.toArray(new TapLatencyEvent[0]);
    }

    /**
     * Envelope follower that rides along the peaks of the waveforms
     * and then decays exponentially.
     *
     * @param buffer
     * @param offset
     * @param numSamples
     */
    private void fillPeakBuffer(float[] buffer, int offset, int numSamples, float[] peakBuffer) {
        float previous = 0.0f;
        for (int i = 0; i < numSamples; i++) {
            float input = Math.abs(buffer[i + offset]);
            float output = previous * mDroop;
            if (input > output) {
                output = input;
            }
            previous = output;
            peakBuffer[i] = output;
        }
    }
}
