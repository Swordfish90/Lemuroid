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

#ifndef LIBRETRODROID_SHADERMANAGER_H
#define LIBRETRODROID_SHADERMANAGER_H

#include <string>
#include <unordered_map>
#include <vector>

namespace libretrodroid {

class ShaderManager {
public:
    struct Pass {
        std::string vertex;
        std::string fragment;
        bool linear;
        float scale;

        bool operator==(const ShaderManager::Pass &other) const;
    };

    struct Chain {
        std::vector<Pass> passes;
        bool linearTexture;

        bool operator==(const ShaderManager::Chain &other) const;
        bool operator!=(const ShaderManager::Chain &other) const;
    };

    enum class Type {
        SHADER_DEFAULT = 0,
        SHADER_CRT = 1,
        SHADER_LCD = 2,
        SHADER_SHARP = 3,
        SHADER_UPSCALE_CUT = 4,
        SHADER_UPSCALE_CUT2 = 5,
        SHADER_UPSCALE_CUT3 = 6,
    };

    struct Config {
        Type type;
        std::unordered_map<std::string, std::string> params;

        inline bool operator==(const Config& other) {
            return type == other.type && params == other.params;
        }
    };

private:
    static const std::string defaultShaderVertex;

    static const std::string defaultShaderFragment;
    static const std::string defaultSharpFragment;
    static const std::string crtShaderFragment;
    static const std::string lcdShaderFragment;

    static const std::unordered_map<std::string, std::string> cutUpscaleParams;
    static const std::string cutUpscaleVertex;
    static const std::string cutUpscaleFragment;

    static const std::unordered_map<std::string, std::string> cut2UpscaleParams;
    static const std::string cut2UpscalePass0Vertex;
    static const std::string cut2UpscalePass0Fragment;
    static const std::string cut2UpscalePass1Vertex;
    static const std::string cut2UpscalePass1Fragment;

    static const std::unordered_map<std::string, std::string> cut3UpscaleParams;
    static const std::string cut3UpscalePass0Vertex;
    static const std::string cut3UpscalePass0Fragment;
    static const std::string cut3UpscalePass1Vertex;
    static const std::string cut3UpscalePass1Fragment;
    static const std::string cut3UpscalePass2Vertex;
    static const std::string cut3UpscalePass2Fragment;

private:
    static std::string buildDefines(
        std::unordered_map<std::string, std::string> baseParams,
        std::unordered_map<std::string, std::string> customParams
    );

public:
    static Chain getShader(const Config& config);
};

}

#endif //LIBRETRODROID_SHADERMANAGER_H
