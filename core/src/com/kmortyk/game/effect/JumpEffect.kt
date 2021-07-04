package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.person.Person


class JumpEffect(val person: Person) : Effect() {
    companion object {
        private const val jumpSpeed = 150f
        private const val jumpHeight = 6f
    }

    private val minHeight: Float = person.position.actualY() + jumpHeight
    private val startHeight: Float = person.position.actualY()
    private var up = true

    override fun onExtend(delta: Float): Boolean {
        if (up) {
            if (person.position.actualY() < minHeight) {
                person.position.actual.y += delta * jumpSpeed
            } else {
                up = false
            }
        } else {
            if (person.position.actualY() > startHeight) {
                person.position.actual.y -= delta * jumpSpeed
            } else person.position.actual.y = startHeight
        }
        return up || person.position.actual.y != startHeight // continue...
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) { /* empty */ }

    override fun dispose() {
        person.position.actual.y = startHeight /* if cancel */
    }
}