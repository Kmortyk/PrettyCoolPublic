package com.kmortyk.game.ui.element.special

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.effect.Callback
import com.kmortyk.game.effect.Effect
import com.kmortyk.game.ui.element.InterfaceElement

class EffectElement(val effect: Effect, val callback: Callback) : InterfaceElement() {

    var finished = false

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(effect.extend(Gdx.graphics.deltaTime)) {
            effect.draw(assetManager, spriteBatch)
        } else if(!finished) {
            finished = true
            callback.run()
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean { return false }

    fun isFinished() : Boolean {
        return finished
    }
}