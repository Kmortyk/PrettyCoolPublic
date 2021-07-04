package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkSpeed : Perk("Speed", "+speed", Assets.atlas("perks").findRegion("perk_speed"), PerkType.Passive) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 5
}