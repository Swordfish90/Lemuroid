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

#ifndef LIBRETRODROID_VFS_H
#define LIBRETRODROID_VFS_H

#include "unistd.h"
#include "retro_common_api.h"
#include "libretro.h"
#include "vfs.h"
#include "vfsfile.h"

#include <vector>
#include <string>
#include <memory>
#include <optional>

namespace libretrodroid {

class VFS {
public:
    static const int SUPPORTED_VERSION = 2;
    static VFS& getInstance()
    {
        static VFS instance;
        return instance;
    }
    VFS(VFS const&) = delete;
    void operator=(VFS const&) = delete;

    static retro_vfs_interface* getInterface();
    void initialize(std::vector<VFSFile> files);
    void deinitialize();

private:
    VFS() {}

    struct retro_vfs_file_handle* virtualOpen(const char *path, unsigned mode, unsigned hints);

    VFSFile* findVirtualFile(const char* path);

public:

    static const char* path(struct retro_vfs_file_handle *stream);
    static struct retro_vfs_file_handle* open(const char *path, unsigned mode, unsigned hints);
    static int close(struct retro_vfs_file_handle* stream);
    static int64_t size(struct retro_vfs_file_handle *stream);
    static int64_t tell(struct retro_vfs_file_handle *stream);
    static int64_t seek(struct retro_vfs_file_handle *stream, int64_t offset, int seek_position);
    static int64_t read(struct retro_vfs_file_handle *stream, void *s, uint64_t len);
    static int64_t write(struct retro_vfs_file_handle *stream, const void *s, uint64_t len);
    static int flush(struct retro_vfs_file_handle *stream);
    static int remove(const char *path);
    static int rename(const char *old_path, const char *new_path);

    static int64_t truncate(struct retro_vfs_file_handle *stream, int64_t length);

private:
    std::vector<VFSFile> virtualFiles;

};

} // namespace libretrodroid

#endif //LIBRETRODROID_VFS_H
