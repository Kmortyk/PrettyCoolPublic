package com.kmortyk.game.modifier

// modifiers that are never expired

abstract class ModifierPassive : Modifier {
    override fun expired(): Boolean = false
}