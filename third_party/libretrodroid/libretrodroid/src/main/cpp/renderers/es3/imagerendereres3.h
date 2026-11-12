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

#ifndef LIBRETRODROID_IMAGERENDERERES3_H
#define LIBRETRODROID_IMAGERENDERERES3_H

#include "../renderer.h"
#include "../../libretro-common/include/libretro.h"
#include "es3utils.h"

#include "GLES3/gl3.h"

#include <cstdint>
#include <utility>
#include <vector>
#include <memory>

namespace libretrodroid {

class ImageRendererES3: public Renderer {
public:
    explicit ImageRendererES3();
    uintptr_t getTexture() override;
    uintptr_t getFramebuffer() override;
    void onNewFrame(const void *data, unsigned width, unsigned height, size_t pitch) override;
    void setPixelFormat(int pixelFormat) override;
    void updateRenderedResolution(unsigned int width, unsigned int height) override;

    bool rendersInVideoCallback() override;

    void setShaders(ShaderManager::Chain shaders) override;

    PassData getPassData(unsigned int layer) override;

private:
    void initializeTextures(unsigned int width, unsigned int height);
    void applyGLSwizzle(int r, int g, int b, int a);
    void convertDataFrom0RGB1555(const void *data, unsigned int width, unsigned int height, size_t pitch) const;

private:
    int pixelFormat = RETRO_PIXEL_FORMAT_RGB565;
    unsigned int bytesPerPixel = 1;
    bool swapRedAndBlueChannels = false;
    unsigned int glType = 0;
    unsigned int glInternalFormat = 0;
    unsigned int glFormat = 0;

    bool isDirty = true;

    unsigned int currentTexture = 0;

    ShaderManager::Chain shaders;
    std::unique_ptr<ES3Utils::Framebuffers> framebuffers = std::make_unique<ES3Utils::Framebuffers>();
};

}

#endif //LIBRETRODROID_IMAGERENDERERES3_H
