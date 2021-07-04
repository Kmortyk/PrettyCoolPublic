package com.kmortyk.game

import com.badlogic.gdx.math.Vector2

// GridPosition - position [vector] at the map array
// row - row of the array, col - column of the array
data class GridPosition(var row: Int, var col: Int) {
    // values setter
    fun setValue(row: Int, col: Int) {
        this.row = row
        this.col = col
    }

    fun setValue(other: GridPosition) {
        this.row = other.row
        this.col = other.col
    }

    override fun equals(other: Any?): Boolean {
        if(other is GridPosition) {
            return row == other.row && col == other.col
        }
        return false
    }

    fun same(row: Int, col: Int): Boolean {
        return this.row == row && this.col == col
    }
}

class Position(row: Int, col: Int) {
    companion object {
        fun projectGridToActual(gridPosition: GridPosition) = projectGridToActual(gridPosition.row, gridPosition.col)
        fun projectGridToActual(row: Int, col: Int) : Vector2 {
            val actual = Vector2()
            actual.x = col * Assets.HexWidth + (Assets.HexHorizontalOffset * (row % 2))
            actual.y = row * (Assets.HexHeight - Assets.HexVerticalOffset)
            return actual
        }

        fun getHexCenter(grid: GridPosition, out: Vector2) : Vector2 {
            out.x = grid.col * Assets.HexWidth + (Assets.HexHorizontalOffset * (grid.row % 2))
            out.y = grid.row * (Assets.HexHeight - Assets.HexVerticalOffset)

            out.x += Assets.HexWidth*0.5f
            out.y += Assets.HexHeight*0.5f

            return out
        }
    }

    // default constructor without arguments
    constructor() : this(0, 0)

    // actual position (of center) at screen
    val actual: Vector2 = Vector2()

    fun actualX() = actual.x

    fun actualY() = actual.y

    // greed-based position with integer representation
    val grid: GridPosition = GridPosition(row, col)

    fun col() = grid.col

    fun row() = grid.row

    fun setGrid(pos: GridPosition, updateActual: Boolean) = setGrid(pos.row, pos.col, updateActual)

    // setGrid - set new position at the game map
    fun setGrid(row: Int, col: Int, updateActual: Boolean = true) {
        // update grid position
        grid.setValue(row, col)
        // update actual position
        if(updateActual) {
            actual.set(projectGridToActual(row, col))
        }
    }

    @Deprecated("do not use in release", ReplaceWith("setGrid(row, col)"))
    fun debugSetActual(x: Float, y: Float) : Vector2 = actual.set(x, y)

    init {
        setGrid(row, col, updateActual = true)
    }
}