package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch

typealias Callback = Runnable

// Effect is represents some sort of additional
// to the main process events,
// which usage NOT (!!!) changes game's state
abstract class Effect {
    var callback: Callback = Callback { }
    private var disabled: Boolean = false

    fun extend(delta: Float) : Boolean {
        if(disabled) return true

        val need = onExtend(delta)
        if(!need) {
            callback.run()
        }
        return need
    }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(disabled) return
        onDraw(assetManager, spriteBatch)
    }

    fun enable() { disabled = false }

    fun disable() { disabled = true }

    // dispose - calls when lifecycle of the effect ends
    open fun dispose() { }

    // onExtend - extends living of the effect with some real-time offset delta
    abstract fun onExtend(delta: Float) : Boolean
    // onDraw - draws effect [if implemented]
    open fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) { /* none at the base class */ }
}