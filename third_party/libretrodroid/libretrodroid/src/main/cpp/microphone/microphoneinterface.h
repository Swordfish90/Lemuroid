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

#ifndef LIBRETRODROID_MICROPHONEINTERFACE_H
#define LIBRETRODROID_MICROPHONEINTERFACE_H

#include "libretro.h"

namespace libretrodroid {

class MicrophoneInterface {
public:
    static retro_microphone_interface* getInterface();

    static retro_microphone_t* libretroOpenMicrophone(const retro_microphone_params_t* params);

    static void libretroCloseMicrophone(retro_microphone_t* opaqueMicrophone);

    static bool libretroGetMicrophoneParams(
        const retro_microphone_t* microphone,
        retro_microphone_params_t* params
    );

    static bool libretroSetMicrophoneState(retro_microphone_t* opaqueMicrophone, bool state);

    static bool libretroGetMicrophoneState(const retro_microphone_t* opaqueMicrophone);

    static int
    libretroReadMicrophone(retro_microphone_t* microphone, int16_t* samples, size_t num_samples);
};

} // libretrodroid

#endif //LIBRETRODROID_MICROPHONEINTERFACE_H
