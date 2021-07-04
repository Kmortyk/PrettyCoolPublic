package com.kmortyk.game.ui.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack

/**
 * Transparent rectangle with rounded edges.
 */
class TextureDescBacking(var texture: AssetDescriptor<Texture>, var x: Float, var y: Float) : InterfaceElement(x, y, 0.0f, 0.0f) {
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val cam: Camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

    private var scale: Float = 1.0f

    init {
        cam.translate(cam.viewportWidth*0.5f, cam.viewportHeight*0.5f, 0.0f)
        cam.update()
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val tex = assetManager[texture]
        bounds.set(x, y, tex.width.toFloat(), tex.height.toFloat())
        scaleBounds(scale)
//        val scissors = Rectangle()
//        ScissorStack.calculateScissors(cam,0.0f, 0.0f, cam.viewportWidth, cam.viewportHeight, spriteBatch.transformMatrix, bounds, scissors)
//
//        spriteBatch.flush() // save drawed image
//
//        if(!ScissorStack.pushScissors(scissors)) {
//            println("[ERROR] can't push mini map scissors")
//        }

        spriteBatch.draw(tex, x, y)
//        spriteBatch.flush()
//        ScissorStack.popScissors()
//
//        Gdx.gl.glLineWidth(1.0f)

//        spriteBatch.end()
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
//        shapeRenderer.color = Color.RED
//        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
//        shapeRenderer.end()
//        spriteBatch.begin()
    }

    override fun onTouch(x: Float, y: Float): Boolean { return true }

    fun scale(scale: Float) {
        this.scale = scale
    }
}