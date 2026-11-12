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

#ifndef LIBRETRODROID_VFSFILE_H
#define LIBRETRODROID_VFSFILE_H

#include <string>

#include "fdwrapper.h"

namespace libretrodroid {

class VFSFile {
public:
    VFSFile(std::string path, const int fd);

    VFSFile(VFSFile&& other) = default;
    VFSFile& operator=(VFSFile&&) = default;

    // Delete copy constructor. File ownership has to be preserved.
    VFSFile(const VFSFile& other) = delete;
    VFSFile& operator=(const VFSFile&) = delete;

    const std::string& getFileName() const;
    int getFD() const;

private:
    std::string virtualPath;
    std::unique_ptr<FDWrapper> fd;
};

}

#endif //LIBRETRODROID_VFSFILE_H
