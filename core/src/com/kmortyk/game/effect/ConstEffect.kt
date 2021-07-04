package com.kmortyk.game.effect

class EmptyEffect : ConstEffect() {
    override fun create() : ConstEffect { return this }
    override fun onExtend(delta: Float) = false
}

abstract class ConstEffect : Effect() {
    // calls when needed to reset effect
    abstract fun create() : ConstEffect
}