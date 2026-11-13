/*
 *     Copyright (C) 2019  Filippo Scognamiglio
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

#ifndef LIBRETRODROID_INPUT_H
#define LIBRETRODROID_INPUT_H

#include <cstdint>
#include <unordered_set>
#include <array>

namespace libretrodroid {

class Input {

private:
    struct GamePadState {
        std::unordered_set<int> pressedKeys;

        int dpadXAxis = 0;
        int dpadYAxis = 0;
        float joypadLeftXAxis = 0;
        float joypadLeftYAxis = 0;
        float joypadRightXAxis = 0;
        float joypadRightYAxis = 0;
        float pointerScreenXAxis = -1;
        float pointerScreenYAxis = -1;
    };

public:
    static constexpr int MOTION_SOURCE_DPAD = 0;
    static constexpr int MOTION_SOURCE_ANALOG_LEFT = 1;
    static constexpr int MOTION_SOURCE_ANALOG_RIGHT = 2;
    static constexpr int MOTION_SOURCE_POINTER = 3;
    static constexpr int MAX_RANGE_MOTION = 0x7fff;

    static constexpr int RETRO_DEVICE_ID_JOYPAD_UP_LEFT = 50;
    static constexpr int RETRO_DEVICE_ID_JOYPAD_UP_RIGHT = 51;
    static constexpr int RETRO_DEVICE_ID_JOYPAD_DOWN_LEFT = 52;
    static constexpr int RETRO_DEVICE_ID_JOYPAD_DOWN_RIGHT = 53;

    int16_t getInputState(unsigned port, unsigned device, unsigned index, unsigned id);

    void onKeyEvent(unsigned int port, int action, int keyCode);
    void onMotionEvent(int port, int motionSource, float xAxis, float yAxis);

private:
    const int UNKNOWN_KEY = -1;

    template<typename ...T>
    bool anyPressed(unsigned int port, unsigned int id, T&... args) const;
    bool anyPressed(unsigned int port, unsigned int id) const;
    int convertAndroidToLibretroKey(int keyCode) const;

    GamePadState pads[4];
};

}

#endif //LIBRETRODROID_INPUT_H
