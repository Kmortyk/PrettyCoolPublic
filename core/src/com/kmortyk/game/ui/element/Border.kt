package com.kmortyk.game.ui.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3

/**
 * Transparent rectangle with rounded edges.
 */
class Border(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        private val borderColor: Color = Color.RED,
        private val lineWidth: Float = 2.0f,
        val useBatchMatrix: Boolean = false
) : InterfaceElement(x, y, width, height) {
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    constructor(r: Rectangle, useBatchMatrix: Boolean=false, borderColor : Color=Color.RED) :
            this(r.x, r.y, r.width, r.height, borderColor=borderColor, useBatchMatrix=useBatchMatrix) {
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()

        if(useBatchMatrix) {
            shapeRenderer.projectionMatrix = spriteBatch.projectionMatrix
        }

        Gdx.gl.glLineWidth(lineWidth)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = borderColor
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
        shapeRenderer.end()

        spriteBatch.begin()
    }

    override fun onTouch(x: Float, y: Float): Boolean { return true }

    fun scale(sx: Float, sy: Float) {
        val dw: Float = bounds.width * (sx - 1)
        val dh: Float = bounds.height * (sy - 1)
        // width
        bounds.x -= dw * 0.5f
        bounds.width += dw * 0.5f
        // height
        bounds.y -= dh * 0.5f
        bounds.height += dh * 0.5f
    }
}