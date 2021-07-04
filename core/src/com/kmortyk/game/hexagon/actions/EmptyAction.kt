package com.kmortyk.game.hexagon.actions

import com.kmortyk.game.LogColors
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.hexagon.HexagonAction
import com.kmortyk.game.log
import com.kmortyk.game.state.GameState

class EmptyAction : HexagonAction {
    override fun parseMap(keys: Map<String, String>): HexagonAction {
        return this
    }

    override fun executeOn(gameState: GameState, activatedHex: Hexagon) {
        log.info("${LogColors.RED}error${LogColors.RESET}: " +
                "empty hexagon action triggered at pos=${activatedHex.gridPosition()}")
    }

    override fun editorDrawable(): String {
        return "hex_act_err"
    }

    override fun gameDrawable(): String {
        return "hex_act_err"
    }
}