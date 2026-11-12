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

#ifndef LIBRETRODROID_ERRORCODES_H
#define LIBRETRODROID_ERRORCODES_H

namespace libretrodroid {
    int ERROR_LOAD_LIBRARY = 0;
    int ERROR_LOAD_GAME = 1;
    int ERROR_GL_NOT_COMPATIBLE = 2;
    int ERROR_SERIALIZATION = 3;
    int ERROR_CHEAT = 4;
    int ERROR_GENERIC = -1;
}

#endif //LIBRETRODROID_ERRORCODES_H
