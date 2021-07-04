package com.kmortyk.game.ui

import com.badlogic.gdx.graphics.Color

data class GameSkin(
    val menuBacking: Color,
    val menuBackingBorder: Color,

    val quickPerksSlotBacking: Color,
    val quickPerksSlotBackingBorder: Color
)

val DarkGameSkin = GameSkin(
    menuBacking = Color.valueOf("#7a5643"),
    menuBackingBorder = Color.BLACK,
    quickPerksSlotBacking = Color.BLACK,
    quickPerksSlotBackingBorder = Color.valueOf("#474747")
)