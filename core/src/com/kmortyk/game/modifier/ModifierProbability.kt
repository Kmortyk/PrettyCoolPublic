package com.kmortyk.game.modifier

import com.kmortyk.game.state.GameStatistic

class ModifierProbability(
    val statistic: GameStatistic,
    val chanceStr: String,
    val modifier: Modifier) : Modifier {

    var expired = false
    var needRevert = false

    override fun apply() {
        if(statistic.eventPossible(chanceStr)) {
            modifier.apply()
            needRevert = true
        }
    }

    override fun revert() {
        if(needRevert) {
            modifier.revert()
            needRevert = false
        }
    }

    override fun expired(): Boolean = expired
}