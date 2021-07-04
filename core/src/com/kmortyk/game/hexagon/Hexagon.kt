package com.kmortyk.game.hexagon

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.GridPosition
import com.kmortyk.game.Position
import com.kmortyk.game.item.Item
import com.kmortyk.game.person.Person
import com.kmortyk.game.person.PersonState
import com.kmortyk.game.scenery.Scenery
import com.kmortyk.game.state.GameState
import java.lang.RuntimeException

class Hexagon {
    constructor(hexID: Int, row: Int, col: Int) {
        if(hexID !in Assets.Hexes)
            throw RuntimeException("unknown hexID $hexID")

        this.hexID = hexID
        this.position = Position(row, col)
        val serialized = Assets.Hexes[hexID]!!
        val drawable = serialized.drawable
        this.texture = Assets.atlas("hexes").findRegion(drawable)
        canStep = serialized.can_step
    }

    constructor(row: Int, col: Int, texture: TextureRegion) {
        this.texture = texture
        this.position = Position(row, col)
    }

    var hexID: Int = 100

    // position - position of the hexagon at world and screen
    // TODO change to rectangle ?
    var position: Position

    // actions that hexagon can perform
    val actions: MutableList<HexagonAction> = mutableListOf()
    // textures of actions need to be drawn
    val actionTexturesToDraw: MutableList<AssetDescriptor<Texture>> = mutableListOf()

    // fun for convenience
    fun row() = position.row()

    // fun for convenience
    fun col() = position.col()

    fun centerX() = position.actualX() + Assets.HexWidth * 0.5f

    fun centerY() = position.actualY() + Assets.HexHeight * 0.5f

    fun center() = Vector2(centerX(), centerY())

    // fun for convenience
    fun setPosition(row: Int, col: Int) = position.setGrid(row, col)

    fun gridPosition(): GridPosition = position.grid

    // texture descriptor for the hexagon
    private var texture: TextureRegion

    // can player step on this hexagon ?
    private var canStep: Boolean = true

    // person slot for game purposes
    var personSlot: Person? = null
    var itemSlots: MutableList<Item> = mutableListOf()
    var scenerySlots: MutableList<Scenery> = mutableListOf()

    // hexagon draw
    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(texture, position.actualX(), position.actualY())
    }

    // hexagon draw
    fun drawDebug(texture: Texture, spriteBatch: SpriteBatch) {
        spriteBatch.draw(texture, position.actualX(), position.actualY())
    }

    // performAction - performs some implemented (or not) action on the person or items
    // example: `lava hex` burns items or person
    fun triggerActions(gameState: GameState) {
        for(action in actions) {
            action.executeOn(gameState, this)
        }
    }

    // checks if point within bounds of this hexagon
    fun contains(worldX: Int, worldY: Int) : Boolean {
        val left = position.actualX()
        val right = left + Assets.HexWidth

        val top = position.actualY()
        val bottom = top + Assets.HexHeight

        return (worldX > left && worldX < right) &&
                (worldY > top && worldY < bottom)
    }

    fun blocked(): Boolean = !canStep || hasPerson() || blockedByScenery()

    fun blockedByScenery(): Boolean {
        for(sc in scenerySlots)
            if(sc.blocksWay) return true
        return false
    }

    fun hasPerson(): Boolean = personSlot != null

    fun hasTalkingPerson(): Boolean =
            hasPerson() && (personSlot!!.state != PersonState.Aggressive) && (personSlot!!.dialog != null)

    fun hasEnemyPerson() : Boolean =
            hasPerson() && (personSlot!!.state == PersonState.Aggressive)

    fun hasItems(): Boolean = itemSlots.size > 0

    override fun toString(): String {
        return "[$hexID]"
    }

    fun addAction(action: HexagonAction, mainGame: Boolean) {
        actions.add(action)
        if(mainGame && action.gameDrawable() != null) {
            actionTexturesToDraw.add(
                Assets[action.gameDrawable()!!]
            )
        }
    }
}