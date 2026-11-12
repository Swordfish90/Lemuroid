/*
 *     Copyright (C) 2025  Filippo Scognamiglio
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
#ifndef LIBRETRODROID_IMMERSIVEMODE_H
#define LIBRETRODROID_IMMERSIVEMODE_H

#include <string>
#include <array>
#include <vector>
#include <memory>
#include <GLES2/gl2.h>

#include "renderers/es3/es3utils.h"

namespace libretrodroid {

class ImmersiveMode {
public:
    struct Config {
        Config(
            int downscaledWidth = 8,
            int downscaledHeight = 8,
            int blurMaskSize = 5,
            float blurBrightness = 0.5F,
            int blurSkipUpdate = 2,
            float blendFactor = 0.1F
        ) :
            downscaledWidth(downscaledWidth),
            downscaledHeight(downscaledHeight),
            blurMaskSize(blurMaskSize),
            blurBrightness(blurBrightness),
            blurSkipUpdate(blurSkipUpdate),
            blendFactor(blendFactor)
        {}

        int downscaledWidth;
        int downscaledHeight;
        int blurMaskSize;
        float blurBrightness;
        int blurSkipUpdate;
        float blendFactor;
    };

    explicit ImmersiveMode(const Config& config = Config()) :
        downscaledWidth(config.downscaledWidth),
        downscaledHeight(config.downscaledHeight),
        blurMaskSize(config.blurMaskSize),
        blurBrightness(config.blurBrightness),
        blurSkipUpdate(config.blurSkipUpdate > 0 ? config.blurSkipUpdate : 1),
        blendFactor(config.blendFactor)
    {}

    void renderBackground(
        unsigned screenWidth,
        unsigned screenHeight,
        std::array<float, 12> backgroundVertices,
        std::array<float, 4> foregroundBounds,
        GLfloat* framebufferVertices,
        uintptr_t texture
    );

private:
    std::string generateBlurShader();
    static std::vector<float> generateSmoothingWeights(int size, float brightness);

    void initializeShaders();

    void initializeFramebuffers();

    void renderToFramebuffer(uintptr_t texture, GLfloat* gBackgroundVertices);

    void renderToFinalOutput(
        unsigned screenWidth,
        unsigned screenHeight,
        std::array<float, 12> backgroundVertices,
        std::array<float, 4> foregroundBounds
    );

private:
    const char* defaultVertexShaderSource = R"(
        attribute mediump vec2 aPosition;
        attribute mediump vec2 aTexCoord;
        varying mediump vec2 vTexCoord;
        void main() {
            gl_Position = vec4(aPosition, 0.0, 1.0);
            vTexCoord = aTexCoord;
        }
    )";

    const char* displayFragmentShaderSource = R"(
        precision mediump float;
        varying mediump vec2 vTexCoord;
        uniform lowp sampler2D texture;
        uniform vec4 foregroundBounds; // (minX, minY, maxX, maxY)

        void main() {
            mediump vec2 normalizedCoords = (vTexCoord - foregroundBounds.xy) / abs(foregroundBounds.zw - foregroundBounds.xy);
            lowp vec2 speed = abs(foregroundBounds.zw - foregroundBounds.xy);

            mediump vec2 adjustedCoords = vec2(0.0);
            adjustedCoords -= speed * normalizedCoords;
            adjustedCoords += (1.0 + speed) * step(vec2(0.0), normalizedCoords) * normalizedCoords;
            adjustedCoords -= (1.0 - speed) * step(vec2(1.0), normalizedCoords) * (normalizedCoords - 1.0);

            gl_FragColor = texture2D(texture, adjustedCoords);
        }
    )";

    const char* blendingVertexShaderSource = R"(
        attribute mediump vec2 aPosition;
        attribute mediump vec2 aTexCoord;
        varying mediump vec2 vTexCoord;
        void main() {
            gl_Position = vec4(aPosition, 0.0, 1.0);
            vTexCoord = aTexCoord;
        }
    )";

    const char* blendingFragmentShaderSource = R"(
        precision mediump float;
        varying mediump vec2 vTexCoord;
        uniform lowp sampler2D currentFrame;
        uniform lowp sampler2D previousFrame;
        uniform float blendFactor;

        void main() {
            lowp float margin = -0.125;
            mediump vec2 adjustedCoord = vTexCoord * (1.0 - 2.0 * margin) + margin;
            lowp vec4 currentColor = texture2D(currentFrame, adjustedCoord);
            lowp vec4 prevColor = texture2D(previousFrame, vTexCoord);
            gl_FragColor = mix(prevColor, currentColor, blendFactor);
        }
    )";

    GLfloat gTextureCoords[12] = {
        0.0F,
        0.0F,

        0.0F,
        1.0F,

        1.0F,
        0.0F,

        1.0F,
        0.0F,

        0.0F,
        1.0F,

        1.0F,
        1.0F,
    };

    GLuint blurShaderProgram = 0;
    std::vector<std::unique_ptr<ES3Utils::Framebuffer>> blurFramebuffers;

    GLint blurPositionHandle = -1;
    GLint blurTextureCoordinatesHandle = -1;
    GLint blurTextureHandle = -1;

    GLint blurDirectionHandle = -1;

    GLuint displayShaderProgram = 0;
    GLint displayTextureHandle = -1;
    GLint displayPositionHandle = -1;
    GLint displayForegroundBoundsHandle = -1;
    GLint displayTextureCoordinatesHandle = -1;

    int downscaledWidth;
    int downscaledHeight;
    int blurMaskSize;
    float blurBrightness;
    int blurSkipUpdate;
    float blendFactor;

    GLuint blendShaderProgram = 0;
    GLint blendTextureHandle = -1;
    GLint blendPrevTextureHandle = -1;
    GLint blendFactorHandle = -1;
    int blendFramebufferWriteIndex = 0;
    int blendFramebufferCurrent = 0;
};

} // libretrodroid

#endif //LIBRETRODROID_IMMERSIVEMODE_H
