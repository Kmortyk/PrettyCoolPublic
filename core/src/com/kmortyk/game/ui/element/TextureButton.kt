package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kmortyk.game.ui.element.InterfaceElement

class TextureButton(left: Float, bottom: Float, private var texture: Texture, private val onTouch: Runnable?) :
        InterfaceElement(left, bottom, texture.width.toFloat(), texture.height.toFloat()) {

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(texture, bounds.x, bounds.y)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if (onTouch == null) {
            return false
        }
        onTouch.run()
        return true
    }
}

class TextureRegionButton(left: Float, bottom: Float, private var textureRegion: TextureRegion, private val onTouch: Runnable?) :
    InterfaceElement(left, bottom, textureRegion.regionWidth.toFloat(), textureRegion.regionHeight.toFloat()) {

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(textureRegion, bounds.x, bounds.y)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if (onTouch == null) {
            return false
        }
        onTouch.run()
        return true
    }
}