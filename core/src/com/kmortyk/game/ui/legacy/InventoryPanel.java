//package com.kmortyk.game.ui;
//
//import android.graphics.RectF;
//
//import com.kmortyk.canekoandthechessking.game.GameWorld;
//import com.kmortyk.canekoandthechessking.game.effects.Effect;
//import com.kmortyk.canekoandthechessking.game.item.Inventory;
//import com.kmortyk.canekoandthechessking.gameinterface.effect.Move;
//import com.kmortyk.canekoandthechessking.gameinterface.model.Backing;
//import com.kmortyk.canekoandthechessking.gameinterface.model.ElementsGroup;
//import com.kmortyk.canekoandthechessking.gameinterface.model.InventoryCell;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class InventoryPanel extends ElementsGroup {
//
//    private List<InventoryCell> cells;
//    private GameWorld gameWorld;
//
//    private float panelHeight;
//    private float height;
//    private float topY;
//
//    public InventoryPanel(GameWorld gameWorld, InventoryWindow inventoryWindow, int width, int height) {
//        this.gameWorld = gameWorld;
//        this.height = height;
//
//        cells = new ArrayList<>(Inventory.MAX_COUNT);
//
//        float cellSize = inventoryWindow.getCellSize();
//        float cellPadding = inventoryWindow.getCellPadding();
//
//        panelHeight = cellSize + cellPadding * 2;
//        topY = height - panelHeight;
//
//        Backing backing = new Backing(0, topY, width, panelHeight);
//        addElements(backing);
//
//        // !
//        bounds = new RectF(backing.bounds);
//
//        RectF cellsBounds = new RectF(0, topY, (cellSize + cellPadding) * Inventory.MAX_COUNT + cellPadding, height);
//
//        for (float x = cellsBounds.left + cellPadding; x < cellsBounds.right; x += cellSize + cellPadding) {
//            InventoryCell cell = new InventoryCell(x, topY + cellPadding, cellSize, cellSize);
//            cells.add(cell);
//            addElements(cell);
//        }
//
//        offset(0, panelHeight);
//
//    }
//
//    public void swipeUp() {
//        setVisible(true);
//        gameWorld.addEffect(new Move(this, 0, topY));
//    }
//
//    public void swipeDown() {
//        Effect e = new Move(this, 0, height);
//        e.setCallback(() -> setVisible(false));
//        gameWorld.addEffect(e);
//    }
//
//    public float topY() { return topY; }
//
//}
