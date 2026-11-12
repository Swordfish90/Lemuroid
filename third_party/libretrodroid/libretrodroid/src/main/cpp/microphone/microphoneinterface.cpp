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

#include "microphoneinterface.h"
#include "../log.h"
#include "microphone.h"

namespace libretrodroid {

retro_microphone_interface* MicrophoneInterface::getInterface() {
    LOGI("Fetching microphone interface");

    return new retro_microphone_interface{
        .interface_version = 1,
        .open_mic = libretroOpenMicrophone,
        .close_mic = libretroCloseMicrophone,
        .get_params = libretroGetMicrophoneParams,
        .set_mic_state = libretroSetMicrophoneState,
        .get_mic_state = libretroGetMicrophoneState,
        .read_mic = libretroReadMicrophone,
    };
}

retro_microphone_t* MicrophoneInterface::libretroOpenMicrophone(const retro_microphone_params_t* params) {
    LOGI("Opened microphone");
    auto* result = new Microphone((int) params->rate);
    result->open();
    return reinterpret_cast<retro_microphone_t*>(result);
}

void MicrophoneInterface::libretroCloseMicrophone(retro_microphone_t* opaqueMicrophone) {
    LOGI("Closed microphone");
    auto microphone = reinterpret_cast<Microphone*>(opaqueMicrophone);
    microphone->close();
    delete microphone;
}

bool MicrophoneInterface::libretroGetMicrophoneParams(
    const retro_microphone_t* opaqueMicrophone,
    retro_microphone_params_t* params
) {
    auto microphone = reinterpret_cast<const Microphone*>(opaqueMicrophone);
    params->rate = microphone->sampleRate();
    return true;
}

bool MicrophoneInterface::libretroSetMicrophoneState(
    retro_microphone_t* opaqueMicrophone,
    bool state
) {
    auto microphone = reinterpret_cast<Microphone*>(opaqueMicrophone);
    microphone->setRunning(state);
    return true;
}

bool MicrophoneInterface::libretroGetMicrophoneState(const retro_microphone_t* opaqueMicrophone) {
    auto microphone = reinterpret_cast<const Microphone*>(opaqueMicrophone);
    return microphone->isRunning();
}

int MicrophoneInterface::libretroReadMicrophone(
    retro_microphone_t* opaqueMicrophone,
    int16_t* samples,
    size_t num_samples
) {
    auto microphone = reinterpret_cast<Microphone*>(opaqueMicrophone);
    return microphone->read(samples, (int) num_samples);
}

} // libretrodroid