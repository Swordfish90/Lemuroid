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

#ifndef LIBRETRODROID_MICROPHONE_H
#define LIBRETRODROID_MICROPHONE_H

#include <memory>
#include <mutex>

#include "libretro.h"
#include "oboe/AudioStreamCallback.h"
#include "oboe/FifoBuffer.h"

namespace libretrodroid {

class Microphone: public oboe::AudioStreamCallback {
public:
    explicit Microphone(int sampleRate);
    Microphone(const Microphone& other) = delete;
    Microphone& operator=(const Microphone& other) = delete;

    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* audioStream,
        void *audioData,
        int32_t numFrames
    ) override;

    bool open();
    bool close();
    int read(int16_t* samples, int numSamples);

    void setRunning(bool shouldRun);
    bool isRunning() const;

    int sampleRate() const;

private:
    bool mIsRunning = false;
    int mSampleRate = 44100;
    std::mutex lock;
    std::unique_ptr<oboe::FifoBuffer> fifoBuffer = nullptr;
    std::shared_ptr<oboe::AudioStream> inputStream;
};

}

#endif //LIBRETRODROID_MICROPHONE_H
