package com.kmortyk.game.ui.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.kmortyk.game.ui.GameSkin

/**
 * Transparent rectangle with rounded edges.
 */
class Backing(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        private val fill: Boolean = true,
        private val border: Boolean = true,
        private val mainColor: Color = Color.valueOf("#7a5643"),
        private val borderColor: Color = Color.BLACK,
        private val lineWidth: Float = 2.0f,
        val useBatchMatrix: Boolean = true
) : InterfaceElement(x, y, width, height) {
    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    var buf = Vector3()

    constructor(gameSkin: GameSkin, x: Float, y: Float, width: Float, height: Float) :
            this(x, y, width, height, mainColor = gameSkin.menuBacking, borderColor = gameSkin.menuBackingBorder)

    constructor(r: Rectangle, useBatchMatrix: Boolean=false, borderColor: Color=Color.RED, mainColor:Color=Color.valueOf("#7a5643"), fill: Boolean=false) :
            this(r.x, r.y, r.width, r.height, fill=fill, borderColor=borderColor, mainColor = mainColor, useBatchMatrix=useBatchMatrix) {
        buf.set(r.x, r.y, 0.0f)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()

        if(useBatchMatrix) {
            shapeRenderer.projectionMatrix = spriteBatch.projectionMatrix
            shapeRenderer.transformMatrix = spriteBatch.transformMatrix
        }

        if(fill) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = mainColor
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
            shapeRenderer.end()
        }

        if(border) {
            Gdx.gl.glLineWidth(lineWidth)

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = borderColor
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
            shapeRenderer.end()
        }

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