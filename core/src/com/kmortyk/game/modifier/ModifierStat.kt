package com.kmortyk.game.modifier

import com.kmortyk.game.state.PlayerStats

class ModifierStat(private val stat: PlayerStats,
                   private val statName: String,
                   private val statModifier: Int,

                   steps: Int) : ModifierSteps(steps) {

    override fun apply() {
        stat.modifyStat(statName, statModifier, 1)
    }

    override fun revert() {
        stat.modifyStat(statName, statModifier, -1)
    }
}