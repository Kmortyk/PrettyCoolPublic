package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkGoldenHurt : Perk("Golden hurt", "+health", Assets.atlas("perks").findRegion("perk_goldenhurt"), PerkType.Permanent) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 3
}