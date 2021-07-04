package com.kmortyk.game.state

import com.kmortyk.game.person.perk.*
import com.kmortyk.game.script.ScriptEngine

class PlayerPerks(val gameState: GameState, val scriptEngine: ScriptEngine) {

    private val perks: MutableList<Perk> = mutableListOf()

    init {
        perks.add(PerkInnerStrength(gameState.player))
        perks.add(PerkGoldenHurt())
        perks.add(PerkSpeed())
        perks.add(PerkGain(scriptEngine))
        perks.add(PerkSoulEater(gameState))
        perks.add(PerkWindDash())
        perks.add(PerkBlindFury())
        perks.add(PerkHorror())
        perks.add(PerkTimeStop())
    }

    fun size() // return actual size of current available perks
        = perks.size

    operator fun get(idx: Int) // return perk by its index
        = perks[idx]
}