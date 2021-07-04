package com.kmortyk.game.ui.element.special

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.kmortyk.game.ui.element.InterfaceElement

public interface ValueReceiver<T> {
    fun receiveValue() : T
}

public class TextWatcherElement<T>(
    private val btmFont: BitmapFont,
    private val left: Float,
    private val bottom: Float,
    private var color: Color,
    private val valueReceiver: ValueReceiver<T>) :

    InterfaceElement(left, bottom, 0.0f, 0.0f) {

    private var valueCache: T = valueReceiver.receiveValue()
    private var valueCacheStr: String = "$valueCache"

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        btmFont.color = color

        val value = valueReceiver.receiveValue()
        if(value != valueCache) {
            valueCache = value
            valueCacheStr = "$valueCache"
            updateBounds()
        }

        if(valueCache != null) {
            btmFont.draw(spriteBatch, valueCacheStr, bounds.x, bounds.y + bounds.height, 1000.0f, Align.left, true)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        return false // TODO ?
    }

    private fun updateBounds() {
//        val x = bounds.x
//        val y = bounds.y
//
//        val layout = GlyphLayout(btmFont, valueCacheStr, Color.BLACK, 1000.0f, Align.left, true)
//
//        bounds.x = left
//        bounds.y = bottom
//        bounds.width = layout.width
//        bounds.height = layout.height
//
//        if(bounds.y == bottom)
//            bounds.y = bottom - bounds.height
//
//        bounds.x = x
//        bounds.y = y
    }
}