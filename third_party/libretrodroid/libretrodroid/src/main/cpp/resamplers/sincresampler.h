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

#ifndef LIBRETRODROID_SINCRESAMPLER_H
#define LIBRETRODROID_SINCRESAMPLER_H

#include "resampler.h"

namespace libretrodroid {

class SincResampler : public Resampler {
public:
    void resample(const int16_t *source, int32_t inputFrames, int16_t *sink, int32_t sinkFrames) override;
    SincResampler(const int taps);
    ~SincResampler() = default;

private:
    static float sinc(float x);

private:
    static constexpr float PI_F = 3.14159265358979f;
    int halfTaps;
};

} //namespace libretrodroid

#endif //LIBRETRODROID_SINCRESAMPLER_H
