package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets

class PerkWindDash : Perk("Wind dash", "skill", Assets.atlas("perks").findRegion("perk_winddash"), PerkType.Skill) {
    override fun execute() { TODO("Not yet implemented") }

    override fun maxLevel(): Int = 1
}