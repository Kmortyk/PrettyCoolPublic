package com.kmortyk.game.item

import com.kmortyk.game.state.GameState

// ItemAction - represents action that can be executed with use of some item
// constructor of ItemAction can hold many of parameters, e.g. Hero, GameWorld and etc.
abstract class ItemAction {
    // execute - executes an action at hex
    abstract fun executeOn(gameState: GameState, row: Int, col: Int)
}