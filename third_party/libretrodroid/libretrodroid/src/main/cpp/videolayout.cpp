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

#include "videolayout.h"
#include "log.h"

namespace libretrodroid {

VideoLayout::VideoLayout(bool bottomLeftOrigin, float rotation, Rect viewportRect, unsigned int viewportAlignment) :
    bottomLeftOrigin(bottomLeftOrigin),
    rotation(rotation),
    viewportRect(viewportRect),
    viewportAlignment(viewportAlignment)
{
    updateBuffers();
}

void VideoLayout::updateBuffers() {
    updateForegroundVertices();
    updateBackgroundVertices();
    updateRelativeForegroundBounds();
}

void VideoLayout::updateForegroundVertices() {
    float cosTheta = cos(-rotation);
    float sinTheta = sin(-rotation);

    LOGD(
        "Computing foreground vertices from screen (%d x %d), aspect ratio (%f) with rotation (%f radians)",
        screenWidth,
        screenHeight,
        aspectRatio,
        rotation
    );

    float screenW = screenWidth * viewportRect.getWidth();
    float screenH = screenHeight * viewportRect.getHeight();
    float screenAspect = screenW / screenH;
    float contentAspect = aspectRatio;

    float scaleX = viewportRect.getWidth();
    float scaleY = viewportRect.getHeight();
    if (contentAspect > screenAspect) {
        scaleY *= screenAspect / contentAspect;
    } else {
        scaleX *= contentAspect / screenAspect;
    }

    float viewportXOffset = (viewportRect.getX() * 2.0f) - (1.0F - viewportRect.getWidth());
    float viewportYOffset = (viewportRect.getY() * 2.0f) - (1.0F - viewportRect.getHeight());

    float emptyVerticalSpace = viewportRect.getHeight() - scaleY;
    switch (viewportAlignment) {
        case V_ALIGN_TOP:
            viewportYOffset -= emptyVerticalSpace;
            break;
        case V_ALIGN_BOTTOM:
            viewportYOffset += emptyVerticalSpace;
            break;
        case V_ALIGN_CENTER:
            // do nothing
            break;
    }

    float factorX = scaleX / (scaleX * fabs(cosTheta) + scaleY * fabs(sinTheta));
    float factorY = scaleY / (scaleX * fabs(sinTheta) + scaleY * fabs(cosTheta));

    float uv[4][2] = {
        { -1.0F, bottomLeftOrigin ? -1.0F : +1.0F },
        { -1.0F, bottomLeftOrigin ? +1.0F : -1.0F },
        { +1.0F, bottomLeftOrigin ? -1.0F : +1.0F },
        { +1.0F, bottomLeftOrigin ? +1.0F : -1.0F }
    };

    float rotatedQuad[4][2];
    for (int i = 0; i < 4; i++) {
        float u = uv[i][0];
        float v = uv[i][1];

        float origX = u * scaleX;
        float origY = v * scaleY;

        float rawX = origX * cosTheta - origY * sinTheta;
        float rawY = origX * sinTheta + origY * cosTheta;

        float finalX = rawX * factorX + viewportXOffset;
        float finalY = rawY * factorY - viewportYOffset;

        rotatedQuad[i][0] = finalX;
        rotatedQuad[i][1] = finalY;
    }

    foregroundVertices[0] = rotatedQuad[0][0];
    foregroundVertices[1] = rotatedQuad[0][1];

    foregroundVertices[2] = rotatedQuad[1][0];
    foregroundVertices[3] = rotatedQuad[1][1];

    foregroundVertices[4] = rotatedQuad[2][0];
    foregroundVertices[5] = rotatedQuad[2][1];

    foregroundVertices[6] = rotatedQuad[2][0];
    foregroundVertices[7] = rotatedQuad[2][1];

    foregroundVertices[8] = rotatedQuad[1][0];
    foregroundVertices[9] = rotatedQuad[1][1];

    foregroundVertices[10] = rotatedQuad[3][0];
    foregroundVertices[11] = rotatedQuad[3][1];
}

void VideoLayout::updateBackgroundVertices() {
    float cosTheta = cos(-rotation);
    float sinTheta = sin(-rotation);

    float uv[4][2] = {
        { -1.0F, bottomLeftOrigin ? -1.0F : +1.0F },
        { -1.0F, bottomLeftOrigin ? +1.0F : -1.0F },
        { +1.0F, bottomLeftOrigin ? -1.0F : +1.0F },
        { +1.0F, bottomLeftOrigin ? +1.0F : -1.0F }
    };

    float rotatedQuad[4][2];
    for (int i = 0; i < 4; i++) {
        float u = uv[i][0];
        float v = uv[i][1];

        rotatedQuad[i][0] = u * cosTheta - v * sinTheta;
        rotatedQuad[i][1] = u * sinTheta + v * cosTheta;
    }

    backgroundVertices[0] = rotatedQuad[0][0];
    backgroundVertices[1] = rotatedQuad[0][1];

    backgroundVertices[2] = rotatedQuad[1][0];
    backgroundVertices[3] = rotatedQuad[1][1];

    backgroundVertices[4] = rotatedQuad[2][0];
    backgroundVertices[5] = rotatedQuad[2][1];

    backgroundVertices[6] = rotatedQuad[2][0];
    backgroundVertices[7] = rotatedQuad[2][1];

    backgroundVertices[8] = rotatedQuad[1][0];
    backgroundVertices[9] = rotatedQuad[1][1];

    backgroundVertices[10] = rotatedQuad[3][0];
    backgroundVertices[11] = rotatedQuad[3][1];
}

void libretrodroid::VideoLayout::updateAspectRatio(float aspectRatio) {
    LOGD("Updated aspect ratio to : %f", aspectRatio);

    this->aspectRatio = aspectRatio;
    updateBuffers();
}

void libretrodroid::VideoLayout::updateScreenSize(unsigned int width,unsigned int height) {
    LOGD("Updating screen size: %d x %d", screenWidth, screenHeight);

    this->screenWidth = width;
    this->screenHeight = height;
    updateBuffers();
}

void libretrodroid::VideoLayout::updateViewportSize(Rect viewport) {
    LOGD(
        "Updating viewport size: (%f, %f, %f, %f)",
        viewport.getX(),
        viewport.getY(),
        viewport.getWidth(),
        viewport.getHeight()
    );

    this->viewportRect = viewport;
    updateBuffers();
}

void VideoLayout::updateViewportAlignment(unsigned int viewportAlignment) {
    LOGD("Updated viewport alignment to : %d", viewportAlignment);

    this->viewportAlignment = viewportAlignment;
    updateBuffers();
}

void VideoLayout::updateRotation(float rotation) {
    LOGD("Updated rotation to : %f", rotation);

    this->rotation = rotation;
    updateBuffers();
}

std::pair<float, float> VideoLayout::getRelativePosition(float touchX, float touchY) {
    float xMin = std::numeric_limits<float>::max();
    float xMax = std::numeric_limits<float>::lowest();
    float yMin = std::numeric_limits<float>::max();
    float yMax = std::numeric_limits<float>::lowest();

    for (size_t i = 0; i < foregroundVertices.size(); i += 2) {
        float x = foregroundVertices[i];
        float y = foregroundVertices[i + 1];
        xMin = std::min(xMin, x);
        xMax = std::max(xMax, x);
        yMin = std::min(yMin, -y);
        yMax = std::max(yMax, -y);
    }

    if (touchX < xMin || touchX > xMax || touchY < yMin || touchY > yMax) {
        return {-10.0f, -10.0f};
    }

    float relativeX = (touchX - xMin) / (xMax - xMin);
    float relativeY = (touchY - yMin) / (yMax - yMin);

    LOGD("Computed relative touch position: %.2f, %.2f", relativeX, relativeY);

    return {relativeX, relativeY};
}

void VideoLayout::updateRelativeForegroundBounds() {
    float xMin = std::numeric_limits<float>::max();
    float xMax = std::numeric_limits<float>::lowest();
    float yMin = std::numeric_limits<float>::max();
    float yMax = std::numeric_limits<float>::lowest();

    for (size_t i = 2; i < foregroundVertices.size(); i += 2) {
        xMin = std::min(xMin, foregroundVertices[i]);
        xMax = std::max(xMax, foregroundVertices[i]);
        yMin = std::min(yMin, (bottomLeftOrigin ? 1.0F : -1.0F) * foregroundVertices[i + 1]);
        yMax = std::max(yMax, (bottomLeftOrigin ? 1.0F : -1.0F) * foregroundVertices[i + 1]);
    }

    relativeForegroundBounds[0] = (xMin + 1.0F) / 2.0F;
    relativeForegroundBounds[1] = (yMin + 1.0F) / 2.0F;
    relativeForegroundBounds[2] = (xMax + 1.0F) / 2.0F;
    relativeForegroundBounds[3] = (yMax + 1.0F) / 2.0F;

    LOGD(
        "Computed relative foreground bounds: (%.2f, %.2f, %.2f, %.2f)",
        relativeForegroundBounds[0],
        relativeForegroundBounds[1],
        relativeForegroundBounds[2],
        relativeForegroundBounds[3]
    );
}

}