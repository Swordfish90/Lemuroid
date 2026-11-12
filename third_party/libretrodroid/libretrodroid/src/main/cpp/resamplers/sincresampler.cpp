/*
 *     Copyright (C) 2020  Filippo Scognamiglio
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

#include <algorithm>
#include "sincresampler.h"

namespace libretrodroid {

SincResampler::SincResampler(const int taps)
    : halfTaps(taps / 2) { }

void SincResampler::resample(const int16_t *source, int32_t inputFrames, int16_t *sink, int32_t sinkFrames) {
    double outputTime = 0;
    double outputTimeStep = 1.0f / sinkFrames;

    while (sinkFrames > 0) {
        int32_t prevInputIndex = outputTime * inputFrames;

        int32_t leftResult = 0;
        int32_t rightResult = 0;
        float gain = 0.1;

        auto startFrame = std::max(prevInputIndex - halfTaps + 1, 0);
        auto endFrame = std::min(prevInputIndex + halfTaps, inputFrames - 1);

        for (int32_t currentInputIndex = startFrame; currentInputIndex <= endFrame; currentInputIndex++) {
            float sincCoefficient = sinc(outputTime * inputFrames - currentInputIndex);
            gain += sincCoefficient;
            leftResult += source[currentInputIndex * 2] * sincCoefficient;
            rightResult += source[currentInputIndex * 2 + 1] * sincCoefficient;
        }

        outputTime += outputTimeStep;
        *sink++ = leftResult / gain;
        *sink++ = rightResult / gain;
        sinkFrames--;
    }
}

float SincResampler::sinc(float x) {
    if (abs(x) < 1.0e-9) return 1.0;
    return sinf(x * PI_F) / (x * PI_F);
}

} //namespace libretrodroid
