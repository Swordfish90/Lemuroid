/*
 *     Copyright (C) 2025  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include "microphone.h"
#include "oboe/AudioStream.h"
#include "../log.h"
#include "libretro.h"

namespace libretrodroid {

oboe::DataCallbackResult Microphone::onAudioReady(
    oboe::AudioStream* audioStream,
    void* audioData,
    int32_t numFrames
) {
    std::lock_guard<std::mutex> l(lock);
    fifoBuffer->write(audioData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

Microphone::Microphone(int sampleRate):
    mSampleRate(sampleRate),
    fifoBuffer(std::make_unique<oboe::FifoBuffer>(2, sampleRate / 2)) { }

bool Microphone::open() {
    std::lock_guard<std::mutex> l(lock);

    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input);
    builder.setInputPreset(oboe::InputPreset::Generic);
    builder.setPerformanceMode(oboe::PerformanceMode::PowerSaving);
    builder.setSharingMode(oboe::SharingMode::Exclusive);
    builder.setSampleRate(mSampleRate);
    builder.setChannelCount(oboe::ChannelCount::Mono);
    builder.setFormat(oboe::AudioFormat::I16);
    builder.setCallback(this);

    oboe::Result result = builder.openStream(inputStream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to open microphone stream");
        return false;
    }

    result = inputStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start stream");
        return false;
    }

    return true;
}

bool Microphone::close() {
    std::lock_guard<std::mutex> l(lock);
    oboe::Result result = inputStream->close();
    if (result != oboe::Result::OK) {
        LOGE("Failed to close stream");
        return false;
    }
    return true;
}

void Microphone::setRunning(bool shouldRun) {
    if (shouldRun) {
        inputStream->requestStart();
    } else {
        inputStream->requestStop();
    }
    mIsRunning = shouldRun;
}

bool Microphone::isRunning() const {
    return mIsRunning;
}

int Microphone::read(int16_t* samples, int numSamples) {
    std::lock_guard<std::mutex> l(lock);
    return fifoBuffer->readNow(samples, numSamples);
}

int Microphone::sampleRate() const {
    return mSampleRate;
}

} // libretrodroid