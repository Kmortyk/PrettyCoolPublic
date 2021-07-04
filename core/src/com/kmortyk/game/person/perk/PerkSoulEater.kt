package com.kmortyk.game.person.perk

import com.kmortyk.game.Assets
import com.kmortyk.game.LogColors
import com.kmortyk.game.effect.SoulEaterEffect
import com.kmortyk.game.log
import com.kmortyk.game.modifier.ModifierPassive
import com.kmortyk.game.modifier.ModifierProbability
import com.kmortyk.game.state.GameState

class PerkSoulEater(val gameState: GameState) : Perk("Soul eater", "passive",
        Assets.atlas("perks").findRegion("perk_soul_eater"), PerkType.Passive) {
    override fun execute() {
        gameState.player.modifiers.addPassiveModifier(
            ModifierProbability(gameState.statistic, "100%",
            object : ModifierPassive() {
                override fun apply() {
                    if(gameState.player.journal.hasAttackedSomebody) {
                        log.info("${LogColors.RED}`soul_eater`${LogColors.RESET} perk applied")

                        val amount = gameState.player.journal.attackAmount
                        gameState.player.addHealthPoints(amount)
                        gameState.game.addEffect(SoulEaterEffect(gameState.player))
                    }
                }
                override fun revert() { /* none */ }
            })
        )
    }

    override fun maxLevel(): Int = 1
}