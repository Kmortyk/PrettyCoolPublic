package com.kmortyk.game.hexagon.actions

import com.kmortyk.game.GridPosition
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.hexagon.HexagonAction
import com.kmortyk.game.map.MapName
import com.kmortyk.game.state.GameState

class LoadMapAction : HexagonAction {
    lateinit var mapName: MapName
    var entrypoint: GridPosition? = null

    override fun parseMap(keys: Map<String, String>) : HexagonAction {
        mapName = MapName(keys["map_name"]!!, "mainStory", true)
        val eRow = keys["entrypointRow"]?.toInt()
        val eCol = keys["entrypointCol"]?.toInt()

        if(eRow != null && eCol != null) {
            entrypoint = GridPosition(eRow, eCol)
        }

        return this
    }

    override fun executeOn(gameState: GameState, activatedHex: Hexagon) {
        gameState.loadMap(mapName, entrypoint)
    }

    override fun editorDrawable(): String {
        return "hex_exit"
    }

    override fun gameDrawable(): String {
        return "hex_exit"
    }
}