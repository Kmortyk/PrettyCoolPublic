package com.kmortyk.game.state

import com.kmortyk.game.modifier.Modifier
import com.kmortyk.game.modifier.ModifierSteps

class PlayerModifiers {

    private val activeModifiers: MutableList<ModifierSteps> = mutableListOf()

    private val passiveModifiers: MutableList<Modifier> = mutableListOf()

    fun addModifier(modifier: ModifierSteps) {
        modifier.apply()
        activeModifiers.add(modifier)
    }

    fun addPassiveModifier(modifier: Modifier) {
        // modifier.apply()
        passiveModifiers.add(modifier)
    }

    fun applyActiveModifiers() { // process active perks actions
        val toDel = mutableListOf<ModifierSteps>()

        for(mod in activeModifiers) {
            mod.step()

            if(mod.expired()) {
                mod.revert()
                toDel.add(mod)
            }
        }

        if(toDel.size > 0)
            activeModifiers.removeAll(toDel)
    }

    fun applyPassiveModifiers() { // process passive perks actions
        for(mod in passiveModifiers) {
            mod.apply()
        }
    }

    fun apply() {
        applyPassiveModifiers()
        applyActiveModifiers()
    }
}