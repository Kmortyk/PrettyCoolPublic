package com.kmortyk.game.ui.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.GdxRuntimeException
import com.kmortyk.game.*
import com.kmortyk.game.animation.AnimationManager
import com.kmortyk.game.map.editor.AssetType
import com.kmortyk.game.map.editor.EditorState
import com.kmortyk.game.ui.*
import com.kmortyk.game.ui.editor.EditorPaneElement
import com.kmortyk.game.ui.editor.EditorPaneElementTexture
import com.kmortyk.game.ui.element.*
import com.kmortyk.game.ui.group.*
import java.io.File
import java.util.*

data class EditorUIState(val state: EditorUIStateType = EditorUIStateType.Main, val elements: MutableList<InterfaceElement>)

enum class EditorUIStateType {
    Main,
    CreateMap,
    SaveMap,
    SaveMapDialog,
    CreateProjectDialog,
    CreateMapDialog,
    DirsDialog,
}

class EditorUI(private val screenWidth: Float,
               private val screenHeight: Float,
               private val gCam: GameCamera,
               private val game: PrettyCoolGame,
               private val gameUiSpriteBatch: SpriteBatch,
               private val editorState: EditorState,
               private val assetManager: AssetManager) : ElementsGroup() {
    companion object {
        const val MaxMapWidth = 99
        const val MaxMapHeight = 99
    }

    // all view's history for returning back
    private val viewsStack: Stack<EditorUIState> = Stack()
    // current ui state type
    private var state: EditorUIStateType = EditorUIStateType.Main

    private val outlineShader = Assets.loadShader("outline")

    fun backward() {
        if(viewsStack.empty())
            return // nothing to back

        val prevUIState = viewsStack.pop()

        state = prevUIState.state
        elements = prevUIState.elements
    }

    private fun forward(newState: EditorUIStateType, savePrevElements: Boolean = false) {
        if(state == newState)
            return // already in this state

        // save current state
        val editorState = EditorUIState(state, elements)
        viewsStack.push(editorState)

        state = newState
        elements = mutableListOf()
        if(savePrevElements)
            elements.addAll(editorState.elements)
    }

    init {
        elements = editorUI()
    }

    fun draw(assetManager: AssetManager) {
        gameUiSpriteBatch.begin()
        super.onDraw(assetManager, gameUiSpriteBatch)
        gameUiSpriteBatch.end()
    }

    // -- UIs ----------------------------------------------------------------------------------------------------------
    private fun editorUI() : MutableList<InterfaceElement>  {
        val elements: MutableList<InterfaceElement> = mutableListOf()

        // assets pane
        val rightPane = Backing(screenWidth - Assets.HexWidth - GameUI.DefaultPadding*2, 0.0f, screenWidth, screenHeight+1,
                mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"), lineWidth = 4.0f)
        elements.add(rightPane)

        var assetID: Int
        var assetName: String
        var prevAssetType: AssetType
        var assetType: AssetType = AssetType.Hexagon

        var prevEl: EditorPaneElement? = null

        // hexagons list
        val list = VerticalListElement(
                     gCam,
            rightPane.bounds.left(),
                screenHeight+1,
            Assets.HexWidth + GameUI.DefaultPadding*2,
                screenHeight+1, object : OnElementSelected {
            override fun onElementSelected(any : Int, element: InterfaceElement) {
                val el = element as EditorPaneElement
                assetID = el.assetID
                assetName = el.assetName

                editorState.setLastAsset(assetID, assetName)

                if(assetType != AssetType.Hexagon) {
                    prevEl?.setSelected(false)
                    el.setSelected(true)
                    prevEl = el
                } else {
                    el.setSelected(false)
                }
            }
        })
        elements.add(list)

        val checks = mutableMapOf<AssetType, CheckElement>()

        // select asset type buttons
        val astTypTex = assetManager[Assets["ui_editor_pane_hexes"]]
        val astTypPad = GameUI.DefaultPadding*0.5f
        val hxCheck = CheckElement(screenWidth - screenWidth*0.1f - (astTypTex.width + astTypPad)*4 - astTypPad,
                screenHeight - astTypTex.height - astTypPad,
                assetManager[Assets["ui_editor_pane_hexes"]], assetManager[Assets["ui_editor_pane_hexes_filled"]], object : OnCheckElement {
            override fun onCheck(toggled: Boolean) {
                prevAssetType = assetType
                assetType = AssetType.Hexagon

                switchPaneType(list, assetType, prevAssetType, checks)
            }
        })
        val scCheck = CheckElement(screenWidth - screenWidth*0.1f - (astTypTex.width + astTypPad)*3 - astTypPad,
                screenHeight - astTypTex.height - astTypPad,
                assetManager[Assets["ui_editor_pane_scenery"]], assetManager[Assets["ui_editor_pane_scenery_filled"]], object : OnCheckElement {
            override fun onCheck(toggled: Boolean) {
                prevAssetType = assetType
                assetType = AssetType.Scenery

                switchPaneType(list, assetType, prevAssetType, checks)
            }
        })
        val itCheck = CheckElement(screenWidth - screenWidth*0.1f - (astTypTex.width + astTypPad)*2 - astTypPad,
                screenHeight - astTypTex.height - astTypPad,
                assetManager[Assets["ui_editor_pane_items"]], assetManager[Assets["ui_editor_pane_items_filled"]], object : OnCheckElement {
            override fun onCheck(toggled: Boolean) {
                prevAssetType = assetType
                assetType = AssetType.Item

                switchPaneType(list, assetType, prevAssetType, checks)
            }
        })
        val prCheck = CheckElement(screenWidth - screenWidth*0.1f - (astTypTex.width + astTypPad)*1 - astTypPad,
                screenHeight - astTypTex.height - astTypPad,
                assetManager[Assets["ui_editor_pane_persons"]], assetManager[Assets["ui_editor_pane_persons_filled"]], object : OnCheckElement {
            override fun onCheck(toggled: Boolean) {
                prevAssetType = assetType
                assetType = AssetType.Person

                switchPaneType(list, assetType, prevAssetType, checks)
            }
        })

        checks[AssetType.Hexagon] = hxCheck
        checks[AssetType.Scenery] = scCheck
        checks[AssetType.Item] = itCheck
        checks[AssetType.Person] = prCheck

        for((_, ch) in checks)
            elements.add(ch)

        selectAssetType(list, assetType, checks)

        // brush button
        val brushTex = assetManager[Assets["ui_editor_brush"]]
        elements.add(TextureButton(screenWidth*0.9f - GameUI.DefaultPadding*1.5f - brushTex.width, GameUI.DefaultPadding, brushTex, Runnable {
            editorState.brush()
        }))

        // eraser
        val eraserTex = assetManager[Assets["ui_editor_eraser"]]
        elements.add(TextureButton(screenWidth*0.9f - GameUI.DefaultPadding*2.5f - brushTex.width - eraserTex.width,
                GameUI.DefaultPadding, eraserTex, Runnable {
            editorState.erase()
        }))

        // instant draw check circle
        elements.add(CheckElement(
                GameUI.DefaultPadding*1.5f, GameUI.DefaultPadding*1.5f,
                assetManager[Assets["ui_editor_touch_to_paint_button_empty"]],
                assetManager[Assets["ui_editor_touch_to_paint_button_filled"]],
                object : OnCheckElement {
                    override fun onCheck(toggled: Boolean) {
                        editorState.setInstantDraw(toggled)
                    }
                }, toggled = false
        ))

        // create map
        val createTex = assetManager[Assets["ui_editor_create_map"]]
        elements.add(TextureButton(GameUI.DefaultPadding,
            screenHeight - createTex.height - GameUI.DefaultPadding, createTex, Runnable {
                openNewMapWindow()
            }))

        // save map
        val saveTex = assetManager[Assets["ui_editor_save_map"]]
        elements.add(TextureButton(GameUI.DefaultPadding,
                screenHeight - createTex.height - saveTex.height - GameUI.DefaultPadding*2, saveTex, Runnable {
            openSaveMapWindow()
        }))

        return elements
    }

    private fun openNewMapWindow() {
        var mapWidth = 23
        var mapHeight = 23

        forward(EditorUIStateType.CreateMap)

        val backing = Backing(screenWidth*0.1f, screenHeight*0.1f, screenWidth*0.8f, screenHeight*0.8f,
        mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"))
        addElements(backing)

        val textWidth = TextElement(Assets.FontKurale, "Ширина:", 1000.0f, backing.bounds.x,
            backing.bounds.top(), Color.WHITE, null, padding = GameUI.DefaultPadding)
        addElement("textWidth", textWidth)

        // map width
        val cellSize = screenWidth*0.07f
        val mapWidthElement = Backing(backing.bounds.left()+GameUI.DefaultPadding, textWidth.bounds.bottom()-cellSize-GameUI.DefaultPadding, cellSize, cellSize,
                mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"), lineWidth = 3.0f)
        addElements(mapWidthElement)
        val mapWidthText = TextElement(Assets.FontTimes24, "$mapWidth", screenWidth*0.1f,
            mapWidthElement.bounds.left(), mapWidthElement.bounds.centerY() + TextElement.lineHeight(Assets.FontTimes24), Color.WHITE, null)
        addElements(mapWidthText)
        val incTex = assetManager[Assets["ui_editor_create_map_increase_size"]]
        val incMapWidth = TextureButton(mapWidthElement.bounds.right()+GameUI.DefaultPadding, mapWidthElement.bounds.top()-incTex.height,
            incTex, Runnable {
            if(mapWidth >= MaxMapWidth) return@Runnable
            mapWidth++
            mapWidthText.updateTextSavePosition("$mapWidth")
        })
        addElements(incMapWidth)
        val decMapWidth = TextureButton(mapWidthElement.bounds.right()+GameUI.DefaultPadding, mapWidthElement.bounds.bottom(),
                assetManager[Assets["ui_editor_create_map_decrease_size"]], Runnable {
            if(mapWidth <= 1) return@Runnable
            mapWidth--
            mapWidthText.updateTextSavePosition("$mapWidth")
        })
        addElements(decMapWidth)

        val textHeight = TextElement(Assets.FontKurale, "Высота:", 1000.0f, backing.bounds.x+GameUI.DefaultPadding,
            mapWidthElement.bounds.bottom()-GameUI.DefaultPadding, Color.WHITE, null, padding = 0.0f)
        addElement("textWidth", textHeight)

        // map height
        val mapHeightElement = Backing(backing.bounds.left()+GameUI.DefaultPadding,
            textHeight.bounds.bottom()-cellSize-GameUI.DefaultPadding, cellSize, cellSize,
                mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"), lineWidth = 3.0f)
        addElements(mapHeightElement)
        val mapHeightText = TextElement(Assets.FontTimes24, "$mapWidth", screenWidth*0.1f,
            mapHeightElement.bounds.left(),
            mapHeightElement.bounds.centerY() +
                        TextElement.lineHeight(Assets.FontTimes24), Color.WHITE, null)
        addElements(mapHeightText)
        val incMapHeight = TextureButton(mapHeightElement.bounds.right()+GameUI.DefaultPadding, mapHeightElement.bounds.top()-incTex.height,
                assetManager[Assets["ui_editor_create_map_increase_size"]], Runnable {
            if(mapHeight >= MaxMapHeight) return@Runnable
            mapHeight++
            mapHeightText.updateTextSavePosition("$mapHeight")
        })
        addElements(incMapHeight)
        val decMapHeight = TextureButton(mapHeightElement.bounds.right()+GameUI.DefaultPadding, mapHeightElement.bounds.bottom(),
                assetManager[Assets["ui_editor_create_map_decrease_size"]], Runnable {
            if(mapHeight <= 1) return@Runnable
            mapHeight--
            mapHeightText.updateTextSavePosition("$mapHeight")
        })
        addElements(decMapHeight)

        var assetID = 100

        // scheme
        val textScheme = TextElement(Assets.FontKurale, "Схема:", 1000.0f, textWidth.bounds.right()+GameUI.DefaultPadding*2,
            backing.bounds.top()-GameUI.DefaultPadding, Color.WHITE, null, padding=0.0f)
        addElement("textScheme", textScheme)
        // hexagons list
        val list = HorizontalListElement(
                 gCam,
            textWidth.bounds.right()+GameUI.DefaultPadding*2,
            textScheme.bounds.bottom()-GameUI.DefaultPadding,
            screenWidth*0.5f,
            Assets.HexHeight+GameUI.DefaultPadding, object : OnElementSelected {
                override fun onElementSelected(idx: Int, element: InterfaceElement) {
                    val el = element as EditorPaneElement
                    assetID = el.assetID
                }
            })
        for((id, hex) in Assets.Hexes) {
            if(id == Assets.NullHex) continue
            val hexTex = Assets.atlas("hexes").findRegion(hex.drawable)
            val el = EditorPaneElement(id, "", hexTex, 0.0f, 0.0f)
            list.addListElement(el)
        }
        list.setSelectionTexture(Assets.atlas("hexes").findRegion("hex_editor_select_hexagon"))
        addElements(list)
        val b = Backing(list.bounds, true, Color.valueOf("#ab8353"))
        b.isTouchable = false
        addElements(b)

        // create button
        val tex = assetManager[Assets["ui_editor_create_map_create"]]
        val createButton = TextureButton(backing.bounds.right()-tex.width-GameUI.DefaultPadding*2,
            list.bounds.bottom()+(-tex.height+list.bounds.height)*0.5f+GameUI.DefaultPadding*0.5f,
            tex, Runnable {
                editorState.createMap(assetID, mapWidth, mapHeight)
                //backward() // TODO i don't like it ?
            })
        addElements(createButton)

        // close button
        val closeText = "Закрыть"
        addElements(TextElement(Assets.FontKurale, closeText, screenWidth,
                backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeText),
                backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
                Runnable { backward() }, positivePadding = false))

        // return to main menu
        val exitText = "Вернуться в главное меню"
        addElements(TextElement(Assets.FontKurale, exitText, screenWidth,
            backing.bounds.left() + GameUI.DefaultPadding,
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale) + 12.0f, Color.WHITE,
            Runnable {
                game.openMenu()
            }, positivePadding = false, padding = 0.0f))
    }

    private fun openSaveMapWindow() {
        forward(EditorUIStateType.SaveMap)

        // 3 types:
        // Main story maps - only for developers                    [CANNOT BE MODIFIED FROM EDITOR]
        // Other users maps - downloaded custom maps of other users [CANNOT BE MODIFIED FROM EDITOR]
        // This user maps - local maps used to be published         [CAN BE MODIFIED]

        val backing = Backing(screenWidth*0.1f, screenHeight*0.1f, screenWidth*0.8f, screenHeight*0.8f,
                mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"))
        addElements(backing)

        val mapNameText = TextElement(Assets.FontKurale12, "Название:", 1000.0f, backing.bounds.x,
            backing.bounds.top(), Color.WHITE, null, padding = GameUI.DefaultPadding)
        addElement("mapNameText", mapNameText)

        val etWidth = 120.0f
        val etHeight = 20.0f
        val tf = TextFieldElement(assetManager, backing.bounds.x+GameUI.DefaultPadding, mapNameText.bounds.bottom()-etHeight-GameUI.DefaultPadding, etWidth, etHeight)
        addElements(tf)

        var mapNameTextField = "awesomeMap"
        var changedByUser: Boolean = false

        tf.onTextChanged = object : OnTextChanged {
            override fun onTextChanged(oldText: String, newText: String) {
                mapNameText.text = "Название (${newText.length}/${tf.maxChars}):"
                mapNameTextField = newText
                changedByUser = true
            }
        }

        var category = 0
        var projectName = ""
        var mapName = ""

        // category
        val categoryList = VerticalListElement(gCam, tf.bounds.right() + GameUI.DefaultPadding*1f, backing.bounds.top()-GameUI.DefaultPadding,
            TextElement.wordWidth(Assets.FontTimes18, "User's maps"),
                screenHeight*0.8f-etHeight-TextElement.lineHeight(Assets.FontKurale), object : OnElementSelected {
            override fun onElementSelected(idx: Int, element: InterfaceElement) {
                category = idx

                val proj = findElementByName("projectList") as VerticalListElement
                proj.clear()
                val projects = getProjectsInCategory(category)
                proj.addListElement(TextElement(Assets.FontTimes18, "+", 1000.0f, 0.0f, 0.0f, Color.BLACK, null))
                for(p in projects) {
                    proj.addListElement(TextElement(Assets.FontTimes18, p, 1000.0f, 0.0f, 0.0f, Color.BLACK, null))
                }

                if(projects.isEmpty()) {
                    (findElementByName("mapsList") as VerticalListElement).clear()
                } else {
                    proj.select(1)
                }
            }
        }, extendTex = 0, selectionPad = GameUI.DefaultPadding)

        categoryList.centering = false
        categoryList.addListElement(TextElement(Assets.FontTimes18, "Story maps", 1000.0f, 0.0f, 0.0f, Color.BLACK, null, padding = 0.0f))
        categoryList.addListElement(TextElement(Assets.FontTimes18, "User's maps", 1000.0f, 0.0f, 0.0f, Color.BLACK, null, padding = 0.0f))
        categoryList.addListElement(TextElement(Assets.FontTimes18, "My maps", 1000.0f, 0.0f, 0.0f, Color.BLACK, null, padding = 0.0f))
        categoryList.setSelectionElement(Backing(0.0f, 0.0f, 0.0f, 0.0f, fill = false, borderColor = Color.valueOf("#927f68")))
        addElements(Backing(categoryList.bounds, true, Color.valueOf("#ab8353")))
        addElements(categoryList)

        // project
        val projectList = VerticalListElement(gCam, categoryList.bounds.right() + GameUI.DefaultPadding*1,
            backing.bounds.top()-GameUI.DefaultPadding, TextElement.wordWidth(Assets.FontTimes18,
                "Example_project_1"),
                screenHeight*0.8f-etHeight-TextElement.lineHeight(Assets.FontKurale), object : OnElementSelected {
            override fun onElementSelected(idx: Int, element: InterfaceElement) {
                if(idx == 0) {
                    openAddProjectDialog(categoryList, category)
                } else {
                    val project = (element as TextElement).text
                    projectName = project

                    val mapsList = findElementByName("mapsList") as VerticalListElement
                    mapsList.clear()

                    mapsList.addListElement(TextElement(Assets.FontTimes18, "+", 1000.0f, 0.0f, 0.0f, Color.BLACK, null))

                    val maps = getMapsInCategoryAndProject(category, project)
                    for(m in maps) {
                        mapsList.addListElement(TextElement(Assets.FontTimes18, m, 1000.0f, 0.0f, 0.0f, Color.BLACK, null))
                    }
                    mapsList.select(0)
                }
            }
        }, extendTex = 0, selectionPad = GameUI.DefaultPadding)
        projectList.centering = false
        projectList.setSelectionElement(Backing(0.0f, 0.0f, 0.0f, 0.0f, fill = false, borderColor = Color.valueOf("#927f68")))
        addElements(Backing(projectList.bounds, true, Color.valueOf("#ab8353")))
        addElement("projectList", projectList)

        // maps
        val mapsList = VerticalListElement(gCam, projectList.bounds.right() + GameUI.DefaultPadding*1, backing.bounds.top()-GameUI.DefaultPadding,
            TextElement.wordWidth(Assets.FontTimes18,
                "Example_map_name1"),
                screenHeight*0.8f-etHeight-TextElement.lineHeight(Assets.FontKurale), object : OnElementSelected {
            override fun onElementSelected(idx: Int, element: InterfaceElement) {
                if(idx == 0) {
                    mapName = mapNameTextField
                    tf.setText(mapName)
                    changedByUser = true
                } else {
                    mapName = (element as TextElement).text
                    tf.setText(mapName)
                    changedByUser = false
                }
            }
        }, extendTex = 0, selectionPad = GameUI.DefaultPadding)
        mapsList.centering = false
        mapsList.setSelectionElement(Backing(0.0f, 0.0f, 0.0f, 0.0f, fill = false, borderColor = Color.valueOf("#927f68")))
        addElements(Backing(mapsList.bounds, true, borderColor = Color.valueOf("#98938b"), mainColor = Color.valueOf("#c0a787"), fill=true))
        addElement("mapsList", mapsList)

        val saveTex = assetManager[Assets["ui_editor_save_map_to_disk"]]
        val save = TextureButton(tf.bounds.left(), tf.bounds.bottom()-saveTex.height-GameUI.DefaultPadding, saveTex, Runnable {
            openSaveDialog(category, projectName, tf.getText(), !changedByUser)
        })
        addElement("saveButton", save)
        val load = TextureButton(tf.bounds.left(), save.bounds.bottom()-save.bounds.height-GameUI.DefaultPadding, assetManager[Assets["ui_editor_save_map_from_disk"]],
            Runnable {
                val mapPath = getMapPath(category, projectName, mapName)
                editorState.loadMap(mapPath)
            })
        addElement("loadButton", load)
        val publish = TextureButton(tf.bounds.left(), load.bounds.bottom()-load.bounds.height-GameUI.DefaultPadding, assetManager[Assets["ui_editor_save_map_to_server"]], null)
        addElement("publishButton", publish)

        val dirsBurt = TextureButton(
            tf.bounds.left(),
            backing.bounds.bottom() + GameUI.DefaultPadding*2 + TextElement.lineHeight(Assets.FontKurale),
            assetManager[Assets["ui_editor_save_map_choose_dirs"]],
            Runnable {
                openDirsDialog(category, mapName, projectName)
            })
        addElement("dirsButton", dirsBurt)

        // close button
        val closeText = "Закрыть"
        addElements(TextElement(Assets.FontKurale, closeText, screenWidth,
                backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeText),
                backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
                Runnable { backward() }, positivePadding = false))

        /// MAKE SOME STUFF
        categoryList.select(0)
    }

    private fun openSaveDialog(category: Int, projectName: String, mapName: String, overwrite: Boolean) {
        forward(EditorUIStateType.SaveMapDialog, true)

        val backing = Backing(screenWidth*0.2f, screenHeight*0.3f, screenWidth*0.6f, screenHeight*0.4f,
            mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"))
        addElements(backing)

        val saveText = if(overwrite) "Вы хотите перезаписать $mapName.json?" else
                                     "Вы хотите сохранить в $mapName.json?"

        val mapNameText = TextElement(Assets.FontTimes18,
            saveText, 1000.0f, backing.bounds.left(),
            backing.bounds.top(), Color.BLACK, null, padding = GameUI.DefaultPadding)
        addElement("mapNameText", mapNameText)

        // close button
        val closeButText = "Закрыть"
        val closeBut = TextElement(Assets.FontKurale, closeButText, screenWidth,
            backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeButText),
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable { backward() }, positivePadding = false)

        // save button
        val saveButText = if(overwrite) "Перезаписать" else "Сохранить"
        val saveBut = TextElement(Assets.FontKurale, saveButText, screenWidth,
            closeBut.bounds.left() - TextElement.wordWidth(Assets.FontKurale, saveButText) - GameUI.DefaultPadding,
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable {
                editorState.saveMap(category, projectName, mapName)
                backward()
        }, positivePadding = false)

        addElements(closeBut, saveBut)
    }

    private fun openAddProjectDialog(categoryList: VerticalListElement, category: Int) {
        forward(EditorUIStateType.CreateProjectDialog, true)

        val backing = Backing(screenWidth*0.2f, screenHeight*0.3f, screenWidth*0.6f, screenHeight*0.4f,
            mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"))
        backing.isTouchable = true
        addElements(backing)

        val createProjectText = "Название проекта:"
        val createProjectBut = TextElement(Assets.FontKurale, createProjectText, screenWidth,
            backing.bounds.left() + GameUI.DefaultPadding,
            backing.bounds.top() - GameUI.DefaultPadding, Color.WHITE, null, padding = 0.0f)
        addElements(createProjectBut)

        val tf = TextFieldElement(
               assetManager,
            backing.bounds.x + GameUI.DefaultPadding,
            createProjectBut.bounds.bottom() - 20.0f - GameUI.DefaultPadding,
            backing.bounds.width - GameUI.DefaultPadding*2,
        20.0f, initText="ProjectName")
        addElements(tf)

        // close button
        val closeButText = "Закрыть"
        val closeBut = TextElement(Assets.FontKurale, closeButText, screenWidth,
            backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeButText),
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable { backward() }, positivePadding = false)

        // save button
        val saveButText = "Создать проект"
        val saveBut = TextElement(Assets.FontKurale, saveButText, screenWidth,
            closeBut.bounds.left() - TextElement.wordWidth(Assets.FontKurale, saveButText) - GameUI.DefaultPadding,
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable {
                createProject(category, tf.getText())
                backward()
                categoryList.select(category)
            }, positivePadding = false)

        addElements(closeBut, saveBut)
    }

    private fun getMapPath(category: Int, projectName: String, mapName: String): String {
        return when(category) {
            0 -> {
                return "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/maps/$projectName/$mapName.json"
            }
            1 -> {
                "" // TODO no local maps yet
            }
            2 -> {
                "" // TODO no user's maps yet
            }
            else -> "" // unknown category
        }
    }

    private fun getMapsInCategoryAndProject(category: Int, project: String): List<String> {
        return when(category) {
            0 -> {
                val mapsAssetsDir = "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/maps/$project"
                val result = mutableListOf<String>()

                File(mapsAssetsDir)
                    .listFiles()
                    ?.forEach {
                        if(it.isFile) {
                            val mapName = it.name.replace(".json", "")
                            result.add(mapName)
                        }
                    }

                result
            }
            1 -> {
                listOf() // TODO no local maps yet
            }
            2 -> {
                listOf() // TODO no user's maps yet
            }
            else -> listOf() // unknown category
        }
    }

    private fun getProjectsInCategory(category: Int): List<String> {
        return when(category) {
            0 -> {
                val mapsAssetsDir = "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/maps"
                val result = mutableListOf<String>()

                File(mapsAssetsDir)
                    .listFiles()
                    ?.forEach {
                        if(it.isDirectory)
                            result.add(it.name)
                    }

                result
            }
            1 -> {
                listOf() // TODO no local maps yet
            }
            2 -> {
                listOf() // TODO no user's maps yet
            }
            else -> listOf() // unknown category
        }
    }

    private fun selectAssetType(list: VerticalListElement, newType: AssetType, checks: Map<AssetType, CheckElement>) {
        list.clear()

        for((_, ch) in checks) {
            ch.toggled = false
        }

        when(newType) {
            AssetType.Hexagon -> {
                for((id, hex) in Assets.Hexes) {
                    if(id == Assets.NullHex) continue
                    val tex = Assets.atlas("hexes").findRegion(hex.drawable)
                    val el = EditorPaneElement(id, "", tex, 0.0f, 0.0f)
                    list.addListElement(el)
                }

                list.setSelectionElement(TextureRegionBacking(Assets.atlas("hexes").findRegion("hex_editor_select_hexagon"), 0.0f, 0.0f))
                checks[AssetType.Hexagon]?.toggled = true
            }
            AssetType.Scenery -> {
                for((name, scn) in Assets.Scenery) {
                    val tex = assetManager[Assets["img/${scn.drawable}"]]
                    val el = EditorPaneElementTexture(0, name, tex, 0.0f, 0.0f)
                    el.setShaderProgram(outlineShader)
                    list.addListElement(el)
                }

                list.setSelectionElement(null)
                checks[AssetType.Scenery]?.toggled = true
            }
            AssetType.Person -> {
                for((_, prs) in Assets.Persons) {
                    val frame = AnimationManager.getDefaultFrame(prs.drawable)
                    val el = EditorPaneElement(0, prs.name, frame, 0.0f, 0.0f)
                    el.setShaderProgram(outlineShader)
                    list.addListElement(el)
                }

                list.setSelectionElement(null)
                checks[AssetType.Person]?.toggled = true
            }
            AssetType.Item -> {
                for((_, itm) in Assets.Items) {
                    val scaled = Assets.scaleTextureDescriptor(assetManager, Assets["img/${itm.drawable}"], 2.0f)
                    val el = EditorPaneElementTexture(0, itm.name, scaled, 0.0f, 0.0f)
                    el.setShaderProgram(outlineShader)
                    list.addListElement(el)
                }

                list.setSelectionElement(null)
                checks[AssetType.Item]?.toggled = true
            }
        }

        list.select(0)
    }

    private fun switchPaneType(list: VerticalListElement, newType: AssetType, thisType: AssetType, checks: Map<AssetType, CheckElement>) : AssetType {
        if(newType == thisType)
            return thisType

        selectAssetType(list, newType, checks)
        editorState.setAssetType(newType)

        return newType
    }

    private fun createProject(category: Int, text: String) {
        when(category) {
            0 -> {
                if(PrettyCoolGame.Debug && Gdx.app.type == Application.ApplicationType.Desktop) {
                    val path = "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/maps/$text"

                    if(!Gdx.files.isExternalStorageAvailable) {
                        throw GdxRuntimeException("can't write to external storage: not available")
                    }

                    val file = File(path)
                    file.mkdir()

                    log.info("create project ${LogColors.GREEN}\"$path\"${LogColors.RESET}")
                }
            }
            1 -> {
                // TODO no local maps yet
            }
            2 -> {
                // TODO no user's maps yet
            }
            else -> {
                log.error("can't create project in assets: cannot be possible")
            }
        }
    }

    private fun openDirsDialog(category: Int, mapName: String, projectName: String) {
        forward(EditorUIStateType.DirsDialog, true)

        val backing = Backing(screenWidth*0.2f, screenHeight*0.3f, screenWidth*0.6f, screenHeight*0.4f,
            mainColor = Color.valueOf("#c0a077"), borderColor = Color.valueOf("#ab8353"))
        backing.isTouchable = true
        addElements(backing)

        val createProjectText = "Выберите направления в проекте $projectName для карты $mapName:"
        val createProjectEl = TextElement(Assets.FontKurale, createProjectText, screenWidth*0.6f-GameUI.DefaultPadding*2,
            backing.bounds.left() + GameUI.DefaultPadding,
            backing.bounds.top() - GameUI.DefaultPadding, Color.WHITE, null, padding = 0.0f)
        addElements(createProjectEl)

        val mapsNames = getMapsInCategoryAndProject(category, projectName)

        val ddlw = 120.0f
        val ddlh = 20.0f
        val none = "[none]"
        val curDirs = editorState.getMapDirs()

        val upDropDown = DropDownList(
            gCam, backing.bounds.centerX() - 60.0f + GameUI.DefaultPadding, createProjectEl.bounds.bottom() - ddlh - GameUI.DefaultPadding,
            ddlw, ddlh)
        upDropDown.addItem(none)
        upDropDown.addAllItems(*mapsNames.toTypedArray())
        upDropDown.select(curDirs.up)

        val rightDropDown = DropDownList(
            gCam, backing.bounds.right() - 120.0f - GameUI.DefaultPadding, upDropDown.bounds.bottom() - 20.0f - GameUI.DefaultPadding,
            ddlw, ddlh)
        rightDropDown.addItem(none)
        rightDropDown.addAllItems(*mapsNames.toTypedArray())
        rightDropDown.select(curDirs.right)

        val downDropDown = DropDownList(
            gCam, backing.bounds.centerX() - 60.0f + GameUI.DefaultPadding, rightDropDown.bounds.bottom() - 20.0f - GameUI.DefaultPadding,
            ddlw, ddlh)
        downDropDown.addItem(none)
        downDropDown.addAllItems(*mapsNames.toTypedArray())
        downDropDown.select(curDirs.down)

        val leftDropDown = DropDownList(
            gCam, upDropDown.bounds.left() - 120.0f - GameUI.DefaultPadding, upDropDown.bounds.bottom() - 20.0f - GameUI.DefaultPadding,
            ddlw, ddlh)
        leftDropDown.addItem(none)
        leftDropDown.addAllItems(*mapsNames.toTypedArray())
        leftDropDown.select(curDirs.left)

        // close button
        val closeButText = "Закрыть"
        val closeBut = TextElement(Assets.FontKurale, closeButText, screenWidth,
            backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeButText),
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable { backward() }, positivePadding = false)

        // save button
        val saveButText = "Сохранить"
        val saveBut = TextElement(Assets.FontKurale, saveButText, screenWidth,
            closeBut.bounds.left() - TextElement.wordWidth(Assets.FontKurale, saveButText) - GameUI.DefaultPadding,
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable {
                val up = if(upDropDown.getSelectedItem() == none) "" else upDropDown.getSelectedItem()
                val rh = if(rightDropDown.getSelectedItem() == none) "" else rightDropDown.getSelectedItem()
                val dw = if(downDropDown.getSelectedItem() == none) "" else downDropDown.getSelectedItem()
                val lf = if(leftDropDown.getSelectedItem() == none) "" else leftDropDown.getSelectedItem()

                editorState.setMapDirs(up=up, right = rh, down = dw, left = lf)
                backward()
            }, positivePadding = false)

        addElements(closeBut, saveBut)
        addElements(downDropDown)
        addElements(upDropDown)
        addElements(rightDropDown)
        addElements(leftDropDown)
    }

    // -- State --------------------------------------------------------------------------------------------------------\
    fun saveState() {
        // TODO
    }

    fun restoreState(state: EditorUIStateType) {
        when (state) {
            EditorUIStateType.DirsDialog -> {
            }
            EditorUIStateType.CreateProjectDialog -> {

            }
            EditorUIStateType.SaveMapDialog -> {

            }
            EditorUIStateType.CreateMap -> {

            }
            EditorUIStateType.CreateMapDialog -> {

            }
            EditorUIStateType.Main -> {
                // nothing to restore
            }
            EditorUIStateType.SaveMap -> {

            }
        }
    }
}