package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets
import com.kmortyk.game.person.Player

class PerkInnerStrength(val player: Player) : Perk("Inner strength", "+strength",
        Assets.atlas("perks").findRegion("perk_soul_strength"), PerkType.Permanent) {

    override fun execute() {
        player.stats.strength += count(curLevel)
    }

    override fun descriptionLong(): String {
        return """ 
                    Вы чувствуете необычайный прилив энергии,
                    которая исходит из вашей души, она дарует
                    вам силу для сокрушения Ваших врагов.

                    Adds ${count(curLevel + 1)} POINT to STRENGTH
                """.trimIndent()
    }

    override fun maxLevel(): Int = 3

    private fun count(lvl: Int) : Int {
        return if(lvl < 3)
            1
        else
            2
    }
}