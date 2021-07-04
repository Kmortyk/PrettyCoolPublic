package com.kmortyk.game.item

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.beust.klaxon.JsonObject
import com.kmortyk.game.Assets
import com.kmortyk.game.Position
import com.kmortyk.game.item.actions.*
import com.kmortyk.game.log
import com.kmortyk.game.sound.SoundMaster
import com.kmortyk.game.state.GameState

enum class ItemType {
    ActivateOnUISelect,
    ActivateOnWorldUse,
    ActivateOnInventoryUse,
    ActivateOnPickUp,
    Resource,
    None
}

enum class ItemResourceCategory {
    one,
    some,
    many
}

// Item - is game object that can be used in some way
// gameState is needed for actions loading
class Item(gameState: GameState, val name: String, row: Int, col: Int, count: Int) {

    constructor(gameState: GameState, itemName: String) :
            this(gameState, itemName, 0, 0, 1)

    // count of that item
    var count: Int = 1

    // texture descriptor for the item
    var texture: AssetDescriptor<Texture> = AssetDescriptor<Texture>("", Texture::class.java)

    // position of the item at map and the screen
    val position: Position = Position()

    val type: ItemType

    val hasView: Boolean

    // actionsList - list that contains added actions
    private val actionsList: MutableList<ItemAction> = mutableListOf()

    // oneTimeActionsList - list that contains one-time actions
    private val oneTimeActionsList: MutableList<ItemAction> = mutableListOf()

    init {
        // update current item position
        position.setGrid(row, col)

        // get serialized representation
        val item = Assets.Items[name]!!

        // parse item type
        type = parseItemType(item.type)

        // set count
        this.count = count

        if(isResource()) {
            val categories = item.resource_category

            when {
                count > categories[ItemResourceCategory.many.toString()]!!.count ->
                    this.texture = Assets["img/${categories[ItemResourceCategory.many.toString()]!!.drawable}"]
                count > categories[ItemResourceCategory.some.toString()]!!.count ->
                    this.texture = Assets["img/${categories[ItemResourceCategory.some.toString()]!!.drawable}"]
                else -> {
                    this.texture = Assets["img/${categories[ItemResourceCategory.one.toString()]!!.drawable}"]
                }
            }
        } else {
            // get texture from assets
            this.texture = Assets["img/${item.drawable}"]
        }

        hasView = item.has_view
        // load actions by their names
        for(act in item.actions) {
            loadActionByName(gameState, act)
        }
    }

    // addAction - adds [not empty] action to execute with item use
    fun addAction(action: ItemAction) = actionsList.add(action)

    fun actions() : List<ItemAction> = actionsList

    // addOneTimeAction - adds [not empty] one-time action that executes only once
    fun addOneTimeAction(oneTimeAction: ItemAction) = oneTimeActionsList.add(oneTimeAction)

    fun isResource() = type == ItemType.Resource

    // applyActions - executes all stored actions
    fun executeActions(gameState: GameState, row: Int, col: Int) {
        if(type == ItemType.None) {
            log.error("item can't be executed: unknown item type")
            return
        }
        // execute all ordinary actions
        for(act in actionsList)
            act.executeOn(gameState, row, col)
        // execute all one-time actions and clear
        if(oneTimeActionsList.size > 0) {
            for(act in oneTimeActionsList)
                act.executeOn(gameState, row, col)
            oneTimeActionsList.clear()
        }
    }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val texture = assetManager[texture]
        spriteBatch.draw(texture,
                position.actualX() + (Assets.HexWidth - texture.width)*0.5f,
                position.actualY() + (Assets.HexHeight - texture.height)*0.5f)
    }

    private fun loadActionByName(gameState: GameState, obj: JsonObject) {
        when(obj.string("name")) {
            "heal" -> addAction(HealAction(gameState.player, obj.int("amount")!!))
            "attack" -> addAction(AttackAction(gameState.player, obj.int("power")!!, obj.int("max_distance")!!,
                obj.string("ammo")))
            "play_animation" -> addAction(PlayAnimationAction(gameState.player, obj.string("animation_name")!!))
            "bullet_effect" -> addAction(BulletEffectAction(gameState.player, obj.string("bullet_drawable")!!))
            "sound" -> addAction(SoundAction(SoundMaster, obj.string("sound_name")!!))
        }
    }

    private fun parseItemType(v: String) : ItemType {
        return when (v) {
            "select"    -> ItemType.ActivateOnUISelect
            "world_use" -> ItemType.ActivateOnWorldUse
            "inv_use"   -> ItemType.ActivateOnInventoryUse
            "pick_up"   -> ItemType.ActivateOnPickUp
            "resource"  -> ItemType.Resource
            else        -> {
                log.error("unknown item type $v");
                ItemType.None
            }
        }
    }

    override fun toString(): String {
        return "Item(itemName='$name', position=$position)"
    }
}