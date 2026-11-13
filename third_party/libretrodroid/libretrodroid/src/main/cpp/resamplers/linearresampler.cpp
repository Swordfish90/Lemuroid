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

#include <cmath>
#include <algorithm>
#include "linearresampler.h"

namespace libretrodroid {

void LinearResampler::resample(const int16_t *source, int32_t inputFrames, int16_t *sink, int32_t sinkFrames) {
    double outputTime = 0;
    double outputTimeStep = 1.0 / sinkFrames;

    double floatingPart, integerPart;

    while (sinkFrames > 0) {
        floatingPart = std::modf(outputTime * inputFrames, &integerPart);

        int32_t floorFrame = integerPart;
        int32_t ceilFrame = std::min(floorFrame + 1, inputFrames - 1);

        *sink++ = source[ceilFrame * 2] * (floatingPart) + source[floorFrame * 2] * (1.0 - floatingPart);
        *sink++ = source[ceilFrame * 2 + 1] * (floatingPart) + source[floorFrame * 2 + 1] * (1.0 - floatingPart);
        outputTime += outputTimeStep;
        sinkFrames--;
    }
}

} //namespace libretrodroid
