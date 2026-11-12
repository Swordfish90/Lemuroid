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

#include "rumble.h"
#include "log.h"
#include "environment.h"

namespace libretrodroid {

void Rumble::fetchFromEnvironment() {
    auto environmentStates = Environment::getInstance().getLastRumbleStates();

    for (int i = 0; i < environmentStates.size(); ++i) {
        if (rumbleStates[i] == environmentStates[i]) {
            continue;
        }

        dirtyStates[i] = true;
        rumbleStates[i] = environmentStates[i];
    }
}

void Rumble::handleRumbleUpdates(const std::function<void(int, float, float)>& handler) {
    for (int i = 0; i < rumbleStates.size(); ++i) {
        if (!dirtyStates[i]) {
            continue;
        }

        dirtyStates[i] = false;

        float normalizedWeak = (float) rumbleStates[i].strengthWeak / 0xFFFF;
        float normalizedStrong = (float) rumbleStates[i].strengthStrong / 0xFFFF;
        handler(i, normalizedWeak, normalizedStrong);
    }
}

} //namespace libretrodroid
