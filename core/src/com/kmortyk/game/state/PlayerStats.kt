package com.kmortyk.game.state

class PlayerStats {
    // STRENGTH influence the melee damage
    var strength: Int = 1

    // ACCURACY allows to shot to more distant hexes
    var accuracy: Int = 1

    // SPEED how many steps can make player (indirectly)
    var speed: Int = 1

    // DEFENSE higher values guarantees more protection for the health
    var defense: Int = 1

    // STEALTH allows to be less seen by nearby enemies
    var stealth: Int = 1

    // INTELLECT influence the ability to craft complex items
    // and sometimes opens new dialog options
    var intellect: Int = 1

    fun modifyStat(name: String, value: Int, sign: Int = 1) {
        when(name) {
            "strength" -> strength += sign*value
            "accuracy" -> accuracy += sign*value
            "speed" -> speed += sign*value
            "defense" -> defense += sign*value
            "stealth" -> stealth += sign*value
            "intellect" -> intellect += sign*value
        }
    }
}