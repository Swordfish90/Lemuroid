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

#ifndef LIBRETRODROID_FRAMEBUFFERRENDERER_H
#define LIBRETRODROID_FRAMEBUFFERRENDERER_H

#include "GLES3/gl3.h"
#include "GLES3/gl3ext.h"

#include "../renderer.h"
#include "es3utils.h"

namespace libretrodroid {

class FramebufferRenderer: public Renderer {
public:
    FramebufferRenderer(
        unsigned width,
        unsigned height,
        bool depth,
        bool stencil,
        ShaderManager::Chain shaders
    );
    uintptr_t getTexture() override;
    uintptr_t getFramebuffer() override;
    void onNewFrame(const void *data, unsigned width, unsigned height, size_t pitch) override;
    void setPixelFormat(int pixelFormat) override;
    void updateRenderedResolution(unsigned int width, unsigned int height) override;

    bool rendersInVideoCallback() override;

    void setShaders(ShaderManager::Chain shaders) override;
    PassData getPassData(unsigned int layer) override;

private:
    bool depth = false;
    bool stencil = false;

    unsigned int width;
    unsigned int height;

    bool isDirty = false;

    std::unique_ptr<ES3Utils::Framebuffer> framebuffer = std::make_unique<ES3Utils::Framebuffer>();

    ShaderManager::Chain shaders;
    std::unique_ptr<ES3Utils::Framebuffers> framebuffers = std::make_unique<ES3Utils::Framebuffers>();

    void initializeBuffers();
};

}

#endif //LIBRETRODROID_FRAMEBUFFERRENDERER_H
