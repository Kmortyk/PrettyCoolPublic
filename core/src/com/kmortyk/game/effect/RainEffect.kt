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

class RainEffect : ConstEffect() {
    data class Raindrop(var x: Float, var y: Float, var height: Float) {
        fun update() { y -= speed }
        fun isOutOfBounds() = y + height <= 0
    }

    companion object {
        // const val raindropOffset = 5.0f
        const val raindropOffset = 20.0f

        const val raindropMinHeight = 10
        const val raindropMaxHeight = 100

        // val color = "#ecd9be"
        private val raindropColor = Color.valueOf("#7e7b74").mul(1f, 1f, 1f, 0.5f)
        private const val speed = 35f
    }

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val raindrops: MutableList<Raindrop> = mutableListOf()

    override fun create() : ConstEffect {
        raindrops.clear()

        val count = (Gdx.graphics.width / raindropOffset).toInt()
        for(i in 0 .. count) {
            val x = i * raindropOffset
            val randY = (Math.random() * Gdx.graphics.height).toFloat()
            val randH = ((Math.random() * (raindropMaxHeight - raindropMinHeight)) + raindropMinHeight).toFloat()

            raindrops.add(Raindrop(x, randY, randH))
        }

        log.info("raindrop effect: raindrops_count=${LogColors.BLUE}$count${LogColors.RESET}")
        return this
    }

    override fun onExtend(delta: Float): Boolean {
        return true // TODO endless effect
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()

        //Gdx.gl.glEnable(GL20.GL_BLEND)
        //Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeType.Filled)

        for(raindrop in raindrops) {
            raindrop.update()
            if(raindrop.isOutOfBounds()) {
                raindrop.x = (Math.random() * Gdx.graphics.width).toFloat()
                raindrop.y = Gdx.graphics.height.toFloat() + (Math.random() * Gdx.graphics.height*0.3f).toFloat()
            }
            shapeRenderer.rectLine(
                    raindrop.x, raindrop.y,
                    raindrop.x, (raindrop.y + raindrop.height),
                    1f, raindropColor, raindropColor)
        }

        shapeRenderer.end()
        //Gdx.gl.glDisable(GL20.GL_BLEND)

        spriteBatch.begin()
    }
}