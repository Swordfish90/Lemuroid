package com.codebutler.retrograde.lib.game.display.shaders

import com.codebutler.retrograde.lib.library.GameSystem
import org.intellij.lang.annotations.Language

sealed class RetroShader(
    @Language("glsl") val fragmentShader: String,
    @Language("glsl") val vertexShader: String,
    val interpolation: Interpolation
) {
    enum class  Interpolation {
        LINEAR,
        NEAREST
    }

    object Default : RetroShader(
        """
        precision mediump float;
        uniform sampler2D u_Texture;
        varying vec2 v_TexCoordinate;
        void main() {
          gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
        }
        """.trimIndent(),
        """
        attribute vec4 vPosition;
        uniform highp mat4 u_Transform;
        attribute mediump vec2 a_TexCoordinate;
        varying mediump vec2 v_TexCoordinate;
        void main() {
          v_TexCoordinate = a_TexCoordinate;
          gl_Position = u_Transform * vPosition;
        }
        """.trimIndent(),
        Interpolation.NEAREST
    )

    object CRT : RetroShader(
        """ 
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        #define HIGHP highp
        #else
        #define HIGHP mediump
        precision mediump float;
        #endif
      
        uniform HIGHP vec2 u_BitmapSize;
      
        uniform lowp sampler2D u_Texture;
        varying HIGHP vec2 v_TexCoordinate;
      
        #define INTENSITY 0.30
        #define BRIGHTBOOST 0.30
      
        void main() {
          lowp vec3 texel = texture2D(u_Texture, v_TexCoordinate).rgb;
          lowp vec3 pixelHigh = ((1.0 + BRIGHTBOOST) - (0.2 * texel)) * texel;
          lowp vec3 pixelLow  = ((1.0 - INTENSITY) + (0.1 * texel)) * texel;
      
          HIGHP vec2 rasterizationCoords = fract(v_TexCoordinate * u_BitmapSize);
        
          lowp float mask = 0.0;
          mask += smoothstep(0.0, 0.5, rasterizationCoords.y);
          mask -= smoothstep(0.5, 1.0, rasterizationCoords.y);
          
          gl_FragColor = vec4(mix(pixelLow, pixelHigh, mask), 1.0);
        }
        """.trimIndent(),
        Default.vertexShader,
        Interpolation.LINEAR
    )

    object LCD : RetroShader(
        """ 
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        #define HIGHP highp
        #else
        #define HIGHP mediump
        precision mediump float;
        #endif
      
        uniform HIGHP vec2 u_BitmapSize;
      
        uniform lowp sampler2D u_Texture;
        varying HIGHP vec2 v_TexCoordinate;
      
        #define INTENSITY 0.80
        #define BRIGHTBOOST 0.2
      
        void main() {
          lowp vec3 texel = texture2D(u_Texture, v_TexCoordinate).rgb;
          lowp vec3 pixelHigh = ((1.0 + BRIGHTBOOST) - (0.2 * texel)) * texel;
          lowp vec3 pixelLow  = ((1.0 - INTENSITY) + (0.1 * texel)) * texel;
      
          HIGHP vec2 coords = fract(v_TexCoordinate * u_BitmapSize) - vec2(0.5);
          coords = coords * coords;
        
          lowp float mask = 1.0 - coords.x - coords.y;

          gl_FragColor = vec4(mix(pixelLow, pixelHigh, mask), 1.0);
        }
        """.trimIndent(),
            Default.vertexShader,
            Interpolation.LINEAR
    )

    companion object {
        fun build(isEnabled: Boolean, systemId: String): RetroShader {
            return if (isEnabled) {
                buildFromSystemId(systemId)
            } else {
                Default
            }
        }

        private fun buildFromSystemId(systemId: String): RetroShader {
            return when (systemId) {
                in listOf(GameSystem.GBA_ID, GameSystem.GBC_ID, GameSystem.GB_ID) -> LCD
                else -> CRT
            }
        }
    }
}
