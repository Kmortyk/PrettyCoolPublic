package com.kmortyk.game.effect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.person.Direction
import com.kmortyk.game.ui.element.TextElement

class MapNameEffect(game: PrettyCoolGame, name: String) : Effect() {
    companion object {
        private const val alphaSpeed = 0.4f
        private const val minAlpha = 0.05f
        private const val waitTime = 1f
    }

    enum class State {
        Appear,
        Wait,
        Disappear
    }

    private val font = Assets.FontKurale
    private val color = Color.WHITE
    private val viewportWidth = game.gameDrawFun().viewPortWidth()
    private val viewportHeight = game.gameDrawFun().viewPortHeight()
    private val gameUISpriteBatch = game.gameDrawFun().gameUiSpriteBatch

    private val textElement = TextElement(
        font, name, 1000.0f,
        (viewportWidth - TextElement.wordWidth(font, name))*0.35f,
        viewportHeight*0.17f,
        color, null, padding = 0.0f)

    private var alphaLevel = minAlpha
    private var state = State.Appear
    private var waitedTime = 0.0f

    override fun onExtend(delta: Float): Boolean {
        when(state) {
            State.Appear -> {
                if(alphaLevel < 1.0f) {
                    alphaLevel += delta * alphaSpeed
                    if(alphaLevel > 1) alphaLevel = 1.0f
                }
                else { state = State.Wait }
            }

            State.Wait -> {
                waitedTime += Gdx.graphics.deltaTime
                if(waitedTime >= waitTime) state = State.Disappear
            }

            State.Disappear -> {
                if (alphaLevel > minAlpha)
                    alphaLevel -= delta * alphaSpeed
            }
        }

        return state != State.Disappear || alphaLevel > minAlpha
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.end()
        gameUISpriteBatch.begin()

        color.a = alphaLevel
        textElement.draw(assetManager, gameUISpriteBatch)
        color.a = 1.0f

        gameUISpriteBatch.end()
        spriteBatch.begin()
    }
}