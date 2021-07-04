package com.kmortyk.game.hexagon

import com.kmortyk.game.state.GameState

interface HexagonAction {
    // parse this action from keys data
    fun parseMap(keys: Map<String, String>) : HexagonAction
    // execute - executes an action at hex
    fun executeOn(gameState: GameState, activatedHex: Hexagon)
    // drawable to represent action at editor view
    fun editorDrawable() : String = "hex_debug"
    // drawable to represent action at game view
    fun gameDrawable() : String? = null
}