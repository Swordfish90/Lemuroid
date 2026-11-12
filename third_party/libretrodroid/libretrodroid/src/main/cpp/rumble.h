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

#ifndef LIBRETRODROID_RUMBLE_H
#define LIBRETRODROID_RUMBLE_H

#include <array>
#include <functional>

#include "rumblestate.h"

namespace libretrodroid {

class Rumble {
public:
    void fetchFromEnvironment();
    void handleRumbleUpdates(const std::function<void(int, float, float)> &handler);

private:
    std::array<RumbleState, 4> rumbleStates;
    std::array<bool, 4> dirtyStates;
};

}

#endif //LIBRETRODROID_RUMBLE_H
