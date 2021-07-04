package com.kmortyk.game.map

import com.kmortyk.game.GridPosition

data class SerializedPos(val row: Int, val col: Int)
data class SerializedEntity(val name: String, val pos: SerializedPos)
data class SerializedItem(val name: String, val pos: SerializedPos, val count: Int = 1)
data class SerializedPerson(val name: String, val pos: SerializedPos, val dir: String = "right")
data class SerializedEntityArray(val name: String, val pos_arr: List<SerializedPos>)
data class SerializedHexes(val rows: Int, val cols: Int, val idxs: List<Int>)
data class SerializedDirs(var up: String, var right: String, var down: String, var left: String)
data class SerializedAction(val from: GridPosition, val to: GridPosition, val action_name: String, val keys: Map<String, String>)

data class SerializedMap(
        val description: String,
        val persons: List<SerializedPerson>,
        val items: List<SerializedItem>,
        val scenery: List<SerializedEntityArray>,
        val hexes: SerializedHexes,
        val dirs: SerializedDirs,
        val actions: List<SerializedAction>
)