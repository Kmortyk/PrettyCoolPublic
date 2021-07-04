package com.kmortyk.game.map;

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.item.Item
import com.kmortyk.game.person.Person
import com.kmortyk.game.scenery.Scenery
import com.kmortyk.game.state.GameState
import java.io.StringReader
import java.util.*
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.kmortyk.game.*
import com.kmortyk.game.hexagon.HexagonActionFactory
import com.kmortyk.game.person.Direction
import com.kmortyk.game.person.Player

enum class LoadPlayerPosType {
    LoadFromFile,
    SetEntrypoint,
    CalculateWorldMap
}

class GameMap {
    // hexes - two-dimensional array that represents game map for the level
    var hexes: MutableList<MutableList<Hexagon>> = mutableListOf()

    var dirs: SerializedDirs = SerializedDirs("", "", "", "")

    var description: String = ""

    // draw map
    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        // for each row in the hexes array
        hexes.forEach {
            for (hex in it) {
                hex.draw(assetManager, spriteBatch)
                // draw action view
                for(actionTex in hex.actionTexturesToDraw) {
                    spriteBatch.draw(assetManager[actionTex], hex.position.actualX(), hex.position.actualY())
                }
                /// DEBUG DRAW HEXES
                if(PrettyCoolGame.Debug && PrettyCoolGame.DrawHexCenter) {
                    val tex = assetManager[Assets["ui_exclamation_icon"]]
                    spriteBatch.draw(tex, hex.centerX(), hex.centerY())
                }
            }
        }
    }

    fun drawEntities(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        // for each row in the hexes array
        hexes.asReversed().forEach {
            for (hex in it) {
                hex.scenerySlots.forEach { scn -> scn.draw(assetManager, spriteBatch) }
                hex.itemSlots.forEach { item -> item.draw(assetManager, spriteBatch) }
                val person = hex.personSlot
                if(person != null) {
                    person.draw(assetManager, spriteBatch)
                    /// DEBUG OBSERVABLE HEXES
                    if(PrettyCoolGame.Debug && PrettyCoolGame.DrawObservableHexes) {
                        val texture = assetManager[Assets["hex_debug"]]
                        for(observableHex in person.observableHexes) {
                            observableHex.drawDebug(texture, spriteBatch)
                        }
                    }
                    /// DEBUG ACTUAL POSITION
                    if(PrettyCoolGame.Debug && PrettyCoolGame.DrawActualPos) {
                        val gridTex = assetManager[Assets["ui_debug_grid_pos"]]
                        val actualTex = assetManager[Assets["ui_debug_actual_pos"]]

                        val v = Position.projectGridToActual(person.gridPosition())
                        spriteBatch.draw(gridTex, v.x, v.y)

                        spriteBatch.draw(actualTex, person.position.actualX(), person.position.actualY())
                    }
                }
            }
        }
    }

    operator fun get(row: Int, col: Int): Hexagon = hexes[row][col]

    operator fun get(pos: Position): Hexagon = hexes[pos.row()][pos.col()]

    operator fun get(pos: GridPosition): Hexagon = hexes[pos.row][pos.col]

    fun rows(): Int = hexes.size

    fun cols(): Int = if(rows() > 0) hexes[0].size else 0

    // findPath - uses A Star algorithm.
    // TODO too difficult for each step of each mob
    fun findPath(from: GridPosition, to: GridPosition): LinkedList<GridPosition> {
        val start = PathNode(from.row, from.col)
        val end = PathNode(to.row, to.col)

        val closed: LinkedList<PathNode> = LinkedList()
        val open: LinkedList<PathNode> = LinkedList()
        start.g = 0
        start.h = PathNode.distance(start, end)
        start.f = start.h
        open.add(start)
        while (open.size > 0) {
            var current: PathNode? = null

            // вершина из open, имеющая самую низкую оценку
            for (node in open) {
                if (current == null || node.f < current.f) current = node
            }

            // конец, нашли путь от начала до конца
            if (current != null && current == end)
                return reconstructPath(current)

            open.remove(current)
            closed.add(current!!)

            for (neighbor in neighbors(current, end)) {
                if (closed.contains(neighbor)) continue
                val nextG: Int = current.g + neighbor.cost
                var tentativeIsBetter: Boolean
                if (!open.contains(neighbor)) {
                    open.add(neighbor)
                    tentativeIsBetter = true
                } else {
                    tentativeIsBetter = nextG < neighbor.g
                }
                if (tentativeIsBetter) {
                    neighbor.parent = current
                    neighbor.g = nextG
                    neighbor.h = PathNode.distance(neighbor, end)
                    neighbor.f = neighbor.g + neighbor.h
                }
            }
        }
        return LinkedList()
    }

    // reconstructPath - reconstructs path from the end to start
    private fun reconstructPath(end: PathNode): LinkedList<GridPosition> {
        val result: LinkedList<GridPosition> = LinkedList()
        var current: PathNode = end

        while (current.parent != null) {
            result.add(GridPosition(current.row, current.col))
            current = current.parent!!
        }

        result.reverse()
        return result
    }

    //neighbors - each neighbor also non null
    fun neighbors(n: PathNode?, end: PathNode): LinkedList<PathNode> {
        val neighbors: LinkedList<PathNode> = LinkedList()
        if (n == null) {
            println("findPath: attempt to find neighbors for null. return.")
            return neighbors
        }

        val possibleOffsets: Array<Pair<Int, Int>> = if (n.row % 2 == 0)
            arrayOf(
                    /*Pair(-1, 1),*/Pair(0, 1),/*Pair(1, 1),*/
                    Pair(-1, 0),/*Pair(0, 0),*/Pair(1, 0),
                    Pair(-1, -1), Pair(0, -1), Pair(1, -1)
            )
        else arrayOf(
                Pair(-1, 1), Pair(0, 1), Pair(1, 1),
                Pair(-1, 0),/*Pair(0, 0),*/Pair(1, 0),
                /*Pair(-1,-1),*/Pair(0, -1)/*Pair(1,-1)*/
        )

        for((rowOffset, colOffset) in possibleOffsets) {
            val row = n.row + rowOffset
            val col = n.col + colOffset

            if (row < 0 || col < 0) continue
            if (row >= rows() || col >= cols()) continue

            if (!get(row, col).blocked() || (row == end.row && col == end.col))
                neighbors.add(PathNode(row, col))
        }
        return neighbors
    }

    /* --- load ----------------------------------------------------------------------------------------------------- */
    fun loadMapFile(gameState: GameState, projectName: String, mapName: String,
                    loadPlayerPosType: LoadPlayerPosType, entrypointPlayerPos: GridPosition?) {
        val klaxon = Klaxon()

        val prevCols = cols()
        val prevRows = rows()

        // clear last state
        hexes.clear()
        gameState.clearState()

        JsonReader(StringReader(Assets.readFile("maps/$projectName/$mapName.json"))).use { reader ->
            val srMap = klaxon.parse<SerializedMap>(reader)!!
            val rows = srMap.hexes.rows
            val cols = srMap.hexes.cols

            // parse hexes
            hexes = MutableList(rows) { mutableListOf<Hexagon>() }

            for(row in 0 until rows) {
                for(col in 0 until cols) {
                    val hxID = srMap.hexes.idxs[row*cols + col]
                    hexes[row].add(Hexagon(hxID, row, col))
                }
            }
            // parse persons
            for(prs in srMap.persons.listIterator()) {
                val dir = if(prs.dir == "right") Direction.Right else Direction.Left

                if(prs.name == "player") {
                    when(loadPlayerPosType) {
                        LoadPlayerPosType.LoadFromFile -> {
                            log.info("load player position from file (${prs.pos.row}, ${prs.pos.col})")
                            gameState.player = Player(gameState)
                            gameState.player.position.setGrid(prs.pos.row, prs.pos.col, updateActual = true)
                        }
                        LoadPlayerPosType.SetEntrypoint -> {
                            log.info("set player position to entrypoint (${entrypointPlayerPos!!})")
                            gameState.player = Player(gameState)
                            gameState.player.position.setGrid(entrypointPlayerPos!!, updateActual = true)
                        }
                        LoadPlayerPosType.CalculateWorldMap -> {
                            val pos = gameState.player.gridPosition()
                            when {
                                pos.col == prevCols - 1 -> pos.col = 0
                                pos.col == 0 -> pos.col = srMap.hexes.cols - 1
                                pos.row == prevRows - 1 -> pos.row = 0
                                pos.row == 0 -> pos.row = srMap.hexes.rows - 1
                            }
                            log.info("set player position to calculated in world (${pos.row}, ${pos.col})")
                            gameState.player.position.setGrid(pos.row, pos.col, updateActual = true)
                        }
                    }
                    gameState.player.direction = dir
                    gameState.addPlayer(gameState.player)
                } else {
                    val person = Person(prs.name, prs.pos.row, prs.pos.col)
                    person.direction = dir
                    gameState.addPerson(person)
                }
            }
            // parse items
            for(itm in srMap.items) {
                gameState.addItem(Item(gameState, itm.name, itm.pos.row, itm.pos.col, itm.count))
            }
            // parse scenery
            for(scn in srMap.scenery) {
                for(pos in scn.pos_arr) {
                    gameState.addScenery(Scenery(scn.name, pos.row, pos.col))
                }
            }
            // parse meta info
            dirs = srMap.dirs
            description = srMap.description
            // parse actions
            for(serializedAction in srMap.actions) {
                for(row in serializedAction.from.row .. serializedAction.to.row) {
                    for(col in serializedAction.from.col .. serializedAction.to.col) {
                        val hex = hexes[row][col]
                        hex.addAction(
                            HexagonActionFactory.createAction(serializedAction.action_name, serializedAction.keys),
                            mainGame = true
                        )
                    }
                }
            }
        }

        log.info("\n"+"""
            ${LogColors.PURPLE}game_map${LogColors.RESET}
                name: ${LogColors.GREEN}"$mapName"${LogColors.RESET}
                num_of_persons: ${LogColors.BLUE}${gameState.persons.size}${LogColors.RESET}
                num_of_scenery: ${LogColors.BLUE}${gameState.scenery.size}${LogColors.RESET}
                num_of_hexes: ${LogColors.BLUE}${rows() * cols()}${LogColors.RESET}
                num_of_items: ${LogColors.BLUE}${gameState.items.size}${LogColors.RESET}
        """.trimIndent())
    }

    fun maxWidth(): Float {
        val size = hexes[0].size

        return size * Assets.HexWidth +
                (size % 2) * Assets.HexHorizontalOffset
    }

    fun maxHeight(): Float {
        val h = Assets.HexHeight - Assets.HexVerticalOffset

        return (hexes.size) * h + Assets.HexVerticalOffset
    }

    fun isHexBlocked(pos: GridPosition) = get(pos.row, pos.col).blocked()

    fun isExitHex(gridPosition: GridPosition) =
        gridPosition.col == 0 || gridPosition.col == cols() - 1 || gridPosition.row == 0 || gridPosition.row == rows() - 1

    fun loadNextMapWithExitHex(gameState: GameState, gridPosition: GridPosition) {
        val dir = when {
            gridPosition.row == rows() - 1 -> dirs.up
            gridPosition.col == cols() - 1 -> dirs.right
            gridPosition.row == 0 -> dirs.down
            gridPosition.col == 0 -> dirs.left
            else -> ""
        }
        if(dir.isEmpty()) {
            println("[WARN] next map is not specified dirs='$dirs'")
            return
        }
        loadMapFile(gameState, gameState.mapsProjectName, dir, LoadPlayerPosType.CalculateWorldMap, null)
    }
}
