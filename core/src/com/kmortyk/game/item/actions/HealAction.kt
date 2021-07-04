package com.kmortyk.game.item.actions

import com.kmortyk.game.item.ItemAction
import com.kmortyk.game.person.Person
import com.kmortyk.game.state.GameState

class HealAction(private val person: Person, private val amount: Int) : ItemAction() {
    override fun executeOn(gameState: GameState, row: Int, col: Int) {
        person.addHealthPoints(amount)
    }
}