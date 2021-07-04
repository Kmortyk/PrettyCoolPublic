package com.kmortyk.game.map.editor

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.item.Item
import com.kmortyk.game.person.Direction
import com.kmortyk.game.person.Person
import com.kmortyk.game.scenery.Scenery
import com.kmortyk.game.script.ScriptEngine
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.game.InventoryCells
import kotlin.math.abs
import kotlin.math.floor

enum class AssetType {
    Hexagon,
    Person,
    Item,
    Scenery
}

class EditorState(val camera: Camera, val game: PrettyCoolGame) {

    private val dummyScriptElement: ScriptEngine = ScriptEngine(game)
    private val dummyGameState: GameState = GameState(game, dummyScriptElement)

    private var instantDraw: Boolean = false
    private var touchedPoint: Vector2 = Vector2()
    private var touchedDown = false
    private val editorMap: EditorMap = EditorMap(dummyGameState)

    private var curAssetType: AssetType = AssetType.Hexagon
    private var assetID: Int = 101
    private var assetName: String = ""

    private val selectionHexagon: Hexagon = Hexagon(0, 0, Assets.atlas("hexes").findRegion("hex_editor_select_map_heaxagon"))

    init {
        editorMap.addHex(Hexagon(101, 0, 0))
        //editorMap.addHex(Hexagon(101, 0, 1))
        //editorMap.addHex(Hexagon(101, 1, 0))
        //editorMap.addHex(Hexagon(101, 1, 1))

        camera.translate(
            -camera.viewportWidth*0.5f+editorMap.optimalCenterWidth()+Assets.HexWidth,
            -camera.viewportHeight*0.5f+editorMap.optimalCenterHeight(), 0.0f
        )
    }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        // draw game map
        editorMap.draw(assetManager, spriteBatch)
        // draw ui-selection hexagon
        selectionHexagon.draw(assetManager, spriteBatch)
        // draw all entities
        editorMap.drawEntities(assetManager, spriteBatch)
    }

    fun touch(worldX: Float, worldY: Float) {
        touchedPoint.set(worldX, worldY)
        touchedDown = true

        val row = floor(worldY / (Assets.HexHeight - Assets.HexVerticalOffset)).toInt()
        val col = floor((worldX - (Assets.HexHorizontalOffset * (row % 2))) / Assets.HexWidth).toInt()

        if(selectionHexagon.position.grid.same(row, col)) {
            when(curAssetType) {
                AssetType.Person -> {
                    // swap direction if already has that person
                    val hex = editorMap[selectionHexagon.gridPosition()]
                    if(hex.personSlot != null) {
                        val dir = hex.personSlot!!.direction
                        if(dir == Direction.Right) {
                            hex.personSlot!!.direction = Direction.Left
                        } else {
                            hex.personSlot!!.direction = Direction.Right
                        }
                    }
                }
            }
        } else {
            selectionHexagon.position.setGrid(row, col, true)
        }
    }

    fun touchUp(worldX: Float, worldY: Float) {
        touchedDown = false
        if(instantDraw)
            brush()
    }

    fun drag(worldX: Float, worldY: Float) {
        if(!touchedDown)
            return

        if(touchedPoint.dst2(worldX, worldY) > InventoryCells.startDragDst) {
            val ox = touchedPoint.x - worldX
            val oy = touchedPoint.y - worldY

            camera.translate(ox, oy, 0.0f)
            camera.update()
        }
    }

    fun setLastAsset(assetID: Int, assetName: String) {
        this.assetID = assetID
        this.assetName = assetName
    }

    fun brush() {
        when (curAssetType) {
            AssetType.Hexagon -> {
                val prevRows = editorMap.rows()
                val prevCols = editorMap.cols()

                editorMap.addHex(Hexagon(assetID, selectionHexagon.row(), selectionHexagon.col()))

                val deltaRows = editorMap.rows() - prevRows
                val deltaCols = editorMap.cols() - prevCols

                if(editorMap.rows() > prevRows) {
                    camera.position.y += (Assets.HexHeight - Assets.HexVerticalOffset)*(editorMap.rows() - prevRows)
                }
                if(editorMap.cols() > prevCols) {
                    camera.position.x += Assets.HexWidth*(editorMap.rows() - prevRows)
                }

                selectionHexagon.position.setGrid(
                    selectionHexagon.position.row() + (editorMap.rows() - prevRows),
                    selectionHexagon.position.col() + (if(abs(deltaRows) > 0) -deltaRows+1 else 0),
                    updateActual = true
                )
            }
            AssetType.Item -> {
                if(!inBoundsSelection()) return
                editorMap.addItem(Item(dummyGameState, assetName, selectionHexagon.row(), selectionHexagon.col(), 1))
            }
            AssetType.Person -> {
                if(!inBoundsSelection()) return
                editorMap.addPerson(Person(assetName, selectionHexagon.row(), selectionHexagon.col()))
            }
            AssetType.Scenery -> {
                if(!inBoundsSelection()) return
                editorMap.addScenery(Scenery(assetName, selectionHexagon.row(), selectionHexagon.col()))
            }
        }
    }

    fun erase() {
        if(!inBoundsSelection()) return

        when(curAssetType) {
            AssetType.Hexagon -> {
                editorMap[selectionHexagon.gridPosition()] = Hexagon(Assets.NullHex, selectionHexagon.row(), selectionHexagon.col())
            }
            AssetType.Item -> {
                val items = editorMap[selectionHexagon.gridPosition()].itemSlots
                if(items.size > 0) {
                    val last = items.last()
                    editorMap[selectionHexagon.gridPosition()].itemSlots.remove(last)
                    editorMap.items.remove(last)
                }
            }
            AssetType.Person -> {
                val person = editorMap[selectionHexagon.gridPosition()].personSlot
                if(person != null) {
                    editorMap[selectionHexagon.gridPosition()].personSlot = null
                    editorMap.persons.remove(person)
                }
            }
            AssetType.Scenery -> {
                val scs = editorMap[selectionHexagon.gridPosition()].scenerySlots
                if(scs.size > 0) {
                    val last = scs.last()
                    editorMap[selectionHexagon.gridPosition()].scenerySlots.remove(last)
                    editorMap.scenery.remove(last)
                }
            }
        }
    }

    private fun inBoundsSelection(): Boolean {
        val row = selectionHexagon.row()
        val col = selectionHexagon.col()

        return row >= 0 && row < editorMap.rows() &&
                col >= 0 && col < editorMap.cols()
    }

    fun setAssetType(assetType: AssetType) {
        curAssetType = assetType
    }

    fun setInstantDraw(toggled: Boolean) {
        this.instantDraw = toggled
    }

    fun createMap(hexID: Int, width: Int, height: Int) {
        editorMap.hexes.clear()

        for(r in 0 until height) {
            for(c in 0 until width) {
                editorMap.addHex(Hexagon(hexID, r, c))
            }
        }
    }

    fun loadMap(mapPath: String) {
        editorMap.loadMapFile(mapPath, true)
    }

    fun saveMap(category: Int, projectName: String, mapName: String) {
        editorMap.saveMapFile(MapDirectory.AssetDirectory, projectName, mapName)
    }

    fun setMapDirs(up: String, right: String, down: String, left: String) {
        editorMap.dirs.up = up
        editorMap.dirs.right = right
        editorMap.dirs.down = down
        editorMap.dirs.left = left
    }

    fun getMapDirs() = editorMap.dirs
}