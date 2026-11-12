/*
 *     Copyright (C) 2021  Filippo Scognamiglio
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

#ifndef LIBRETRODROID_FDWRAPPER_H
#define LIBRETRODROID_FDWRAPPER_H

#include <unistd.h>

namespace libretrodroid {

class FDWrapper {
public:
    // Delete copy and move to make sure the close function is never called.
    FDWrapper(const FDWrapper& other) = delete;
    FDWrapper(FDWrapper&& other) = delete;
    FDWrapper& operator=(const FDWrapper&) = delete;
    FDWrapper& operator=(FDWrapper&&) = delete;

    int getFD();

    FDWrapper(int fd) : fd(fd) { }
    ~FDWrapper();

private:
    int fd;
};

}

#endif //LIBRETRODROID_FDWRAPPER_H
