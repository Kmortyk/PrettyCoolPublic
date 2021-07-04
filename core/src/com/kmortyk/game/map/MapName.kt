package com.kmortyk.game.map

import com.kmortyk.game.GridPosition

data class MapName(
    val fileName: String,
    val projectName: String,
    val loadPlayerPos: Boolean,
    val setPlayerPos: GridPosition? = null,
    val extension: String = "json"
)