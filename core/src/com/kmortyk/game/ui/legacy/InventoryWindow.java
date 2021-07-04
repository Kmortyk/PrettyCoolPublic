//package com.kmortyk.game.ui;
//
//import android.content.res.Configuration;
//import android.graphics.RectF;
//
//import com.kmortyk.canekoandthechessking.game.item.GameItem;
//import com.kmortyk.canekoandthechessking.gameinterface.model.InventoryCell;
//import com.kmortyk.canekoandthechessking.gameinterface.model.TextElement;
//import com.kmortyk.canekoandthechessking.gameinterface.model.window.WindowElement;
//
//import com.kmortyk.canekoandthechessking.game.item.Inventory;
//import com.kmortyk.canekoandthechessking.resources.ResourceManager;
//import com.kmortyk.canekoandthechessking.thread.GameThread;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class InventoryWindow extends WindowElement {
//
//    private Inventory inventory;
//    private List<InventoryCell> cells;
//
//    private float cellSize;
//    private float cellPadding;
//
//    public InventoryWindow(GameThread gameThread, WindowElement window) { this(gameThread, window.bounds.centerX(), window.bounds.centerY(), window.width(), window.height()); }
//
//    public InventoryWindow(GameThread gameThread, float cx, float cy, float w, float h) {
//        super(gameThread.getResources(), cx, cy, w, h);
//        inventory = gameThread.getGameWorld().getInventory();
//        cells = new ArrayList<>(Inventory.MAX_COUNT);
//
//        boolean isLandscape = (gameThread.getOrientation() == Configuration.ORIENTATION_LANDSCAPE);
//
//        cellSize    = isLandscape ? ResourceManager.pxFromDp(44.82f) : ResourceManager.pxFromDp(54);
//        cellPadding = cellSize / 6.0f;
//
//        int cols = isLandscape ? 5 : 4;
//        int rows = isLandscape ? 4 : 5;
//
//        TextElement textElement = new TextElement("Inventory", cx, bounds.top);
//
//        RectF cellsBounds = new RectF(0, 0, (cellSize + cellPadding) * cols + cellPadding, (cellSize + cellPadding) * rows + cellPadding);
//        cellsBounds.offsetTo(cx - cellsBounds.width()*0.5f, cy + textElement.height() - cellsBounds.height()*0.5f);
//
//        textElement.offsetTo(textElement.bounds.left - textElement.width()*0.5f, cellsBounds.top - textElement.height()*1.5f);
//
//        for (float y = cellsBounds.top + cellPadding; y < cellsBounds.bottom; y += cellSize + cellPadding) {
//            for (float x = cellsBounds.left + cellPadding; x < cellsBounds.right; x += cellSize + cellPadding) {
//                InventoryCell cell = new InventoryCell(x, y, cellSize, cellSize);
//                cells.add(cell);
//                addElements(cell);
//            }
//        }
//
//        addElements(textElement);
//    }
//
//    @Override
//    public void onOpen() {
//
//        int count = 0;
//
//        for(GameItem it: inventory.getItems()) {
//            InventoryCell cell = cells.get(count);
//            cell.setItem(it);
//            count++;
//        }
//
//    }
//
//    public List<InventoryCell> getCells() { return cells; }
//
//    public float getCellSize() { return cellSize; }
//
//    public float getCellPadding() { return cellPadding; }
//
//}
