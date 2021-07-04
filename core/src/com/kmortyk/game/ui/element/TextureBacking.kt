package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Transparent rectangle with rounded edges.
 */
open class TextureBacking(tex: Texture, x: Float, y: Float) :
        InterfaceElement(x, y, tex.width.toFloat(), tex.height.toFloat()) {

    var texture: Texture = Texture(Pixmap(tex.width, tex.height, Pixmap.Format.RGB888))
        set(value) {
            bounds.set(bounds.x, bounds.y, value.width.toFloat(), value.height.toFloat())
            field = value
        }

    constructor() : this(Texture(Pixmap(0, 0, Pixmap.Format.RGB888)), 0.0f, 0.0f)

    init { texture = tex }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(texture, bounds.x, bounds.y)
    }

    override fun onTouch(x: Float, y: Float): Boolean { return true }
}