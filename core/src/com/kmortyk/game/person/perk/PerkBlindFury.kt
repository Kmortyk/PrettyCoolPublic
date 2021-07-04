package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkBlindFury : Perk("Blind fury", "skill", Assets.atlas("perks").findRegion("perk_blindfury"), PerkType.Skill) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 2
}