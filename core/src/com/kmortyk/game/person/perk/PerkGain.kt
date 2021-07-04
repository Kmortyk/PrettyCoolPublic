package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets
import com.kmortyk.game.script.ScriptEngine

class PerkGain(private val scriptEngine: ScriptEngine) : Perk("Gain", "skill", Assets.atlas("perks").findRegion("perk_gain"), PerkType.Skill) {
    override fun execute() { scriptEngine.executeScript("scripts/v1/perks/gain.pcs") }

    override fun maxLevel(): Int = 1
}