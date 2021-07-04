package com.kmortyk.game.effect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.kmortyk.game.Assets
import com.kmortyk.game.LogColors
import com.kmortyk.game.log

class SnowEffect : ConstEffect() {
    data class Snowflake(var x: Float, var y: Float) {
        fun update() {
            y -= speed
        }

        fun isOutOfBounds() = y + 9 <= 0
    }

    companion object {
        const val raindropOffset = 20.0f
        private const val speed = 15f
    }

    private val raindrops: MutableList<Snowflake> = mutableListOf()

    override fun create() : ConstEffect {
        raindrops.clear()

        val count = (Gdx.graphics.width / raindropOffset).toInt()
        for(i in 0 .. count) {
            val x = i * raindropOffset
            val randY = (Math.random() * Gdx.graphics.height).toFloat()

            raindrops.add(Snowflake(x, randY))
        }

        log.info("raindrop effect: raindrops_count=${LogColors.BLUE}$count${LogColors.RESET}")
        return this
    }

    override fun onExtend(delta: Float): Boolean { return true /*TODO endless effect*/ }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        for(snowflake in raindrops) {
            snowflake.update()
            if(snowflake.isOutOfBounds()) {
                snowflake.x = (Math.random() * Gdx.graphics.width).toFloat()
                snowflake.y = Gdx.graphics.height.toFloat() + (Math.random() * Gdx.graphics.height*0.3f).toFloat()
            }

            spriteBatch.draw(assetManager[Assets["eff_snowflake"]], snowflake.x, snowflake.y)
        }
    }
}