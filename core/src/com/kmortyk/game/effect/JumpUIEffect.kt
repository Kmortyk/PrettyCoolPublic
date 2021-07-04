package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.ui.element.InterfaceElement

class JumpUIEffect(private val element: InterfaceElement) : Effect() {
    companion object {
        private const val jumpSpeed = 150f
        private const val jumpHeight = 3f
    }

    private val minHeight: Float = element.bounds.y + jumpHeight
    private val startHeight: Float = element.bounds.y
    private var up: Boolean = true

    override fun onExtend(delta: Float): Boolean {
        if (up) {
            if (element.bounds.y < minHeight) {
                element.bounds.y += delta * jumpSpeed
            } else {
                up = false
            }
        } else {
            if (element.bounds.y > startHeight) {
                element.bounds.y -= delta * jumpSpeed
            } else element.bounds.y = startHeight
        }
        return isNotEnded() // continue...
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) { /* empty */ }

    override fun dispose() {
        element.bounds.y = startHeight /* if cancel */
    }

    fun isNotEnded() : Boolean {
        return up || element.bounds.y != startHeight
    }
}