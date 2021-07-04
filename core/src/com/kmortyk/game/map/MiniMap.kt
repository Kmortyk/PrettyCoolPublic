package com.kmortyk.game.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.person.Person
import com.kmortyk.game.person.PersonState
import com.kmortyk.game.ui.element.Border
import com.kmortyk.game.ui.screens.GameUI
import com.kmortyk.game.ui.element.InterfaceElement

class MiniMap(
    private val camera: Camera,
    private val uiCamera: Camera,
    private val gameMap: GameMap,
    private val player: Person,
    mapBounds: InterfaceElement) :

    InterfaceElement() {

    companion object {
        const val iconWidth = 13
        const val iconHeight = 11

        const val iconHorizontalOffset = iconWidth * 0.5f
        const val iconVerticalOffset = 0.0f
    }
    private val iconHexStone = Assets.atlas("map_icons").findRegion("hex_stone_icon")
    private val iconPlayer   = Assets.atlas("map_icons").findRegion("hex_player_icon")
    private val iconThreat   = Assets.atlas("map_icons").findRegion("hex_threat_icon")
    private val iconItem     = Assets.atlas("map_icons").findRegion("hex_item_icon")
    private val iconPerson   = Assets.atlas("map_icons").findRegion("hex_prs_icon")

    private lateinit var border: Border

    private val cam: Camera = OrthographicCamera(uiCamera.viewportWidth, uiCamera.viewportHeight)
    private val miniMapBounds: Rectangle
    private val scissors = Rectangle()

    init {
        val p1 = camera.project(Vector3(mapBounds.bounds.x, mapBounds.bounds.y, 0.0f))
        val p2 = camera.project(Vector3(mapBounds.bounds.x + mapBounds.width(), mapBounds.bounds.y + mapBounds.height(), 0.0f))

        val b = Rectangle(0.0f, 0.0f, p2.x, p2.y)
        val offset = 10

        miniMapBounds = Rectangle(
            b.x + offset,
            b.y + offset,
            b.width - 2*offset,
            b.height - 2*offset
        )


        border = Border(miniMapBounds, borderColor = Color.BLUE)

        cam.translate(cam.viewportWidth*0.5f, cam.viewportHeight*0.5f, 0.0f)
        cam.update()
    }

    val pltPrjPos = Vector3()

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        ScissorStack.calculateScissors(
            cam,
            0.0f, 0.0f,
            cam.viewportWidth, cam.viewportHeight,
            uiCamera.view, miniMapBounds, scissors)

        spriteBatch.flush() // save drawn image

        if(!ScissorStack.pushScissors(scissors)) {
            println("[ERROR] can't push mini map scissors")
        }

        pltPrjPos.set(player.position.actualX(), player.position.actualY(), 0.0f)
        camera.project(pltPrjPos)

        val centeringOffsetX = GameUI.DefaultPadding + GameUI.MiniMapSize*0.4f - pltPrjPos.x * (iconWidth / Assets.HexWidth)
        val centeringOffsetY = GameUI.DefaultPadding + GameUI.MiniMapSize*0.4f - pltPrjPos.y * (16 / Assets.HexHeight)

        gameMap.hexes.forEach {
            for(hex in it) {
                val x = hex.col() * iconWidth + (iconHorizontalOffset * (hex.row() % 2)) + centeringOffsetX
                val y = hex.row() * iconHeight - iconVerticalOffset + centeringOffsetY

                when {
                    hex.hasPerson() && hex.personSlot == player -> spriteBatch.draw(iconPlayer, x, y)
                    hex.hasPerson() && hex.personSlot != player && hex.personSlot!!.state == PersonState.Aggressive -> spriteBatch.draw(iconThreat, x, y)
                    hex.hasPerson() && hex.personSlot != player && hex.personSlot!!.state != PersonState.Aggressive -> spriteBatch.draw(iconPerson, x, y)
                    hex.hasItems() -> spriteBatch.draw(iconItem, x, y)
                    else -> spriteBatch.draw(iconHexStone, x, y)
                }
            }
        }

        spriteBatch.flush()

        ScissorStack.popScissors()

        /// DEBUG show ui bounds
        if(PrettyCoolGame.Debug && PrettyCoolGame.ShowUIBounds) {
            border.draw(assetManager, spriteBatch)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean { return false }
}