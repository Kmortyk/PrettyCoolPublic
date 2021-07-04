package com.kmortyk.game.effect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.kmortyk.game.LogColors
import com.kmortyk.game.log
import java.time.Duration
import java.util.concurrent.TimeUnit

class GradualRainEffect : ConstEffect() {
    companion object {
        private val raindropColor = Color.valueOf("#7e7b74").mul(1f, 1f, 1f, 0.5f)

        const val maxCount = 128
        const val velocity = 3
    }

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val raindrops: MutableList<RainEffect.Raindrop> = mutableListOf()

    private var timeDelay: Long = TimeUnit.MILLISECONDS.toMillis(800)
    private var prevTime = System.currentTimeMillis()

    override fun create() : ConstEffect {
        raindrops.clear()
        return this
    }

    override fun onExtend(delta: Float): Boolean {
        return raindrops.size < maxCount
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()
        shapeRenderer.begin(ShapeType.Filled)

        increaseRaindrops()

        for(raindrop in raindrops) {
            raindrop.update()

            if(raindrop.isOutOfBounds()) {
                raindrop.x = (Math.random() * Gdx.graphics.width).toFloat()
                raindrop.y = Gdx.graphics.height.toFloat() + (Math.random() * Gdx.graphics.height*0.3f).toFloat()
            }

            shapeRenderer.rectLine(raindrop.x, raindrop.y, raindrop.x, (raindrop.y + raindrop.height), 1f, raindropColor, raindropColor)
        }

        shapeRenderer.end()
        spriteBatch.begin()
    }

    private fun increaseRaindrops() {
        if(System.currentTimeMillis() - prevTime > timeDelay && raindrops.size < maxCount) {
            prevTime = System.currentTimeMillis()

            if(raindrops.size > maxCount*0.5f) {
                timeDelay /= 2
            }

            for(i in 0 .. velocity) {
                val x = (raindrops.size - 1) * RainEffect.raindropOffset
                val randY = (Math.random() * Gdx.graphics.height).toFloat()
                val randH = ((Math.random() * (RainEffect.raindropMaxHeight - RainEffect.raindropMinHeight)) + RainEffect.raindropMinHeight).toFloat()

                raindrops.add(RainEffect.Raindrop(x, randY, randH))
            }
        }
    }
}