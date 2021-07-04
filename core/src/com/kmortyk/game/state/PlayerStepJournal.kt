package com.kmortyk.game.state

class PlayerStepJournal {

    /// -- ATTACK ------------------------------------------------------------------------------------------------------
    var hasAttackedSomebody: Boolean = false
    var attackAmount: Int = 0

    fun attack(amount: Int) {
        hasAttackedSomebody = true
        attackAmount = amount
    }

    fun reset() {
        hasAttackedSomebody = false
        attackAmount = 0
    }
}