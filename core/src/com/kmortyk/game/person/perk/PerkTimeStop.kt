package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkTimeStop : Perk("Time stop", "skill", Assets.atlas("perks").findRegion("perk_time_stop"), PerkType.Skill) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 1
}