package com.kmortyk.game.scenery

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.Position
import java.lang.RuntimeException

class Scenery(val name: String, row: Int, col: Int) {

    private val tex: AssetDescriptor<Texture>
    private val offset: Vector2

    // whether this scenery blocks person's way or not
    val blocksWay: Boolean

    // position of the item at map and the screen
    val position: Position = Position()

    init {
        if(name !in Assets.Scenery) {
            throw RuntimeException("unknown scenery name '$name'")
        }
        val serializedScenery = Assets.Scenery[name]!!
        tex = Assets["img/${serializedScenery.drawable}"]
        offset = Vector2(serializedScenery.offset_x, serializedScenery.offset_y)
        blocksWay = serializedScenery.blocks_way
        position.setGrid(row, col)
    }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val texture = assetManager[tex]
        spriteBatch.draw(texture,
                position.actualX() + (Assets.HexWidth - texture.width)*0.5f + offset.x,
                position.actualY() + (Assets.HexHeight - texture.height)*0.5f + offset.y)
    }
}