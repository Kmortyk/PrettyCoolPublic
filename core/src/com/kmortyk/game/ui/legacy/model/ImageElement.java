//package com.kmortyk.game.ui.model;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//
//public class ImageElement extends InterfaceElement {
//
//    private Bitmap texture;
//    private final Paint paint;
//
//    public ImageElement(Bitmap texture, float left, float top) {
//        super(left, top, texture.getWidth(), texture.getHeight());
//        this.texture = texture;
//        paint = new Paint();
//    }
//
//    public void setTexture(Bitmap texture) { this.texture = texture; }
//
//    @Override
//    public void onDraw(Canvas canvas) { canvas.drawBitmap(texture, bounds.left, bounds.top, paint); }
//
//    @Override
//    public boolean onTouch(float x, float y) { return false; }
//
//}
