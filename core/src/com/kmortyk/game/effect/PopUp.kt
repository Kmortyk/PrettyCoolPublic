package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.screens.GameUI
import kotlin.math.ceil

class PopUpText(var x: Float, var y: Float, private val text: String, private val showBacking: Boolean = false) : ConstEffect() {
    companion object {
        private const val radius = 12f // rect corners round radius
        private const val alphaSpeed = 40f // fade alpha
        private const val heightSpeed = 10f // vertical moving
        private const val maxDelay = 4f // time before fade

        /**
         * Create new pop-up message effect and add it to scene.
         */
        fun addTo(game: PrettyCoolGame, x: Float, y: Float, text: String) {
            game.addEffect(PopUpText(x, y, text))
        }

        /**
         * If scene has pop-up message with the same text, simply move it,
         * otherwise it creates a new one.
         */
        fun uniqueAddTo(game: PrettyCoolGame, x: Float, y: Float, text: String) {
            for (i in game.effects.indices) {
                val e = game.effects[i]
                if (e is PopUpText) {
                    val popUp = e
                    // if matches
                    if (popUp.text == text) {
                        // recreate in pos (x, y)
                        popUp.x = x; popUp.y = y
                        popUp.create()
                        return
                    }
                }
            }

            // else
            game.addEffect(PopUpText(x, y, text))
        }
    }

    private lateinit var bounds: Rectangle
    private var alphaLevel = 255f
    private var heightOffset = 0f
    private var deltaDelay = 0f

    private val sren: ShapeRenderer = ShapeRenderer()
    private val textElement = TextElement(Assets.FontTimes12, text, 1000.0f, x, y, Color.WHITE, null)

    init {
        create()
    }

    override fun create(): ConstEffect {
        bounds = TextElement.textBounds(Assets.FontTimes12, text)

        alphaLevel = 255f
        heightOffset = 0f
        deltaDelay = 0f

        // ~ equals
        val letterWidth = bounds.width / text.length
        val w = bounds.width + GameUI.DefaultPadding*2
        val h: Float = bounds.height + GameUI.DefaultPadding*2

        bounds.set(x, y, w, h)

        val c = Color()
        Color.rgba8888ToColor(c, Color.rgba8888(191.0f/255.0f, 191.0f/255.0f, 191.0f/255.0f, 1.0f))
        textElement.color = c

        return this
    }

    override fun onExtend(delta: Float): Boolean {
        if (delta.let { deltaDelay += it; deltaDelay } > maxDelay) {
            if (alphaLevel > 0) alphaLevel -= delta * alphaSpeed
            heightOffset = delta * heightSpeed
        }
        return alphaLevel > 0
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        bounds.y += heightOffset

        if(showBacking) {
            spriteBatch.end()
            sren.projectionMatrix = spriteBatch.projectionMatrix
            sren.begin(ShapeRenderer.ShapeType.Filled)
            sren.setColor(0.0f, 0.0f, 0.0f, alphaLevel / 255.0f)
            sren.color = Color.BLACK
            roundedRect(bounds.x, bounds.y, bounds.width, bounds.height, radius)
            sren.end()
            spriteBatch.begin()
        }

        textElement.bounds.x = bounds.x + GameUI.DefaultPadding
        textElement.bounds.y = ceil(bounds.y + GameUI.DefaultPadding)
        textElement.draw(assetManager, spriteBatch)
    }

    private fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        // Central rectangle
        sren.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius)

        // Four side rectangles, in clockwise order
        sren.rect(x + radius, y, width - 2 * radius, radius)
        //sren.rect(x + width - radius, y + radius, radius, height - 2 * radius)
        sren.rect(x + radius, y + height - radius, width - 2 * radius, radius)
        //sren.rect(x, y + radius, radius, height - 2 * radius)

        // Four arches, clockwise too
        sren.arc(x + radius, y + radius, radius, 180f, 90f)
        sren.arc(x + width - radius, y + radius, radius, 270f, 90f)
        sren.arc(x + width - radius, y + height - radius, radius, 0f, 90f)
        sren.arc(x + radius, y + height - radius, radius, 90f, 90f)
    }
}