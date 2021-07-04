package com.kmortyk.game.hexagon

import com.kmortyk.game.hexagon.actions.EmptyAction
import com.kmortyk.game.hexagon.actions.LoadMapAction

class HexagonActionFactory {
    companion object {
        fun createAction(actionName: String, keys: Map<String, String>) : HexagonAction {
            return when(actionName) {
                "load_map" -> LoadMapAction().parseMap(keys)
                else -> EmptyAction()
            }
        }
    }
}