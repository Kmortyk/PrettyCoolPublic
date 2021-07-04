package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkPlaceholder : Perk("Placeholder", "unknown", Assets.atlas("perks").findRegion("perk_placeholder"), PerkType.Permanent) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int { return 1 }
}