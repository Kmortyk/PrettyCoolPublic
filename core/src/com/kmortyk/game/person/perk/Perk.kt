package com.kmortyk.game.person.perk

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

enum class PerkType {
    Permanent,
    Passive,
    Skill,
    SkillUpgrade,
}

abstract class Perk(val name: String, private val description: String, val texture: TextureRegion, val perkType: PerkType) {
    // current level of this perk
    var curLevel: Int = 0
        private set
    // execute - executes perk action
    abstract fun execute()
    // returns max level of this perk
    abstract fun maxLevel() : Int
    // returns full description for this perk upgrade
    open fun descriptionLong() : String { return "" }
    // returns short line with description of this perk
    fun descriptionShort() : String { return description }

    fun nextLevel() { curLevel++ }

    override fun toString(): String {
        return "Perk[$name, level=$curLevel]"
    }
}