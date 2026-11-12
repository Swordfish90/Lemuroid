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

#ifndef LIBRETRODROID_LIBRETRODROIDEXCEPTION_H
#define LIBRETRODROID_LIBRETRODROIDEXCEPTION_H

#include <string>

namespace libretrodroid {

class LibretroDroidError : public std::runtime_error {
public:
    LibretroDroidError(const std::string& msg, int errorCode);
    int getErrorCode() { return errorCode; }

private:
    int errorCode;
};

} // namespace libretrodroid

#endif //LIBRETRODROID_LIBRETRODROIDEXCEPTION_H
