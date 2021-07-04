//package com.kmortyk.game.ui.model;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//
//import com.kmortyk.canekoandthechessking.game.item.GameItem;
//import com.kmortyk.canekoandthechessking.resources.ResourceManager;
//
//public class InventoryCell extends ElementsGroup {
//
//    private Backing backing;
//    private TextElement count;
//    private GameItem curItem;
//
//    public InventoryCell(float left, float top, float w, float h) {
//        super(left, top, w, h);
//
//        backing = new Backing(bounds);
//        backing.setFlags(true, true);
//
//        count = new TextElement("", left + w, top + h);
//        count.setFontSize(ResourceManager.pxFromDp(10));
//
//        addElements(backing, count);
//    }
//
//    @Override
//    public void onDraw(Canvas canvas) {
//        backing.draw(canvas);
//        if(curItem != null && curItem.is(GameItem.STACKABLE)) count.draw(canvas);
//    }
//
//    public void setItem(GameItem item) {
//        curItem = item;
//        updateCount();
//
//        Bitmap texture = item.fullTextureName == null ?
//                item.texture :
//                ResourceManager.getInstance().loadDrawable(
//                        ResourceManager.getInstance().getResourceId(item.fullTextureName, "drawable"),
//                        bounds.width(),
//                        bounds.height()
//                );
//
//        backing.setTexture(texture, 0, 0, 255);
//    }
//
//    private void updateCount() {
//        count.setText(curItem.getCount() + "");
//        count.offset(-count.width()-count.height()*0.5f, -count.height()*0.5f);
//    }
//
//}
