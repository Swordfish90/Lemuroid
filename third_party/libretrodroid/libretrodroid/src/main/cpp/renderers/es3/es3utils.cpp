/*
 *     Copyright (C) 2022  Filippo Scognamiglio
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

#include "es3utils.h"

#include "../../log.h"

namespace libretrodroid {

std::unique_ptr<ES3Utils::Framebuffer> ES3Utils::createFramebuffer(
    unsigned int width,
    unsigned int height,
    bool linear,
    bool repeat,
    bool includeDepth,
    bool includeStencil
) {
    auto result = std::make_unique<Framebuffer>();
    result->width = width;
    result->height = height;

    glGenFramebuffers(1, &result->framebuffer);
    glGenTextures(1, &result->texture);

    if (includeDepth) {
        unsigned int depthBuffer;
        glGenRenderbuffers(1, &depthBuffer);
        result->depth = depthBuffer;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, result->framebuffer);

    glBindTexture(GL_TEXTURE_2D, result->texture);
    glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, width, height);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, repeat ? GL_MIRRORED_REPEAT : GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, repeat ? GL_MIRRORED_REPEAT : GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, linear ? GL_LINEAR : GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, linear ? GL_LINEAR : GL_NEAREST);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, result->texture, 0);

    if (includeDepth) {
        glBindRenderbuffer(GL_RENDERBUFFER, result->depth.value());
        glRenderbufferStorage(
            GL_RENDERBUFFER,
            includeStencil ? GL_DEPTH24_STENCIL8 : GL_DEPTH_COMPONENT16,
            width,
            height
        );
        glFramebufferRenderbuffer(
            GL_FRAMEBUFFER,
            includeStencil ? GL_DEPTH_STENCIL_ATTACHMENT : GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER,
            result->depth.value()
        );
    }

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Error while creating framebuffer. Leaving!");
        throw std::runtime_error("Cannot create framebuffer");
    }

    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);

    return result;
}

void ES3Utils::deleteFramebuffer(std::unique_ptr<ES3Utils::Framebuffer> data) {
    if (data == nullptr) {
        return;
    }

    glDeleteFramebuffers(1, &data->framebuffer);
    glDeleteTextures(1, &data->texture);

    if (data->depth.has_value()) {
        glDeleteRenderbuffers(1, &data->depth.value());
    }
}

std::unique_ptr<ES3Utils::Framebuffers> ES3Utils::buildShaderPasses(
    unsigned int width,
    unsigned int height,
    const libretrodroid::ShaderManager::Chain &shaders
) {
    auto result = std::make_unique<std::vector<std::unique_ptr<ES3Utils::Framebuffer>>>();
    auto passes = shaders.passes;

    for (int i = 0; i < passes.size() - 1; ++i) {
        auto pass = passes[i];
        unsigned int passWidth = std::lround(width * pass.scale);
        unsigned int passHeight = std::lround(height * pass.scale);

        std::unique_ptr<ES3Utils::Framebuffer> data = ES3Utils::createFramebuffer(
            passWidth,
            passHeight,
            pass.linear,
            false,
            false,
            false
        );
        result->push_back(std::move(data));
    }

    return result;
}

} //namespace libretrodroid