package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

class TextElement(private val btmFont: BitmapFont,
                  val _text: String,
                  val maxWidth: Float,
                  val left: Float,
                  val bottom: Float,
                  var color: Color,
                  val onTextTouch: Runnable?,
                  val positivePadding: Boolean = true,
                  val padding: Float = 10.0f) :
        InterfaceElement(left, bottom, 0.0f, 0.0f) {

    var text: String = _text
    set(value) {
        field = value
        updateBounds()
    }

    companion object {
        fun lineHeight(btmFont: BitmapFont, maxWidth: Float = Float.MAX_VALUE) : Float {
            val layout = GlyphLayout(btmFont, "Hello", Color.BLACK, maxWidth, Align.left, true)
            return layout.height
        }

        fun wordWidth(btmFont: BitmapFont, text: String, maxWidth: Float = Float.MAX_VALUE) : Float {
            val layout = GlyphLayout(btmFont, text, Color.BLACK, maxWidth, Align.left, true)
            return layout.width
        }

        fun textBounds(btmFont: BitmapFont, text: String, maxWidth: Float = Float.MAX_VALUE) : Rectangle {
            val layout = GlyphLayout(btmFont, text, Color.BLACK, maxWidth, Align.left, true)
            return Rectangle(0.0f, 0.0f, layout.width, layout.height)
        }
    }

    init {
        updateBounds()
        updatePadding(0.0f, padding)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        btmFont.color = color
        btmFont.draw(spriteBatch, text, bounds.x, bounds.y + bounds.height, maxWidth - padding, Align.left, true)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if(bounds.contains(x, y) && onTextTouch != null) {
            onTextTouch.run()
            return true
        }
        return false // not consume
    }

    fun updateTextSavePosition(newText: String) {
        val x = bounds.x
        val y = bounds.y

        text = newText
        bounds.x = x
        bounds.y = y
    }

    private fun updateBounds() {
        val layout = GlyphLayout(btmFont, text, Color.BLACK, maxWidth - padding, Align.left, true)

        bounds.x = left
        bounds.y = bottom
        bounds.width = layout.width
        bounds.height = layout.height

        if(bounds.y == bottom)
            bounds.y = bottom - bounds.height
    }

    private fun updatePadding(oldPadding: Float, newPadding: Float) {
        val c = if(positivePadding) 1 else -1
        bounds.x = bounds.x + c*(newPadding - oldPadding)
        bounds.y = bounds.y + c*(oldPadding - newPadding)
    }
}