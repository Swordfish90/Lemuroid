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

#ifndef LIBRETRODROID_VIDEOLAYOUT_H
#define LIBRETRODROID_VIDEOLAYOUT_H

#include <array>

#include "utils/rect.h"

#define V_ALIGN_CENTER  0
#define V_ALIGN_TOP     1
#define V_ALIGN_BOTTOM  2

namespace libretrodroid {

class VideoLayout {
public:
    VideoLayout(bool bottomLeftOrigin, float rotation, Rect viewportRect, unsigned int viewportAlignment);

    void updateAspectRatio(float aspectRatio);

    void updateScreenSize(unsigned screenWidth, unsigned screenHeight);

    void updateViewportSize(Rect viewportRect);

    void updateViewportAlignment(unsigned int viewportAlignment);

    void updateRotation(float rotation);

    std::array<float, 12>& getForegroundVertices() { return foregroundVertices; }
    std::array<float, 12>& getBackgroundVertices() { return backgroundVertices; }
    std::array<float, 12>& getFramebufferVertices() { return framebufferVertices; }
    std::array<float, 12>& getTextureCoordinates() { return textureCoordinates; }
    std::array<float, 4>& getRelativeForegroundBounds() { return relativeForegroundBounds; }

    int getScreenWidth() { return screenWidth; }

    int getScreenHeight() { return screenHeight; }

    std::pair<float, float> getRelativePosition(float touchX, float touchY);

private:
    void updateBuffers();

    void updateForegroundVertices();

    void updateBackgroundVertices();

    void updateRelativeForegroundBounds();

private:
    std::array<float, 12> foregroundVertices = {
        -1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        -1.0F,

        +1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        +1.0F,
    };

    std::array<float, 12> textureCoordinates {
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

    std::array<float, 12> backgroundVertices = {
        -1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        -1.0F,

        +1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        +1.0F,
    };

    std::array<float, 12> framebufferVertices = {
        -1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        -1.0F,

        +1.0F,
        -1.0F,

        -1.0F,
        +1.0F,

        +1.0F,
        +1.0F,
    };

    std::array<float, 4> relativeForegroundBounds = {
        +0.0F,
        +0.0F,
        +1.0F,
        +1.0F,
    };

    bool bottomLeftOrigin = false;
    float rotation = 0.0F;
    float aspectRatio = 1;
    Rect viewportRect = Rect(0.0F, 0.0F, 1.0F, 1.0F);

    unsigned screenWidth = 0;
    unsigned screenHeight = 0;

    unsigned viewportAlignment = V_ALIGN_CENTER;
};

} // namespace libretrodroid

#endif //LIBRETRODROID_VIDEOLAYOUT_H
