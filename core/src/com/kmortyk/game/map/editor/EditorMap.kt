package com.kmortyk.game.map.editor

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.kmortyk.game.*
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.item.Item
import com.kmortyk.game.person.Person
import com.kmortyk.game.person.Player
import com.kmortyk.game.scenery.Scenery
import com.kmortyk.game.state.GameState
import java.io.StringReader
import java.lang.RuntimeException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Json
import com.google.gson.Gson
import com.kmortyk.game.map.*
import java.io.File
import com.google.gson.GsonBuilder
import com.kmortyk.game.SerializedItem
import com.kmortyk.game.hexagon.HexagonActionFactory
import com.kmortyk.game.map.SerializedPerson
import com.kmortyk.game.person.Direction

enum class MapDirectory {
    AssetDirectory,
    ExternalDirectoryLocal,
    ExternalDirectoryServer
}

class EditorMap(val dummyGameState: GameState) {

    /// OBJECTS
    // persons updates with map change, persons[0] == player
    val persons: CopyOnWriteArrayList<Person> = CopyOnWriteArrayList()
    // items represents all items on the map
    val items: MutableList<Item> = mutableListOf()
    // scenery represents all decorations on the map
    val scenery: MutableList<Scenery> = mutableListOf()
    // in every game state we has player somewhere ...
    var player: Player = Player(dummyGameState)

    /// MAP AND META
    // hexes - two-dimensional array that represents editor map
    var hexes: MutableList<MutableList<Hexagon>> = mutableListOf(mutableListOf(Hexagon(100, 0, 0)))
    // map dirs
    var dirs: SerializedDirs = SerializedDirs("", "", "", "")


    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val debugHex = assetManager[Assets["hex_debug"]]

        hexes.forEach {
            for (hex in it) {
                hex.draw(assetManager, spriteBatch)
                // draw action view
                for(action in hex.actions) {
                    spriteBatch.draw(assetManager[Assets[action.editorDrawable()]],
                        hex.position.actualX(), hex.position.actualY())
                }
                /// DEBUG
                if(PrettyCoolGame.Debug && PrettyCoolGame.EditorDebugHexes)
                    spriteBatch.draw(debugHex, hex.position.actualX(), hex.position.actualY())
            }
        }
    }

    fun drawEntities(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        hexes.asReversed().forEach {
            for (hex in it) {
                hex.scenerySlots.forEach { scn -> scn.draw(assetManager, spriteBatch) }
                hex.itemSlots.forEach { item -> item.draw(assetManager, spriteBatch) }
                hex.personSlot?.draw(assetManager, spriteBatch)
            }
        }
    }

    // -- get and set --------------------------------------------------------------------------------------------------
    fun rows(): Int = hexes.size

    fun cols(): Int = if(rows() > 0) hexes[0].size else 0

    operator fun get(row: Int, col: Int): Hexagon = hexes[row][col]

    operator fun get(gridPosition: GridPosition): Hexagon = hexes[gridPosition.row][gridPosition.col]

    operator fun set(gridPosition: GridPosition, value: Hexagon) {
        hexes[gridPosition.row][gridPosition.col] = value
    }

    // -- extend -------------------------------------------------------------------------------------------------------
    fun addHex(hex: Hexagon) {
        var rowsOffset = when {
            hex.row() < 0 -> abs(hex.row())
            hex.row() >= rows() -> (hex.row() + 1) - rows()
            else -> 0
        }
        val colsOffset = when {
            hex.col() < 0 -> abs(hex.col())
            hex.col() >= cols() -> (hex.col() + 1) - cols()
            else -> 0
        }

        var rowOffset = if(hex.row() < 0) rowsOffset else 0
        val colOffset = if(hex.col() < 0) colsOffset else 0

        if(rowOffset % 2 != 0) {
            rowsOffset += 1
            rowOffset += 1
            hex.position.setGrid(hex.row() + rowOffset, hex.col() + colOffset - 1)
        } else {
            hex.position.setGrid(hex.row() + rowOffset, hex.col() + colOffset)
        }

        hexes = createMap(rows() + rowsOffset, cols() + colsOffset, rowOffset, colOffset)
        hexes[hex.row()][hex.col()] = hex
    }

    fun createMap(rows: Int, cols: Int, offsetRows: Int, offsetCols: Int): MutableList<MutableList<Hexagon>> {
        val newHexes = mutableListOf<MutableList<Hexagon>>()
        for(r in 0 until rows) {
            newHexes.add(mutableListOf())
            for(c in 0 until cols) {
                newHexes[r].add(Hexagon(Assets.NullHex, r, c))
            }
        }

        if(rows() > 0 && cols() > 0) {
            for(r in 0 until rows()) {
                for(c in 0 until cols()) {
                    val hex = hexes[r][c]
                    val grid = GridPosition(r + offsetRows, c + offsetCols)
                    hex.position.setGrid(grid, updateActual=true)
                    if(hex.personSlot != null)
                        hex.personSlot!!.position.setGrid(grid, updateActual=true)
                    for(item in hex.itemSlots)
                        item.position.setGrid(grid, updateActual=true)
                    for(scn in hex.scenerySlots)
                        scn.position.setGrid(grid, updateActual=true)
                    newHexes[r + offsetRows][c + offsetCols] = hex
                }
            }
        }

        return newHexes
    }

    fun optimalCenterWidth(): Float {
        return (hexes[0].size - 1) * Assets.HexWidth
    }

    fun optimalCenterHeight(): Float {
        val y = (hexes.size - 1) * Assets.HexHeight * 0.7f + Assets.HexHeight * 0.3f
        return y*0.5f
    }

    // --- load --------------------------------------------------------------------------------------------------------
    fun loadMapFile(mapPath: String, loadPlayerPos: Boolean = false) {
        val klaxon = Klaxon()

        val prevCols = cols()
        val prevRows = rows()

        // clear last state
        hexes.clear()
        persons.clear()
        items.clear()
        scenery.clear()

        JsonReader(StringReader(Assets.readFile(mapPath))).use { reader ->
            val srMap = klaxon.parse<SerializedMap>(reader)!!
            val rows = srMap.hexes.rows
            val cols = srMap.hexes.cols

            hexes = MutableList(rows) { mutableListOf<Hexagon>() }

            for(row in 0 until rows) {
                for(col in 0 until cols) {
                    println(row*cols + col)
                    val hxID = srMap.hexes.idxs[row*cols + col]
                    hexes[row].add(Hexagon(hxID, row, col))
                }
            }

            // parse persons
            for(prs in srMap.persons.listIterator()) {
                val dir = if(prs.dir == "right") Direction.Right else Direction.Left

                if(prs.name == "player") {
                    if(loadPlayerPos) {
                        player = Player(dummyGameState)
                        player.position.setGrid(prs.pos.row, prs.pos.col)
                    } else {
                        val pos = player.gridPosition()
                        when {
                            pos.col == prevCols - 1 -> pos.col = 0
                            pos.col == 0 -> pos.col = srMap.hexes.cols - 1
                            pos.row == prevRows - 1 -> pos.row = 0
                            pos.row == 0 -> pos.row = srMap.hexes.rows - 1
                        }
                        player.position.setGrid(pos.row, pos.col)
                    }
                    player.direction = dir
                    addPerson(player)
                } else {
                    val person = Person(prs.name, prs.pos.row, prs.pos.col)
                    person.direction = dir
                    addPerson(person)
                }
            }
            // parse items
            for(itm in srMap.items) {
                addItem(Item(dummyGameState, itm.name, itm.pos.row, itm.pos.col, itm.count))
            }
            // parse scenery
            for(scn in srMap.scenery) {
                for(pos in scn.pos_arr) {
                    addScenery(Scenery(scn.name, pos.row, pos.col))
                }
            }
            // parse meta info
            dirs = srMap.dirs
            // parse actions
            for(serializedAction in srMap.actions) {
                for(row in serializedAction.from.row .. serializedAction.to.row) {
                    for(col in serializedAction.from.col .. serializedAction.to.col) {
                        val hex = hexes[row][col]
                        hex.addAction(
                            HexagonActionFactory.createAction(serializedAction.action_name, serializedAction.keys),
                            mainGame = false
                        )
                    }
                }
            }
        }

        log.info("\n"+"""
            ${LogColors.PURPLE}editor_map${LogColors.RESET}:
                path=${LogColors.GREEN}"$mapPath"${LogColors.RESET},
                num_of_persons=${LogColors.BLUE}${persons.size}${LogColors.RESET},
                num_of_scenery=${LogColors.BLUE}${scenery.size}${LogColors.RESET}
                num_of_hexes=${LogColors.BLUE}${rows() * cols()}${LogColors.RESET},
                num_of_items=${LogColors.BLUE}${items.size}${LogColors.RESET}
        """.trimIndent())
    }

    fun addPerson(p: Person) {
        // occupy slot
        get(p.position.grid).personSlot = p
        // add to list of persons
        persons.add(p)
    }

    fun addItem(it: Item) {
        // extend slots
        get(it.position.grid).itemSlots.add(it)
        // add to list of items
        items.add(it)
    }

    fun addScenery(scn: Scenery) {
        // extend slots
        get(scn.position.grid).scenerySlots.add(scn)
        // add to list of items
        scenery.add(scn)
    }

    fun saveMapFile(dir: MapDirectory, projectName: String, mapName: String) {
        when(dir) {
            MapDirectory.AssetDirectory -> {
                if(PrettyCoolGame.Debug && Gdx.app.type == Application.ApplicationType.Desktop) {
                    val path = "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/maps/$projectName/$mapName.json"
                    val json = serializeMap()

                    // println(json)

                    if(!Gdx.files.isExternalStorageAvailable) {
                        throw GdxRuntimeException("can't write to external storage: not available")
                    }

                    val file = File(path)
                    file.writeText(json)

                    log.info("write map file to ${LogColors.GREEN}\"$path\"${LogColors.RESET}")

                } else {
                    log.error("can't save map to assets: cannot be possible")
                }
            }
        }
    }

    private fun serializeMap(): String {
        // persons
        val persons = mutableListOf<SerializedPerson>()
        for(person in this.persons) {
            val dir = if(person.direction == Direction.Right) "right" else "left"
            val serialized = SerializedPerson(person.name, SerializedPos(person.position.row(), person.position.col()), dir)
            persons.add(serialized)
        }

        // items
        val items = mutableListOf<com.kmortyk.game.map.SerializedItem>()
        for(item in this.items) {
            val serialized = com.kmortyk.game.map.SerializedItem(
                item.name, SerializedPos(item.position.row(), item.position.col()),
                count = item.count
            )
            items.add(serialized)
        }

        // scenery
        val scenery = mutableListOf<SerializedEntityArray>()
        val sceneryMap: MutableMap<String, MutableList<SerializedPos>> = mutableMapOf()
        for(scn in this.scenery) {
            if(scn.name !in sceneryMap) {
                sceneryMap[scn.name] = mutableListOf()
            }
            sceneryMap[scn.name]!!.add(SerializedPos(scn.position.row(), scn.position.col()))
        }
        for((name, posArr) in sceneryMap) {
            scenery.add(SerializedEntityArray(name, posArr))
        }

        // TODO add actions
        val serializedActions: MutableList<SerializedAction> = mutableListOf()

        // hexes
        val hexesIds = mutableListOf<Int>()
        for(row in 0 until rows())
            for(col in 0 until cols())
                hexesIds.add(get(row, col).hexID)

        val serializedMap = SerializedMap("[editor_map]",
            persons, items, scenery,
            SerializedHexes(rows(), cols(), hexesIds), dirs, serializedActions)

        val gson = GsonBuilder().setPrettyPrinting().create()

        return gson.toJson(serializedMap)
    }
}