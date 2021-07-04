package com.kmortyk.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.kmortyk.game.effect.MoveCameraEffect
import com.kmortyk.game.input.EditorInputAdapter
import com.kmortyk.game.input.GameInputAdapter
import com.kmortyk.game.input.MainMenuInputAdapter
import com.kmortyk.game.map.MiniMap
import com.kmortyk.game.map.editor.EditorState
import com.kmortyk.game.person.Dialog
import com.kmortyk.game.script.ScriptEngine
import com.kmortyk.game.sound.SoundMaster
import com.kmortyk.game.state.ControlState
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.DarkGameSkin
import com.kmortyk.game.ui.GameSkin
import com.kmortyk.game.ui.element.Border
import com.kmortyk.game.ui.screens.GameUI
import com.kmortyk.game.ui.screens.MainMenuUI
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.screens.EditorUI
import kotlin.math.abs

interface DrawFun {
    fun create()
    fun draw()
    fun resize()
    fun open()
}

// draw main menu
class MainMenuDrawFun(private val assetManager: AssetManager, private val game: PrettyCoolGame) : DrawFun {
    lateinit var inputAdapter: InputAdapter
    lateinit var mainMenuUI: MainMenuUI

    // sprite batch for draw this ui
    private var gameUiSpriteBatch: SpriteBatch = SpriteBatch()

    override fun create() {
        resize()
        if(PrettyCoolGame.Debug && PrettyCoolGame.DisableMainMenuMusic) {
            /// DEBUG
        } else {
            SoundMaster.playMusic("main_menu")
        }
    }

    override fun draw() {
        gameUiSpriteBatch.projectionMatrix = game.originalMatrix

        mainMenuUI.draw(assetManager)
    }

    override fun resize() {
        mainMenuUI = MainMenuUI(gameUiSpriteBatch, game.camera.viewportWidth, game.camera.viewportHeight, game, assetManager)
        // initialize game's input
        inputAdapter = MainMenuInputAdapter(game, game.camera, mainMenuUI)
        Gdx.input.inputProcessor = inputAdapter
    }

    override fun open() {
        Gdx.input.inputProcessor = inputAdapter
    }
}

// draw actual game
class GameDrawFun(private val spriteBatch: SpriteBatch,
                  private val assetManager: AssetManager,
                  private val game: PrettyCoolGame,
                  private val camera: Camera) : DrawFun {

    var hasState: Boolean = false

    // sprite batch for draw this ui
    var gameUiSpriteBatch: SpriteBatch = SpriteBatch()

    lateinit var inputAdapter: InputAdapter
    lateinit var gameState: GameState
    lateinit var gameUI: GameUI

    lateinit var scriptEngine: ScriptEngine

    val uiCamera : Camera = OrthographicCamera(game.camera.viewportWidth, game.camera.viewportHeight)
    var gameSkin: GameSkin = DarkGameSkin

    override fun create() {
        // initialize script engine
        scriptEngine = ScriptEngine(game)

        // create and initialize game state
        gameState = GameState(game, scriptEngine)
        gameState.createGameMap()
        hasState = true

        // create game ui
        resize()
    }

    override fun draw() {
        // draw current state on the screen
        spriteBatch.begin()
        gameState.draw(assetManager, spriteBatch)
        game.processEffects()

        /// DEBUG draw bounds of the camera
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawCameraBounds) {
            val camBounds = cameraBounds()

            if(border == null)
                border = Border(camBounds, useBatchMatrix = true)
            border?.draw(assetManager, spriteBatch)
            spriteBatch.draw(assetManager[Assets["ui_debug_actual_pos"]], game.camera.position.x, game.camera.position.y)
            spriteBatch.draw(assetManager[Assets["ui_debug_grid_pos"]], camBounds.x, camBounds.y)
        }

        spriteBatch.end()

        gameUiSpriteBatch.projectionMatrix = game.originalMatrix

        // draw ui
        gameUI.draw(assetManager)
    }

    override fun resize() {
        val inv = Matrix4().set(game.originalMatrix).inv()
        uiCamera.invProjectionView.set(inv)
        uiCamera.combined.set(Matrix4().set(game.originalMatrix))

        //val uiCam = OrthographicCamera(game.camera.viewportWidth, game.camera.viewportHeight)
        //uiCam.invProjectionView.set(Matrix4().set(game.originalMatrix).inv())

        gameUI = GameUI(game, gameUiSpriteBatch, gameSkin, camera.viewportWidth, camera.viewportHeight, GameCamera(uiCamera, game.camera), gameState, assetManager)
        gameUI.addElements(MiniMap(
            camera = camera,
            uiCamera = uiCamera,
            gameMap = gameState.gameMap,
            player = gameState.player,
            mapBounds = gameUI.findElementByName("mapBounds")!!))

        inputAdapter = GameInputAdapter(game, game.camera, uiCamera, gameUI, gameState)
        Gdx.input.inputProcessor = inputAdapter

        // move camera to see player
        focusCameraOnPlayer()
    }

    override fun open() {
        Gdx.input.inputProcessor = inputAdapter
    }

    fun focusCameraOnPlayer() {
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawCameraBounds) {
            game.addEffect(MoveCameraEffect(
                game.camera,
                null,
                gameState.player.position.actualX(),
                gameState.player.position.actualY()))
        } else {
            game.addEffect(MoveCameraEffect(
                game.camera,
                cameraBounds(),
                gameState.player.position.actualX(),
                gameState.player.position.actualY()))
        }
    }

    fun openDialog(dialog: Dialog) { gameUI.openDialog(dialog) }

    fun uiBackward() { gameUI.backward() }

    fun updateControlState(controlState: ControlState) {
        val el = gameUI.findElementByName("weaponBacking")!! as TextureBacking
        when(controlState) {
            ControlState.TouchToWalk   -> { el.texture = game.assetManager[Assets["ui_weapon_backing"]] }
            ControlState.TouchToAttack -> { el.texture = game.assetManager[Assets["ui_weapon_backing_selected"]] }
        }
    }

    fun viewPortWidth() : Float = camera.viewportWidth

    fun viewPortHeight() : Float = camera.viewportHeight

    val cameraBounds: Rectangle = Rectangle()
    var border: Border? = null

    fun cameraBounds() : Rectangle {
        // set to non-black bounds
        cameraBounds.set(Assets.HexHorizontalOffset,
            Assets.HexVerticalOffset,
            gameState.gameMap.maxWidth() - 2*Assets.HexHorizontalOffset,
            gameState.gameMap.maxHeight() - 2*Assets.HexVerticalOffset)

        // offset by camera max
        cameraBounds.setPosition(
            cameraBounds.x + viewPortWidth() * 0.5f,
            cameraBounds.y + viewPortHeight() * 0.5f)

        // offset size by twice as half sizes
        cameraBounds.setSize(
            cameraBounds.width - viewPortWidth(),
            cameraBounds.height - viewPortHeight()
        )

        return cameraBounds
    }
}

// draw map editor
class MapEditorDrawFun(private val spriteBatch: SpriteBatch, private val game: PrettyCoolGame, private val assetManager: AssetManager) : DrawFun {

    lateinit var editor: EditorState
    lateinit var editorUI: EditorUI
    lateinit var inputAdapter: InputAdapter

    // sprite batch for draw this ui
    private var gameUiSpriteBatch: SpriteBatch = SpriteBatch()

    override fun create() {
        resize()

        SoundMaster.stopMusic()
    }

    override fun draw() {
        spriteBatch.begin()
        editor.draw(assetManager, spriteBatch)
        spriteBatch.end()

        gameUiSpriteBatch.projectionMatrix = game.originalMatrix

        // draw ui
        editorUI.draw(assetManager)
    }

    override fun resize() {
        val uiCam = OrthographicCamera(game.camera.viewportWidth, game.camera.viewportHeight)

        uiCam.invProjectionView.set(Matrix4().set(game.originalMatrix).inv())
        uiCam.combined.set(Matrix4().set(game.originalMatrix))

        editor = EditorState(game.camera, game)
        editorUI = EditorUI(game.camera.viewportWidth, game.camera.viewportHeight, GameCamera(uiCam, game.camera), game, gameUiSpriteBatch, editor, assetManager)

        // initialize game's input
        inputAdapter = EditorInputAdapter(game, game.camera, uiCam, editorUI, editor)
        Gdx.input.inputProcessor = inputAdapter
    }

    override fun open() {
        Gdx.input.inputProcessor = inputAdapter
    }
}