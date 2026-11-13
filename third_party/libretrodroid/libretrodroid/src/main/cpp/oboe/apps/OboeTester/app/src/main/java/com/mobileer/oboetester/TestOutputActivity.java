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

package com.mobileer.oboetester;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

/**
 * Test basic output.
 */
public final class TestOutputActivity extends TestOutputActivityBase {

    public static final int MAX_CHANNEL_BOXES = 16;
    private CheckBox[] mChannelBoxes;
    private Spinner mOutputSignalSpinner;
    private TextView mVolumeTextView;
    private SeekBar mVolumeSeekBar;
    private CheckBox mShouldSetStreamControlByAttributes;

    private class OutputSignalSpinnerListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mAudioOutTester.setSignalType(pos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            mAudioOutTester.setSignalType(0);
        }
    }

    private SeekBar.OnSeekBarChangeListener mVolumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setVolume(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Override
    protected void inflateActivity() {
        setContentView(R.layout.activity_test_output);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateEnabledWidgets();

        mAudioOutTester = addAudioOutputTester();

        mChannelBoxes = new CheckBox[MAX_CHANNEL_BOXES];
        int ic = 0;
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox0);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox1);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox2);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox3);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox4);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox5);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox6);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox7);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox8);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox9);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox10);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox11);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox12);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox13);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox14);
        mChannelBoxes[ic++] = (CheckBox) findViewById(R.id.channelBox15);
        configureChannelBoxes(0);

        mOutputSignalSpinner = (Spinner) findViewById(R.id.spinnerOutputSignal);
        mOutputSignalSpinner.setOnItemSelectedListener(new OutputSignalSpinnerListener());
        mOutputSignalSpinner.setSelection(StreamConfiguration.NATIVE_API_UNSPECIFIED);

        mCommunicationDeviceView = (CommunicationDeviceView) findViewById(R.id.comm_device_view);

        mVolumeTextView = (TextView) findViewById(R.id.textVolumeSlider);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.faderVolumeSlider);
        mVolumeSeekBar.setOnSeekBarChangeListener(mVolumeChangeListener);

        mShouldSetStreamControlByAttributes = (CheckBox) findViewById(R.id.enableSetStreamControlByAttributes);
    }

    @Override
    int getActivityType() {
        return ACTIVITY_TEST_OUTPUT;
    }

    public void openAudio() throws IOException {
        super.openAudio();
        mShouldSetStreamControlByAttributes.setEnabled(false);
    }

    private void configureChannelBoxes(int channelCount) {
        for (int i = 0; i < mChannelBoxes.length; i++) {
            mChannelBoxes[i].setChecked(i < channelCount);
            mChannelBoxes[i].setEnabled(i < channelCount);
        }
    }

    private void setVolume(int progress) {
        // Convert from (0, 500) range to (-50, 0).
        double decibels = (progress - 500) / 10.0f;
        double amplitude = Math.pow(10.0, decibels / 20.0);
        // When the slider is all way to the left, set a zero amplitude.
        if (progress == 0) {
            amplitude = 0;
        }
        mVolumeTextView.setText("Volume(dB): " + String.format(Locale.getDefault(), "%.1f",
                decibels));
        mAudioOutTester.setAmplitude((float) amplitude);
    }


    public void stopAudio() {
        configureChannelBoxes(0);
        mOutputSignalSpinner.setEnabled(true);
        super.stopAudio();
    }

    public void pauseAudio() {
        configureChannelBoxes(0);
        mOutputSignalSpinner.setEnabled(true);
        super.pauseAudio();
    }

    public void releaseAudio() {
        configureChannelBoxes(0);
        mOutputSignalSpinner.setEnabled(true);
        super.releaseAudio();
    }

    public void closeAudio() {
        configureChannelBoxes(0);
        mOutputSignalSpinner.setEnabled(true);
        mShouldSetStreamControlByAttributes.setEnabled(true);
        super.closeAudio();
    }

    public void startAudio() throws IOException {
        super.startAudio();
        int channelCount = mAudioOutTester.getCurrentAudioStream().getChannelCount();
        configureChannelBoxes(channelCount);
        mOutputSignalSpinner.setEnabled(false);
    }

    public void onChannelBoxClicked(View view) {
        CheckBox checkBox = (CheckBox) view;
        String text = (String) checkBox.getText();
        int channelIndex = Integer.parseInt(text);
        mAudioOutTester.setChannelEnabled(channelIndex, checkBox.isChecked());
    }

    @Override
    protected boolean shouldSetStreamControlByAttributes() {
        return mShouldSetStreamControlByAttributes.isChecked();
    }

    @Override
    public void startTestUsingBundle() {
        try {
            StreamConfiguration requestedOutConfig = mAudioOutTester.requestedConfiguration;
            IntentBasedTestSupport.configureOutputStreamFromBundle(mBundleFromIntent, requestedOutConfig);

            int signalType = IntentBasedTestSupport.getSignalTypeFromBundle(mBundleFromIntent);
            mAudioOutTester.setSignalType(signalType);

            openAudio();
            startAudio();

            int durationSeconds = IntentBasedTestSupport.getDurationSeconds(mBundleFromIntent);
            if (durationSeconds > 0) {
                // Schedule the end of the test.
                Handler handler = new Handler(Looper.getMainLooper()); // UI thread
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopAutomaticTest();
                    }
                }, durationSeconds * 1000);
            }
        } catch (Exception e) {
            showErrorToast(e.getMessage());
        } finally {
            mBundleFromIntent = null;
        }
    }

    void stopAutomaticTest() {
        String report = getCommonTestReport();
        stopAudio();
        maybeWriteTestResult(report);
        mTestRunningByIntent = false;
    }
}
