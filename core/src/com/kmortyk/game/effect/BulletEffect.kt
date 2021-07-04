package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.GridPosition
import com.kmortyk.game.hexagon.Hexagon

class BulletEffect(private val bulletTex: Texture, val startPos: GridPosition, targetPos: GridPosition) : Effect() {
    companion object {
        const val Steps = 20
        const val Err = 0.001f
    }

    private val to: Vector2 = Hexagon(Assets.NullHex, targetPos.row, targetPos.col).center()
    private val from: Vector2 = Hexagon(Assets.NullHex, startPos.row, startPos.col).center()

    private val pos: Vector2 = Vector2()
    private var curStep = 0

    private var lastDst: Float = Float.MAX_VALUE

    override fun onExtend(delta: Float): Boolean {
        curStep++

        val t = curStep.toFloat() / Steps

        pos.x = Interpolation.linear.apply(from.x, to.x, t)
        pos.y = Interpolation.linear.apply(from.y, to.y, t)

        val dst = Vector2.dst(pos.x, pos.y, to.x, to.y)

        // if the distance change was too small, complete
        return if (lastDst - dst < Err) {
            false
        } else {
            // save dst
            lastDst = dst
            true
        }
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(bulletTex, pos.x, pos.y)
    }
}