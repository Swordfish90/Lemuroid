package com.mobileer.oboetester;

import android.app.Activity;
import android.media.AudioDeviceInfo;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

/**
 * Measure tap-to-tone latency by and update the waveform display.
 */
public class TapToToneTester {

    private static final float MAX_TOUCH_LATENCY = 0.200f;
    private static final float MAX_OUTPUT_LATENCY = 1.200f;
    private static final float ANALYSIS_TIME_MARGIN = 0.500f;

    private static final float ANALYSIS_TIME_DELAY = MAX_OUTPUT_LATENCY;
    private static final float ANALYSIS_TIME_TOTAL = MAX_TOUCH_LATENCY + MAX_OUTPUT_LATENCY;
    private static final int ANALYSIS_SAMPLE_RATE = 48000; // need not match output rate

    private final boolean mRecordEnabled = true;
    private final AudioRecordThread mRecorder;
    private final TapLatencyAnalyser mTapLatencyAnalyser;

    private final Activity mActivity;
    private final WaveformView mWaveformView;
    private final TextView mResultView;

    private final String mTapInstructions;
    private float mAnalysisTimeMargin = ANALYSIS_TIME_MARGIN;

    private boolean mArmed = true;

    // Stats for latency
    private int mMeasurementCount;
    private int mLatencySumSamples;
    private int mLatencyMin;
    private int mLatencyMax;

    public static class TestResult {
        public float[] samples;
        public float[] filtered;
        public int frameRate;
        public TapLatencyAnalyser.TapLatencyEvent[] events;
    }

    public TapToToneTester(Activity activity, String tapInstructions) {
        mActivity = activity;
        mTapInstructions = tapInstructions;
        mResultView = (TextView) activity.findViewById(R.id.resultView);
        mWaveformView = (WaveformView) activity.findViewById(R.id.waveview_audio);
        mWaveformView.setEnabled(false);

        if (mRecordEnabled) {
            float analysisTimeMax = ANALYSIS_TIME_TOTAL + mAnalysisTimeMargin;
            mRecorder = new AudioRecordThread(ANALYSIS_SAMPLE_RATE,
                    1,
                    (int) (analysisTimeMax * ANALYSIS_SAMPLE_RATE));
        }
        mTapLatencyAnalyser = new TapLatencyAnalyser();
    }

    public void start() throws IOException {
        if (mRecordEnabled) {
            mRecorder.startAudio();
            mWaveformView.setEnabled(true);
        }
    }

    public void stop() {
        if (mRecordEnabled) {
            mRecorder.stopAudio();
            mWaveformView.setEnabled(false);
        }
    }

    /**
     * @return true if ready to process a tap, false if already triggered
     */
    public boolean isArmed() {
        return mArmed;
    }

    public void setArmed(boolean armed) {
        this.mArmed = armed;
    }

    public void analyzeLater(String message) {
        showPendingStatus(message);
        Runnable task = this::analyseAndShowResults;
        scheduleTaskWhenDone(task);
        mArmed = false;
    }

    private void showPendingStatus(final String message) {
        mWaveformView.post(() -> {
            mWaveformView.setMessage(message);
            mWaveformView.clearSampleData();
            mWaveformView.invalidate();
        });
    }

    private void scheduleTaskWhenDone(Runnable task) {
        if (mRecordEnabled) {
            // schedule an analysis to start in the near future
            int numSamples = (int) (mRecorder.getSampleRate() * ANALYSIS_TIME_DELAY);
            mRecorder.scheduleTask(numSamples, task);
        }
    }

    private void analyseAndShowResults() {
        TestResult result = analyzeCapturedAudio();
        if (result != null) {
            showTestResults(result);
        }
    }

    public TestResult analyzeCapturedAudio() {
        if (!mRecordEnabled) return null;
        int numSamples = (int) (mRecorder.getSampleRate() * ANALYSIS_TIME_TOTAL);
        float[] buffer = new float[numSamples];
        mRecorder.setCaptureEnabled(false); // TODO wait for it to settle
        int numRead = mRecorder.readMostRecent(buffer);

        TestResult result = new TestResult();
        result.samples = buffer;
        result.frameRate = mRecorder.getSampleRate();
        result.events = mTapLatencyAnalyser.analyze(buffer, 0, numRead);
        result.filtered = mTapLatencyAnalyser.getFilteredBuffer();
        mRecorder.setCaptureEnabled(true);
        return result;
    }

    public void resetLatency() {
        mMeasurementCount = 0;
        mLatencySumSamples = 0;
        mLatencyMin = Integer.MAX_VALUE;
        mLatencyMax = 0;
        showTestResults(null);
    }

    // Runs on UI thread.
    public void showTestResults(TestResult result) {
        String text;
        mWaveformView.setMessage(null);
        if (result == null) {
            text = mTapInstructions;
            mWaveformView.clearSampleData();
        } else {
            // Show edges detected.
            if (result.events.length == 0) {
                mWaveformView.setCursorData(null);
            } else {
                int numEdges = Math.min(8, result.events.length);
                int[] cursors = new int[numEdges];
                for (int i = 0; i < numEdges; i++) {
                    cursors[i] = result.events[i].sampleIndex;
                }
                mWaveformView.setCursorData(cursors);
            }
            // Did we get a good measurement?
            if (result.events.length < 2) {
                text = "Not enough edges. Use fingernail.\n";
            } else if (result.events.length > 2) {
                text = "Too many edges.\n";
            } else {
                int latencySamples = result.events[1].sampleIndex - result.events[0].sampleIndex;
                mLatencySumSamples += latencySamples;
                mMeasurementCount++;

                int latencyMillis = 1000 * latencySamples / result.frameRate;
                if (mLatencyMin > latencyMillis) {
                    mLatencyMin = latencyMillis;
                }
                if (mLatencyMax < latencyMillis) {
                    mLatencyMax = latencyMillis;
                }

                text = String.format(Locale.getDefault(), "tap-to-tone latency = %3d msec\n", latencyMillis);
            }
            mWaveformView.setSampleData(result.filtered);
        }

        if (mMeasurementCount > 0) {
            int averageLatencySamples = mLatencySumSamples / mMeasurementCount;
            int averageLatencyMillis = 1000 * averageLatencySamples / result.frameRate;
            final String plural = (mMeasurementCount == 1) ? "test" : "tests";
            text = text + String.format(Locale.getDefault(), "min = %3d, avg = %3d, max = %3d, %d %s",
                    mLatencyMin, averageLatencyMillis, mLatencyMax, mMeasurementCount, plural);
        }
        final String postText = text;
        mWaveformView.post(new Runnable() {
            public void run() {
                mResultView.setText(postText);
                mWaveformView.postInvalidate();
            }
        });

        mArmed = true;
    }

    void setInputDevice(AudioDeviceInfo deviceInfo) {
        mRecorder.setInputDevice(deviceInfo);
    }
}
