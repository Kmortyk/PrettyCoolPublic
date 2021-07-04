package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkHorror : Perk("Horror", "skill", Assets.atlas("perks").findRegion("perk_horror"), PerkType.Skill) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 1
}