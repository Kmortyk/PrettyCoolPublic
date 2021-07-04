package com.kmortyk.game.modifier

interface Modifier {
    // applies modifier
    fun apply()

    // reverts modifier
    fun revert()

    // checks whether or not modifier is active
    fun expired() : Boolean
}

abstract class ModifierSteps(private val stepsDuration: Int) : Modifier {
    var curStep: Int = 0

    fun step() {
        curStep++
    }

    override fun expired() : Boolean {
        return curStep > stepsDuration
    }
}