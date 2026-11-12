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

#ifndef LIBRETRODROID_IMAGERENDERERES2_H
#define LIBRETRODROID_IMAGERENDERERES2_H

#include "GLES2/gl2.h"

#include "../renderer.h"
#include "../../libretro-common/include/libretro.h"

#include <cstdint>
#include <utility>
#include <vector>

namespace libretrodroid {

class ImageRendererES2: public Renderer {
public:
    explicit ImageRendererES2();
    uintptr_t getTexture() override;
    uintptr_t getFramebuffer() override;
    void onNewFrame(const void *data, unsigned width, unsigned height, size_t pitch) override;
    void setPixelFormat(int pixelFormat) override;
    void updateRenderedResolution(unsigned int width, unsigned int height) override;
    bool rendersInVideoCallback() override;

    void setShaders(ShaderManager::Chain shaders) override;

    PassData getPassData(unsigned int layer) override;

private:
    void convertDataFromRGB8888(const void* pixelData, size_t size);
    void convertDataFrom0RGB1555(const void *data, unsigned int width, unsigned int height, size_t pitch) const;

private:
    int pixelFormat = RETRO_PIXEL_FORMAT_RGB565;
    unsigned int bytesPerPixel = 1;
    unsigned int glType = 0;
    unsigned int glInternalFormat = 0;
    unsigned int glFormat = 0;

    bool linear = false;

    unsigned int currentTexture = 0;
};

}

#endif //LIBRETRODROID_IMAGERENDERERES2_H
