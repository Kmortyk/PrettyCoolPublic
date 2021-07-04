package com.kmortyk.game.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.LogColors
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.animation.Particle
import com.kmortyk.game.effect.Callback
import com.kmortyk.game.effect.GradualRainEffect
import com.kmortyk.game.log
import com.kmortyk.game.ui.bottom
import com.kmortyk.game.ui.element.*
import com.kmortyk.game.ui.element.special.EffectElement
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.left
import com.kmortyk.game.ui.right
import java.util.*

class MainMenuUI(val gameUiSpriteBatch: SpriteBatch,
    private val screenWidth: Float, private val screenHeight: Float,
                 private val game: PrettyCoolGame, private val assetManager: AssetManager) : ElementsGroup() {

    // all view's history for returning back
    private val viewsStack: Stack<GameUIState> = Stack()
    // current ui state type
    private var state: GameUIStateType = GameUIStateType.Main

    private val stars: MutableList<Particle> = mutableListOf()

    fun backward() {
        if(viewsStack.empty())
            return // nothing to back

        val prevUIState = viewsStack.pop()

        state = prevUIState.state
        elements = prevUIState.elements
    }

    private fun forward(newState: GameUIStateType) {
        if(state == newState)
            return // already in this state

        // save current state
        viewsStack.push(GameUIState(state, elements))

        state = newState
        elements = mutableListOf()
    }

    init {
        elements = mainMenu()
    }

    fun draw(assetManager: AssetManager) {
        gameUiSpriteBatch.begin()
        super.onDraw(assetManager, gameUiSpriteBatch)
        gameUiSpriteBatch.end()
    }

    // -- UIs ----------------------------------------------------------------------------------------------------------
    private fun mainMenu() : MutableList<InterfaceElement>  {
        val elements: MutableList<InterfaceElement> = mutableListOf()

        addStars(elements)

        // book view
        val tex = assetManager[Assets["ui_main_menu"]]
        val cx = screenWidth * 0.5f - tex.width * 0.5f
        val cy = screenHeight * 0.5f - tex.height * 0.5f

        val textPadding = GameUI.DefaultPadding *3

        val top = cy + tex.height

        val backing = TextureBacking(tex, cx, cy)
        addElementTo(elements, "book_backing", backing)

        // title
        // play game button
        val title = TextElement(Assets.FontKurale, "Pretty Cool, " + PrettyCoolGame.Version, screenWidth,
            backing.bounds.left() + GameUI.DefaultPadding *3, top - TextElement.lineHeight(Assets.FontKurale)*1.5f, Color.FIREBRICK, Runnable {})
        addElementTo(elements, "title", title)

        // play game button
        val playGameButton = TextElement(Assets.FontKurale, "Open book", screenWidth,
                cx + textPadding, top - TextElement.lineHeight(Assets.FontKurale)*3.7f, Color.BLACK, Runnable {
            stopStars()
            if(PrettyCoolGame.Debug && PrettyCoolGame.FastLoad) {
                game.openGame()
            } else {
                hideElementsWithNames(
                        "map_editor_button",
                        "map_editor_text",
                        "plant1", "plant2")
                elements.add(EffectElement(GradualRainEffect(), Callback {
                    game.openGame()
                }))
            }
        })
        addElementTo(elements, "play_game_button", playGameButton)

        // options button
        val optionsButton = TextElement(Assets.FontKurale, "Operate", screenWidth,
                cx + textPadding, top - TextElement.lineHeight(Assets.FontKurale)*5.7f, Color.BLACK, Runnable {
            // run game
        })
        addElementTo(elements, "options_button", optionsButton)

        // exit button
        val exitButton = TextElement(Assets.FontKurale, "Leave", screenWidth,
                cx + textPadding, top - TextElement.lineHeight(Assets.FontKurale)*7.7f, Color.BLACK, Runnable {
            Gdx.app.exit()
        })
        addElementTo(elements, "exit_button", exitButton)

        // map editor button
        val mapEditorTex = assetManager[Assets["ui_map_editor"]]
        val mapEditorX = cx
        val mapEditorY = cy - GameUI.DefaultPadding *2 - mapEditorTex.height
        val mapEditorButton = TextureButton(mapEditorX, mapEditorY, mapEditorTex, null)
        addElementTo(elements, "map_editor_button", mapEditorButton)
        val mapEditorText = TextElement(Assets.FontKurale, "Map editor", screenWidth,
                mapEditorX + GameUI.DefaultPadding, mapEditorY + mapEditorTex.height - GameUI.DefaultPadding *2, Color.DARK_GRAY, Runnable {
            game.openEditor()
        }, padding = 0.0f)
        addElementTo(elements, "map_editor_text", mapEditorText)

        // decorations
        val plant = Assets.scaleTexture(assetManager[Assets["ui_plant_green_grass"]], 0.5f)
        addElementTo(elements, "plant1", TextureBacking(plant, backing.bounds.x - plant.width, backing.bounds.bottom() + GameUI.DefaultPadding*2))
        addElementTo(elements, "plant2", TextureBacking(plant, backing.bounds.right(), backing.bounds.bottom() + GameUI.DefaultPadding*2))

        return elements
    }

    // -- Scripts ------------------------------------------------------------------------------------------------------
    private fun addStars(elements: MutableList<InterfaceElement>) {
        val starsCols = 10
        val starsRows = 10

        val stepX = screenWidth / starsCols.toFloat()
        val stepY = screenHeight / starsRows.toFloat()

        log.info("main_menu_stars_count=${LogColors.BLUE}${starsCols*starsRows}${LogColors.RESET}")

        val rand = Random(10_231)

        for (i in 0 .. starsRows) {
            for(j in 0 .. starsCols) {
                if(rand.nextFloat() > 0.5f) {
                    val signX = if(rand.nextFloat() > 0.5) 1 else -1
                    val signY = if(rand.nextFloat() > 0.5) 1 else -1

                    val randOffsetX = signX * rand.nextFloat() * stepX
                    val randOffsetY = signY * rand.nextFloat() * stepY

                    val particle = Particle("ui_main_menu_star",
                            i*stepX + randOffsetX,
                            j*stepY + randOffsetY)
                    particle.randomOffset()
                    elements.add(particle)
                    stars.add(particle)
                }
            }
        }
    }

    private fun stopStars() {
        for(star in stars) {
            star.normalizeDuration()
            star.setPlayMode(Animation.PlayMode.NORMAL)
        }
    }
}