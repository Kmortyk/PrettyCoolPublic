package com.kmortyk.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.GdxRuntimeException
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.StringReader


data class SerializedHexagon(val map_id: Int, val drawable: String, val can_step: Boolean = true)
data class SerializedPerson(val name: String, val drawable: String, val state: String, val barehands_power: Int, val init_hp: Int, val dialog: String? = null)
data class SerializedItem(val name: String, val drawable: String, val type: String, val actions: MutableList<JsonObject>, val has_view: Boolean = false,
                          val resource_category: Map<String, SerializedItemResource> = mapOf())
data class SerializedItemResource(val drawable: String, val count: Int)
data class SerializedScenery(val name: String, val drawable: String, val offset_x: Float, val offset_y: Float, val blocks_way: Boolean = false)
data class SerializedAnimation(val name: String, val atlas: String, val keyframes: Array<String>, val type: String, val duration: Float)
data class SerializedParticle(val name: String, val type: String, val duration: Float)
data class SerializedQuest(val questID: String, val personID: String, val type: String, val name: String, val description: String, val keys: Map<String, String>)

class Assets {
    companion object {

        // Metrics
        //val HexWidth = if(PrettyCoolGame.HexWidthAdjustment) 45.0f else 46.0f
        //const val HexHeight = 47.0f

        val HexWidth = if(PrettyCoolGame.HexWidthAdjustment) 44.0f else 45.0f
        const val HexHeight = 45.0f

        const val NullHex = 100

        //const val HexHorizontalOffset = HexWidth / 2f
        //const val HexVerticalOffset = HexHeight / 3f

        val HexHorizontalOffset = HexWidth * 0.5f
        const val HexVerticalOffset = 12.0f

        // Fonts
        lateinit var FontKurale: BitmapFont
        lateinit var FontKurale12: BitmapFont
        lateinit var FontTimes: BitmapFont
        lateinit var FontTimes24: BitmapFont
        lateinit var FontTimes18: BitmapFont
        lateinit var FontTimes14: BitmapFont
        lateinit var FontTimes12: BitmapFont

        val Hexes: MutableMap<Int, SerializedHexagon> = mutableMapOf()
        val Persons: MutableMap<String, SerializedPerson> = mutableMapOf()
        val Items: MutableMap<String, SerializedItem> = mutableMapOf()
        val Scenery: MutableMap<String, SerializedScenery> = mutableMapOf()
        val Animations: MutableMap<String, SerializedAnimation> = mutableMapOf()
        val Particles: MutableMap<String, SerializedParticle> = mutableMapOf()
        val Quests: MutableMap<String, SerializedQuest> = mutableMapOf()

        // Drawable TODO downloadable content ?
        val assetsNames = mapOf(
            // UI
            "ui_heart" to "img/herohp4.png",
            "ui_heart_full" to "img/herohpfull11.png",
            "ui_quick_slot" to "img/ui_quick_slot2.png",
            "ui_quick_slot_selected" to "img/ui_quick_slot_selected2.png",

            "ui_perks" to "img/ui_circle_perks4.png",
            "ui_inventory" to "img/ui_circle_inv4.png",
            "ui_quests" to "img/ui_circle_quests2.png",

            "ui_inventory_cell" to "img/inv_cell.png",
            "ui_inventory_cell_selected" to "img/inv_cell_selected4.png",
            "ui_inventory_cell_quick_slot" to "img/inv_cell_quick_slot6.png",
            "ui_dialog_icon" to "img/dialog_icon.png",
            "ui_question_icon" to "img/question_icon.png",
            "ui_exclamation_icon" to "img/exclamation_icon.png",
            "ui_weapon_backing" to "img/ui_weapon_backing3.png",
            "ui_weapon_backing_hand" to "img/ui_weapon_backing_hand14.png",
            "ui_settings_button" to "img/ui_settings_button4.png",
            "ui_main_menu" to "img/ui_main_menu14.png",
            "ui_perks_tree_arrow" to "img/ui_perks_tree_arrow.png",
            "ui_weapon_backing_selected" to "img/ui_weapon_backing_selected2.png",
            "ui_map_editor" to "img/ui_map_editor3.png",
            "ui_debug_grid_pos" to "img/ui_debug_grid_pos.png",
            "ui_debug_actual_pos" to "img/ui_debug_actual_pos.png",
            "ui_lvlup" to "img/ui_lvlup5.png",
            "ui_nwqst" to "img/ui_nwqst2.png",

            // Editor
            "ui_editor_brush" to "img/ui_editor_brush.png",
            "ui_editor_eraser" to "img/ui_editor_eraser.png",
            "ui_editor_touch_to_paint_button_empty" to "img/ui_editor_touch_to_paint_button_empty.png",
            "ui_editor_touch_to_paint_button_filled" to "img/ui_editor_touch_to_paint_button_filled2.png",
            "ui_editor_save_map" to "img/ui_editor_save_map.png",
            "ui_editor_create_map" to "img/ui_editor_create_map2.png",

            "ui_editor_pane_hexes" to "img/ui_editor_pane_hexes5.png",
            "ui_editor_pane_items" to "img/ui_editor_pane_items3.png",
            "ui_editor_pane_persons" to "img/ui_editor_pane_persons3.png",
            "ui_editor_pane_scenery" to "img/ui_editor_pane_scenery7.png",

            "ui_editor_pane_hexes_filled" to "img/ui_editor_pane_hexes_filled.png",
            "ui_editor_pane_items_filled" to "img/ui_editor_pane_items_filled.png",
            "ui_editor_pane_persons_filled" to "img/ui_editor_pane_persons_filled.png",
            "ui_editor_pane_scenery_filled" to "img/ui_editor_pane_scenery_filled7.png",

            "ui_editor_create_map_decrease_size" to "img/ui_editor_create_map_decrease_size2.png",
            "ui_editor_create_map_increase_size" to "img/ui_editor_create_map_increase_size2.png",
            "ui_editor_create_map_create" to "img/ui_editor_create_map_create.png",

            "ui_editor_save_map_from_disk" to "img/ui_editor_save_map_from_disk.png",
            "ui_editor_save_map_to_disk" to "img/ui_editor_save_map_to_disk.png",
            "ui_editor_save_map_to_server" to "img/ui_editor_save_map_to_server.png",

            "ui_editor_save_map_choose_dirs" to "img/ui_editor_save_map_choose_dirs6.png",

            "ui_perk_slot" to "img/ui_perk_slot.png",
            "ui_hero_portrait" to "img/ui_hero_portrait.png",

            // Hexes
            "hex_debug" to "img/hex_debug.png",
            "hex_exit" to "img/hex_exit3.png",
            "hex_act_err" to "img/hex_act_err.png",

            // Weapons
            "pipe_view" to "img/wv_pipe_view7.png",
            "pistol_view" to "img/wv_pistol_view20.png",
            "hand_grenade_launcher_view" to "img/wv_hand_grenade_launcher_view6.png",
            "shotgun_view" to "img/wv_shotgun_view3.png",
            "two_barrels_view" to "img/wv_two_barrels_view3.png",

            "ui_plant_green_grass" to "img/ui_plant_green_grass2.png",
            
            // Effects
            "eff_snowflake" to "img/eff_snowflake.png",
            "eff_pistol_bullet" to "img/eff_pistol_bullet.png",
            "eff_sl_etr" to "img/eff_sl_etr.png"
        )

        private val assetDescriptors = mutableMapOf<String, AssetDescriptor<Texture>>()
        private val atlases = mutableMapOf<String, TextureAtlas>()

        operator fun get(assetName: String): AssetDescriptor<Texture> {
            if (assetName !in assetDescriptors) {
                throw RuntimeException("unknown asset name $assetName")
            }
            return assetDescriptors[assetName]!!
        }

        fun atlas(atlasName: String): TextureAtlas = atlases[atlasName]!!

        fun width(am: AssetManager, name: String): Int = am[assetDescriptors[name]].width

        fun height(am: AssetManager, name: String): Int = am[assetDescriptors[name]].height

        fun load(assetManager: AssetManager) {
            for ((assetName, assetPath) in assetsNames) {
                val assetDescriptor = AssetDescriptor<Texture>(assetPath, Texture::class.java)
                assetDescriptors[assetName] = assetDescriptor
                assetManager.load(assetDescriptor)
            }

            atlases["map_icons"] = TextureAtlas(Gdx.files.internal("img/map_icons.atlas"))
            atlases["hexes"] = TextureAtlas(Gdx.files.internal("img/hexes_new.atlas"))
            atlases["perks"] = TextureAtlas(Gdx.files.internal("img/perks.atlas"))
            atlases["quest_icons"] = TextureAtlas(Gdx.files.internal("img/quest_icons.atlas"))
            atlases["ui_game"] = TextureAtlas(Gdx.files.internal("img/ui_game_atlas.atlas"))
            atlases["ui_quest_check_mark"] = TextureAtlas(Gdx.files.internal("img/ui_quest_check_mark.atlas"))

            FontKurale = loadFont(assetManager, "fonts/kurale.ttf", 20)
            FontKurale12 = loadFont(assetManager, "fonts/kurale12.ttf", 12)
            FontTimes = loadFont(assetManager, "fonts/times.ttf", 10)
            FontTimes24 = loadFont(assetManager, "fonts/times24.ttf", 24)
            FontTimes18 = loadFont(assetManager, "fonts/times18.ttf", 18)
            FontTimes14 = loadFont(assetManager, "fonts/times14.ttf", 14)
            FontTimes12 = loadFont(assetManager, "fonts/times12.ttf", 12)

            assetManager.load("gdx/skins/uiskin.json", Skin::class.java)
            assetManager.finishLoading()

            // TODO filtering ?
//            for(assetName in assetsNames.keys) {
//                val tex = assetManager[this[assetName]]
//                tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear)
//            }

            val klaxon = Klaxon()
            // hexes
            JsonReader(StringReader(readFile("hexes.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val hex = klaxon.parse<SerializedHexagon>(reader)!!
                        Hexes[hex.map_id] = hex
                    }
                }
            }
            // persons
            JsonReader(StringReader(readFile("persons.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val person = klaxon.parse<SerializedPerson>(reader)!!
                        Persons[person.name] = person
                    }
                }
            }
            // items
            JsonReader(StringReader(readFile("items.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        reader.beginObject {
                            val actions: MutableList<JsonObject> = mutableListOf()
                            var drawable = ""
                            var type = ""
                            var name = ""
                            var hasView = false
                            var resource_category : MutableMap<String, SerializedItemResource> = mutableMapOf()

                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "name" -> {
                                        name = reader.nextString()
                                    }
                                    "drawable" -> {
                                        drawable = reader.nextString()
                                    }
                                    "type" -> {
                                        type = reader.nextString()
                                    }
                                    "actions" -> {
                                        reader.beginArray {
                                            while (reader.hasNext()) {
                                                actions.add(reader.nextObject())
                                            }
                                        }
                                    }
                                    "has_view" -> {
                                        hasView = reader.nextBoolean()
                                    }
                                    "resource_category" -> {
                                        reader.beginObject {
                                            while(reader.hasNext()) {
                                                val nm = reader.nextName()
                                                val obj = klaxon.parseFromJsonObject<SerializedItemResource>(reader.nextObject())!!
                                                resource_category[nm] = obj
                                            }
                                        }
                                    }
                                }
                            }
                            Items[name] = SerializedItem(name, drawable, type, actions, has_view = hasView, resource_category = resource_category)
                            val assetDescriptor = AssetDescriptor<Texture>("img/${drawable}", Texture::class.java)
                            assetDescriptors["img/${drawable}"] = assetDescriptor
                            for((_, v) in resource_category) {
                                val assetDescriptorResource = AssetDescriptor<Texture>("img/${v.drawable}", Texture::class.java)
                                assetDescriptors["img/${v.drawable}"] = assetDescriptorResource
                                assetManager.load(assetDescriptorResource)
                            }
                            assetManager.load(assetDescriptor)
                        }
                    }
                }
            }
            // scenery
            JsonReader(StringReader(readFile("scenery.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val scn = klaxon.parse<SerializedScenery>(reader)!!
                        Scenery[scn.name] = scn

                        val assetDescriptor = AssetDescriptor<Texture>("img/${scn.drawable}", Texture::class.java)
                        assetDescriptors["img/${scn.drawable}"] = assetDescriptor
                        assetManager.load(assetDescriptor)
                    }
                }
            }
            // animations
            JsonReader(StringReader(readFile("animations.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val anm = klaxon.parse<SerializedAnimation>(reader)!!
                        Animations[anm.name] = anm
                        if (anm.atlas !in atlases)
                            atlases[anm.atlas] = TextureAtlas(Gdx.files.internal("img/${anm.atlas}"))
                    }
                }
            }
            // particles
            JsonReader(StringReader(readFile("particles.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val prt = klaxon.parse<SerializedParticle>(reader)!!
                        Particles[prt.name] = prt
                        if (prt.name !in atlases)
                            atlases[prt.name] = TextureAtlas(Gdx.files.internal("img/${prt.name}"))
                    }
                }
            }
            // quests
            JsonReader(StringReader(readFile("quests.json"))).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val prt = klaxon.parse<SerializedQuest>(reader)!!
                        Quests[prt.questID] = prt
                    }
                }
            }

            assetManager.finishLoading()
        }

        fun loadFont(assetManager: AssetManager, fontAssetPath: String, fontSize: Int): BitmapFont {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
                    "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                    "1234567890-=+,?!.()<>{}*%^$#@\"':;/"

            val resolver: FileHandleResolver = InternalFileHandleResolver()
            assetManager.setLoader(FreeTypeFontGenerator::class.java, FreeTypeFontGeneratorLoader(resolver))
            assetManager.setLoader(BitmapFont::class.java, ".ttf", FreetypeFontLoader(resolver))

            val param = FreeTypeFontLoaderParameter()
            param.fontFileName = fontAssetPath
            param.fontParameters.size = fontSize
            param.fontParameters.characters = chars
            param.fontParameters.shadowColor = Color.BLACK
            param.fontParameters.shadowOffsetX = 1
            param.fontParameters.shadowOffsetY = 1

            assetManager.load(fontAssetPath, BitmapFont::class.java, param)
            assetManager.finishLoading()
            return assetManager.get(fontAssetPath, BitmapFont::class.java)
        }

        fun scaleTextureDescriptor(
            assetManager: AssetManager,
            texture: AssetDescriptor<Texture>,
            scale: Float
        ): Texture {
            if (!assetManager[texture].textureData.isPrepared) {
                assetManager[texture].textureData.prepare()
            }
            val tex = assetManager[texture].textureData.consumePixmap()

            val pixmap = Pixmap((tex.width * scale).toInt(), (tex.height * scale).toInt(), tex.format)
            pixmap.filter = Pixmap.Filter.NearestNeighbour

            pixmap.drawPixmap(
                tex,
                0, 0, tex.width, tex.height,
                0, 0, pixmap.width, pixmap.height
            )
            val newTex = Texture(pixmap)

            tex.dispose()
            pixmap.dispose()

            return newTex
        }

        fun scaleFitTextureRegion(texReg: TextureRegion, width: Int, height: Int): Texture {
            if (!texReg.texture.textureData.isPrepared) {
                texReg.texture.textureData.prepare()
            }
            val tex = texReg.texture.textureData.consumePixmap()

            val pixmap = Pixmap(width, height, tex.format)
            pixmap.filter = Pixmap.Filter.BiLinear

            pixmap.drawPixmap(
                tex,
                texReg.regionX, texReg.regionY, texReg.regionWidth, texReg.regionHeight,
                0, 0, pixmap.width, pixmap.height
            )
            val newTex = Texture(pixmap)

            tex.dispose()
            pixmap.dispose()

            return newTex
        }

        fun scaleFitTexture(texture: Texture, width: Int, height: Int): Texture {
            if (!texture.textureData.isPrepared) {
                texture.textureData.prepare()
            }
            val tex = texture.textureData.consumePixmap()

            val pixmap = Pixmap(width, height, tex.format)
            pixmap.filter = Pixmap.Filter.NearestNeighbour

            pixmap.drawPixmap(
                tex,
                0, 0, tex.width, tex.height,
                0, 0, pixmap.width, pixmap.height
            )
            val newTex = Texture(pixmap)

            tex.dispose()
            pixmap.dispose()

            return newTex
        }

        fun scaleTexture(texture: Texture, scale: Float): Texture {
            if (!texture.textureData.isPrepared) {
                texture.textureData.prepare()
            }
            val tex = texture.textureData.consumePixmap()

            val pixmap = Pixmap((tex.width * scale).toInt(), (tex.height * scale).toInt(), tex.format)
            pixmap.filter = Pixmap.Filter.NearestNeighbour

            pixmap.drawPixmap(
                tex,
                0, 0, tex.width, tex.height,
                0, 0, pixmap.width, pixmap.height
            )
            val newTex = Texture(pixmap)

            tex.dispose()
            pixmap.dispose()

            return newTex
        }

        fun centerCropTextureRegion(textureRegion: TextureRegion, cropWidth: Int, cropHeight: Int): TextureRegion {
            val halfW = (textureRegion.regionWidth - cropWidth) / 2
            val halfH = (textureRegion.regionHeight - cropHeight) / 2

            return TextureRegion(
                textureRegion, halfW, halfH,
                textureRegion.regionWidth - halfW*2, textureRegion.regionHeight - halfH*2
            )
        }

        fun readFile(fileName: String): String {
            val file = Gdx.files.internal(fileName)
            return file.readString()
        }

        fun loadShader(shaderName: String): ShaderProgram {
            val vertexShader = Gdx.files.internal("shaders/$shaderName/vertex.glsl").readString()
            val fragmentShader = Gdx.files.internal("shaders/$shaderName/fragment.glsl").readString()

            val shaderProgram = ShaderProgram(vertexShader, fragmentShader)
            if (!shaderProgram.isCompiled)
                throw GdxRuntimeException("couldn't compile shader '$shaderName': \n${shaderProgram.log}")

            return shaderProgram
        }

        fun dispose() {
            for ((_, atlas) in atlases)
                atlas.dispose()
        }
    }
}