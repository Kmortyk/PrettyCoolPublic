package com.kmortyk.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.kmortyk.game.effect.*
import com.kmortyk.game.script.ScriptEngine
import java.util.*

class PrettyCoolGame : ApplicationAdapter() {
    companion object {
        const val ScreenWidth = 640*2
        const val ScreenHeight = 480*2

        const val Version = "v0.21"

        // debug consts
        const val Debug = true
        const val ShowUIBounds = false
        const val DrawPlayerPos = false
        const val DrawActualPos = false
        const val DrawObservableHexes = false
        const val DrawHexCenter = false
        const val FastLoad      = true
        const val EditorDebugHexes = false
        const val DisableMainMenuMusic = true
        const val DisableAI = true

        const val DrawTouchedPosMenu = false
        const val DrawTouchedPosGame = false
        const val DrawTouchedPosEditor = false

        const val DrawScissors = false
        const val MapPlayroom = false

        const val DrawCameraBounds = false

        // options
        var HexWidthAdjustment = false

        // logs
        const val LOG_ADDED_EFFECTS = false

        // layer indexes
        const val LayerFar = 0
        const val LayerDefault = 1
        const val LayerNear = 2
    }

    /// MAIN DRAW VARS
    lateinit var assetManager: AssetManager
    lateinit var spriteBatch: SpriteBatch
    lateinit var camera: Camera
    lateinit var viewport: Viewport

    lateinit var originalMatrix: Matrix4

    /// EFFECTS
    val effects: MutableList<Effect> = LinkedList()
    val constEffects: Array<MutableMap<String, ConstEffect>> = arrayOf(mutableMapOf(), mutableMapOf(), mutableMapOf())

    /// DRAW FUN
    lateinit var drawFun: DrawFun
    val drawFunsState: MutableMap<String, DrawFun> = mutableMapOf()

    fun openGame() {
        if(!drawFunsState.containsKey("game")) {
            drawFun = GameDrawFun(spriteBatch, assetManager, this, camera)
            drawFun.create()
            drawFunsState["game"] = drawFun
        } else {
            drawFun = drawFunsState["game"]!!
            drawFun.open()
        }
    }

    fun openMenu() {
        if(!drawFunsState.containsKey("menu")) {
            drawFun = MainMenuDrawFun(assetManager, this)
            drawFun.create()
            drawFunsState["menu"] = drawFun
        } else {
            drawFun = drawFunsState["menu"]!!
            drawFun.open()
        }
    }

    fun openEditor() {
        if(!drawFunsState.containsKey("editor")) {
            drawFun = MapEditorDrawFun(spriteBatch, this, assetManager)
            drawFun.create()
            drawFunsState["editor"] = drawFun
        } else {
            drawFun = drawFunsState["editor"]!!
            drawFun.open()
        }
    }

    // prepare all asset utils
    override fun create() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // load game assets
        assetManager = AssetManager()
        Assets.load(assetManager)

        // create camera
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        camera = OrthographicCamera(screenWidth, screenHeight)
        viewport = ExtendViewport(640.0f, 480.0f, camera)
        viewport.apply()

        originalMatrix = Matrix4(camera.combined)

        // create batch TODO batch manager ?
        spriteBatch = SpriteBatch()
        spriteBatch.enableBlending()

        openMenu()
    }

    override fun resize(width: Int, height: Int) {
        // update viewport
        viewport.update(width, height, true)
        viewport.apply()

        // reset const effects after resize
        for(layer in constEffects)
            for((_, effect) in layer)
                effect.create()

        originalMatrix = Matrix4(camera.combined)

        // drawFun resize
        drawFun.resize()
    }

    // draw all objects
    override fun render() {
        camera.update()
        spriteBatch.projectionMatrix = camera.combined

        // clear screen off the previous frame
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // drawFun draw
        drawFun.draw()
    }

    // dispose on game pause or stop
    override fun dispose() {
        spriteBatch.dispose()
        assetManager.dispose()
        Assets.dispose()
    }

    fun addEffect(effect: Effect) {
        if(LOG_ADDED_EFFECTS)
            log.info("added effect $effect")
        effects.add(effect)
    }

    fun putConstEffect(effectName: String, effect: ConstEffect, layer: Int = LayerDefault) {
        effect.create()
        constEffects[layer].put(effectName, effect)
    }

    fun getConstEffect(effectName: String) : ConstEffect {
        for(layer in constEffects) {
            if(effectName in layer) {
                return layer[effectName]!!
            }
        }
        log.error("effect $effectName not found")
        return EmptyEffect()
    }

    fun processEffects() {
        for(effectIdx in 0 until effects.size) {
            // check if effects is still presented
            if(effectIdx >= effects.size) { return }
            // pick next effect
            val effect = effects[effectIdx]
            // remove if time is expired
            if(!effect.extend(Gdx.graphics.deltaTime)) {
                effects.removeAt(effectIdx)
            // otherwise draw effect
            } else {
                effect.draw(assetManager, spriteBatch)
            }
        }

        for(layer in constEffects) {
            for((_, effect) in layer) {
                effect.extend(Gdx.graphics.deltaTime)
                effect.draw(assetManager, spriteBatch)
            }
        }
    }

    fun gameDrawFun() : GameDrawFun {
        return drawFun as GameDrawFun
    }
}