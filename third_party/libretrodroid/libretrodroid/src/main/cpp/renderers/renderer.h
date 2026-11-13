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

#ifndef LIBRETRODROID_RENDERER_H
#define LIBRETRODROID_RENDERER_H

#include <cstdint>
#include <utility>
#include <vector>
#include <optional>

#include "../shadermanager.h"

namespace libretrodroid {

class Renderer {
public:
    struct PassData {
        std::optional<unsigned int> framebuffer = std::nullopt;
        std::optional<unsigned int> texture = std::nullopt;
        std::optional<unsigned int> width = std::nullopt;
        std::optional<unsigned int> height = std::nullopt;
    };

public:
    virtual uintptr_t getFramebuffer() = 0;
    virtual uintptr_t getTexture() = 0;
    virtual void updateRenderedResolution(unsigned width, unsigned height) = 0;
    virtual void setPixelFormat(int pixelFormat) = 0;
    virtual void onNewFrame(const void *data, unsigned width, unsigned height, size_t pitch);
    virtual bool rendersInVideoCallback() = 0;
    virtual void setShaders(ShaderManager::Chain shaders) = 0;
    virtual PassData getPassData(unsigned int layer) = 0;

    virtual ~Renderer() = default;

public:
    std::pair<int, int> lastFrameSize;
};

}


#endif //LIBRETRODROID_RENDERER_H
