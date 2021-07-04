package com.kmortyk.game.ui.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class LineElement(private val x1: Float, private val y1: Float, private val x2: Float, private val y2: Float,
                  private val lineWidth: Float = 2.0f) : InterfaceElement() {
    companion object {
        val mainColor: Color = Color.BLACK
    }

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    init {
        shapeRenderer.color = mainColor
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()

        shapeRenderer.projectionMatrix = spriteBatch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rectLine(x1, y1, x2, y2, lineWidth)
        shapeRenderer.end()

        spriteBatch.begin()
    }

    override fun onTouch(x: Float, y: Float): Boolean = false // never touchable
}