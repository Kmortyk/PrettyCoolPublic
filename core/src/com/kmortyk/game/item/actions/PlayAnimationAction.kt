package com.kmortyk.game.item.actions

import com.kmortyk.game.item.ItemAction
import com.kmortyk.game.person.Person
import com.kmortyk.game.state.GameState

class PlayAnimationAction(val person: Person, private val animationName: String) : ItemAction() {
    override fun executeOn(gameState: GameState, row: Int, col: Int) {
        person.animationManager.playOnce(animationName)
    }
}