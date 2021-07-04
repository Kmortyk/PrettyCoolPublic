package com.kmortyk.game.ui.game

import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.InterfaceElement
import com.kmortyk.game.ui.element.TextureDescBacking
import com.kmortyk.game.ui.group.ElementsGroup

class QuickItemCell(x: Float, y: Float, row: Int, col: Int) : ItemCell(x, y, row, col) {
    init {
        (elements[0] as TextureDescBacking).texture = Assets["ui_inventory_cell_quick_slot"]
    }
}

open class ItemCell(x: Float, y: Float, public val row: Int, public val col: Int) : ElementsGroup() { // TODO delete
    val selection: InterfaceElement = TextureDescBacking(Assets["ui_inventory_cell_selected"], x, y)
    val cell: TextureDescBacking = TextureDescBacking(Assets["ui_inventory_cell"], x, y)

    init {
        cell.scale(0.95f)

        addElement("cell", cell)
        addElement("selection", selection)

        selection.isTouchable = false
        selection.isVisible = false
    }

    override fun contains(x: Float, y: Float): Boolean {
        return cell.contains(x, y)
    }
}

class InventoryCells(
        private val gameState: GameState,
        private val rows: Int,
        private val cols: Int,
        private val offsetX: Float,
        private val offsetY: Float) : ElementsGroup() {

    companion object {
        const val invCellSize = 54.0f
        const val startDragDst = 5.0f
    }

    private var lastSelectedCell: ItemCell? = null
    private var startPoint: Vector2 = Vector2()

    private var touchDown: Boolean = false
    private var draggedItem : InterfaceElement? = null
    private var draggedIdx: Int = -1

    private val itemViews: Array<ItemView?> = Array(7 * 7) { null }

    init {
        for(inventoryCols in 0 until cols) {
            addElement("inventoryQuickCell$inventoryCols",
                    QuickItemCell(offsetX + inventoryCols * invCellSize, offsetY, 0, inventoryCols)
            )
        }
        for(inventoryRows in 1 until rows) {
            for(inventoryCols in 0 until cols) {
                addElement("inventorySlot${inventoryRows*rows + inventoryCols}",
                        ItemCell(offsetX + inventoryCols * invCellSize,offsetY - inventoryRows * invCellSize, inventoryRows, inventoryCols)
                )
            }
        }

        val playerItems = gameState.player.items
        for(itemIdx in 0 until playerItems.size()) {
            val itm = playerItems[itemIdx]

            if(itm != null) {
                itemViews[itemIdx] = ItemView(gameState, itemIdx, itm, offsetX, offsetY)
                addElements(itemViews[itemIdx]!!)
            }
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        startPoint.set(x, y)
        touchDown = true

        var someTouched = false

        for(row in 0 until rows) {
            for(col in 0 until cols) {
                val itemCell: ItemCell = elements[row * rows + col] as ItemCell
                val touched = itemCell.cell.bounds.contains(x, y)
                if(touched) {
                    lastSelectedCell = itemCell
                    someTouched = true
                }
                itemCell.selection.isVisible = touched
            }
        }

        if(!someTouched && lastSelectedCell != null)
            lastSelectedCell!!.selection.isVisible = true

        return super.onTouch(x, y)
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        if(draggedItem != null) {
            for(idx in 0 until 7*7) {
                val el = elements[idx]
                if(el.contains(x, y)) {
                    val row = idx / 7
                    val col = idx % 7

                    swapDragged(row, col)
                    select(row, col)
                    break
                }
            }
        }

        touchDown = false
        draggedIdx = -1
        draggedItem = null

        return false
    }

    private fun select(row: Int, col: Int) {
        if(lastSelectedCell != null)
            lastSelectedCell!!.selection.isVisible = false

        val itemCell: ItemCell = elements[row * rows + col] as ItemCell
        itemCell.selection.isVisible = true

        lastSelectedCell = itemCell
    }

    private fun swapDragged(row: Int, col: Int) {
        val idx = row*7 + col
        val ddx = draggedIdx

        val itm  = gameState.player.items[idx]
        val ditm = gameState.player.items[ddx]
        gameState.player.items[idx] = ditm
        gameState.player.items[ddx] = itm

        val itmView  = itemViews[idx]
        val ditmView = itemViews[ddx]
        itemViews[idx] = ditmView
        itemViews[ddx] = itmView

        updateItemViews()
    }

    private fun updateItemViews() {
        for(itemIdx in itemViews.indices) {
            val itm = itemViews[itemIdx]
            itm?.updatePos(itemIdx)
        }
    }

    override fun onTouchDragged(x: Float, y: Float): Boolean {
        if(draggedItem != null) {
            draggedItem!!.bounds.setPosition(x, y); return true
        } else if(startPoint.dst2(x, y) > startDragDst) {
            val draggedItem = findItemView(startPoint.x, startPoint.y)
            if(draggedItem != null) {
                this.draggedIdx = draggedItem.itmIdx
                this.draggedItem = draggedItem
                this.draggedItem!!.bounds.setPosition(x, y)
            }
            return true
        } else {
            return false
        }
    }

    fun findItemView(x: Float, y: Float) : ItemView? {
        for(itmView in itemViews) {
            if(itmView != null && itmView.bounds.contains(x, y)) {
                return itmView
            }
        }
        return null
    }
}