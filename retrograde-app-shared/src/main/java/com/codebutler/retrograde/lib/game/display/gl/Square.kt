/*
 * Square.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.retrograde.lib.game.display.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glDisableVertexAttribArray
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLUtils
import android.opengl.Matrix
import org.intellij.lang.annotations.Language
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

@Suppress("no-wildcard-imports")
class Square {
    @Language("glsl")
    private val vertexShaderCode = """
      attribute vec4 vPosition;
      uniform highp mat4 u_Transform;
      attribute vec2 a_TexCoordinate;
      varying vec2 v_TexCoordinate;
      void main() {
        v_TexCoordinate = a_TexCoordinate;
        gl_Position = u_Transform * vPosition;
      }
      """.trimIndent()

    @Language("glsl")
    private val fragmentShaderCode = """
      precision mediump float;
      uniform sampler2D u_Texture;
      varying vec2 v_TexCoordinate;
      void main() {
        gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
      }
      """.trimIndent()

    private val program: Int
    private val bytesPerFloat: Int = 4
    private val vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private val uvBuffer: FloatBuffer
    private val transform = FloatArray(16)

    private val squareCoords = floatArrayOf(
            -1.0f, 1.0f, 0.0f, // top left
            -1.0f, -1.0f, 0.0f, // bottom left
            1.0f, -1.0f, 0.0f, // bottom right
            1.0f, 1.0f, 0.0f) // top right

    private val squareUvCoords = floatArrayOf(
            0.0f, 0.0f, // top left
            0.0f, 1.0f, // bottom left
            1.0f, 1.0f, // bottom right
            1.0f, 0.0f) // top right

    // number of coordinates per vertex in this array
    private val COORDS_PER_VERTEX = 3
    private val vertexCount: Int = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    var bitmap: Bitmap? = null

    private var textureOffset = -1
    private val texturePool = intArrayOf(-1, -1, -1)

    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0

    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoords)
        vertexBuffer.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        uvBuffer = ByteBuffer.allocateDirect(squareUvCoords.size * bytesPerFloat)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        uvBuffer.put(squareUvCoords).position(0)

        val vertexShader = GlRenderer2d.loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GlRenderer2d.loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        program = GLES20.glCreateProgram()

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader)

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader)

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program)
    }

    fun setGlBounds(width: Int, height: Int) {
        if (width == surfaceWidth && height == surfaceHeight) return
        surfaceWidth = width
        surfaceHeight = height
        updateTransform()
    }

    fun setBitmapBounds(width: Int, height: Int) {
        if (width == bitmapWidth && height == bitmapHeight) return
        bitmapWidth = width
        bitmapHeight = height
        updateTransform()
    }

    private fun updateTransform() {
        if (bitmapWidth == 0 || bitmapHeight == 0 || surfaceWidth == 0 || surfaceHeight == 0) return

        val surfaceAspect = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        val bitmapAspect = bitmapWidth.toFloat() / bitmapHeight.toFloat()

        if (surfaceAspect > bitmapAspect) {
            // image is taller
            Matrix.orthoM(transform, 0,
                    -surfaceAspect / bitmapAspect, surfaceAspect / bitmapAspect,
                    -1.0f, 1.0f,
                    -1.0f, 1.0f)
        } else {
            // image is wider
            Matrix.orthoM(transform, 0,
                    -1.0f, 1.0f,
                    -bitmapAspect / surfaceAspect, bitmapAspect / surfaceAspect,
                    -1.0f, 1.0f)
        }
    }

    fun draw() {
        val b = bitmap ?: return

        setBitmapBounds(b.width, b.height)

        // Add program to OpenGL ES environment
        glUseProgram(program)

        // bind the texture
        val textureLocation = bindTexture(b)
        glActiveTexture(0)
        glBindTexture(GL_TEXTURE_2D, textureLocation)
        glUniform1i(glGetUniformLocation(program, "u_Texture"), 0)

        // update the transform
        val transformLocation = glGetUniformLocation(program, "u_Transform")
        glUniformMatrix4fv(transformLocation, 1, false, transform, 0)

        // getProvider handle to vertex shader's vPosition member
        val positionHandle = glGetAttribLocation(program, "vPosition")

        // Enable a handle to the triangle vertices
        glEnableVertexAttribArray(positionHandle)

        // Prepare the triangle coordinate data
        glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // Pass in the texture coordinate information
        val textureCoordHandle = glGetAttribLocation(program, "a_TexCoordinate")
        uvBuffer.position(0)
        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, false,
                0, uvBuffer)

        glEnableVertexAttribArray(textureCoordHandle)

        // Draw the square
        glDrawElements(
                GL_TRIANGLES, drawOrder.size,
                GL_UNSIGNED_SHORT, drawListBuffer)

        // Disable vertex array
        glDisableVertexAttribArray(positionHandle)
    }

    private fun bindTexture(bitmap: Bitmap): Int {
        textureOffset = (textureOffset + 1) % texturePool.size

        if (texturePool[textureOffset] == -1) {
            glGenTextures(1, texturePool, textureOffset)
        }

        if (texturePool[textureOffset] != 0) {
            // Bind to the texture in OpenGL
            glBindTexture(GL_TEXTURE_2D, texturePool[textureOffset])

            // Set filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        }

        if (texturePool[textureOffset] == 0) {
            throw RuntimeException("Error loading texture.")
        }

        return texturePool[textureOffset]
    }
}
