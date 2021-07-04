package com.kmortyk.game.ui.editor

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.kmortyk.game.ui.element.InterfaceElement


open class EditorPaneElement(public val assetID: Int, public val assetName: String, val textureReg: TextureRegion, x: Float, y: Float) :
        InterfaceElement(x, y, textureReg.regionWidth.toFloat() + ExtendTexSize * 2, textureReg.regionHeight.toFloat() + ExtendTexSize * 2) {
    companion object {
        const val ExtendTexSize = 4
    }

    private val actualTex: Texture

    init {
        val fb = FrameBuffer(
                Pixmap.Format.RGBA8888,
                textureReg.regionWidth + ExtendTexSize * 2,
                textureReg.regionHeight + ExtendTexSize * 2,
                false)
        val m = Matrix4()
        m.setToOrtho2D(0f, 0f, fb.width.toFloat(), fb.height.toFloat())

        val sp = SpriteBatch(1)
        sp.projectionMatrix = m

        fb.begin()
        sp.begin()
        textureReg.flip(false, true)
        sp.draw(textureReg, ExtendTexSize.toFloat(), ExtendTexSize.toFloat())
        textureReg.flip(false, true) // return back
        sp.end()
        fb.end()

        actualTex = fb.colorBufferTexture
        actualTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }

    private var shaderProgram: ShaderProgram? = null
    private var isSelected: Boolean = false

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(isSelected && shaderProgram != null) {
            spriteBatch.end()

            shaderProgram!!.begin()
            //shaderProgram!!.setUniformf("u_viewportInverse", Vector2(1f / texture.regionWidth, 1f / texture.regionHeight))
            //shaderProgram!!.setUniformf("u_offset", 0.01f)
            //shaderProgram!!.setUniformf("u_step", Math.min(1f, texture.regionWidth / 70f))
            //val c = Color.valueOf("#f5374d")
            //shaderProgram!!.setUniformf("u_color", Vector3(c.r, c.g, c.b))
            shaderProgram!!.end()

            spriteBatch.shader = shaderProgram
            spriteBatch.begin()
            spriteBatch.draw(actualTex, bounds.x, bounds.y)

            spriteBatch.end()
            spriteBatch.shader = null
            spriteBatch.begin()
            //spriteBatch.draw(texture, bounds.x, bounds.y)
        } else {
            spriteBatch.draw(actualTex, bounds.x, bounds.y)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean { return true }

    fun setShaderProgram(shaderProgram: ShaderProgram) {
        this.shaderProgram = shaderProgram
    }

    fun setSelected(selected: Boolean) {
        isSelected = selected
    }
}

class EditorPaneElementTexture(assetID: Int, assetName: String, texture: Texture, x: Float, y: Float) :
        EditorPaneElement(assetID, assetName, TextureRegion(texture), x, y)