//package com.kmortyk.game.ui.model;
//
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import com.badlogic.gdx.assets.AssetManager;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//
///**
// * Draw row of circles, which show the total number
// * of levels in the race and the current level.
// *
// * approximate view (max=4, cur=2):
// * o-*-o-o
// */
//
//public class Ruler extends InterfaceElement {
//
//    private final Paint paint, fillPaint;
//
//    private float radius; // radius of circles
//    private float cx, cy; // center of element
//
//    private int max; // number of levels in race
//    private int cur; // current level
//
//    private float dx; // distance between centers of circles
//
//    /**
//     * @param cx center X
//     * @param cy center Y
//     * @param radius circles radius
//     * @param max number of circles
//     * @param cur which circle to fill
//     */
//    public Ruler(float cx, float cy, float radius, int max, int cur) {
//        super(cx, cy, max*radius, radius);
//        this.radius = radius;
//        this.cx  =  cx;
//        this.cy  =  cy;
//        this.cur = cur;
//        this.max = max;
//
//        paint = new Paint();
//        paint.setColor(Color.parseColor("#B1ADAC"));
//        paint.setStrokeWidth(1);
//        paint.setAlpha(88);
//        paint.setStyle(Paint.Style.STROKE);
//
//        fillPaint = new Paint(paint);
//        fillPaint.setStyle(Paint.Style.FILL);
//
//        dx = radius*3;
//        float width = (max-1)*dx;
//        this.cx -= width*0.5f;
//    }
//
//    @Override
//    public void onDraw(AssetManager assetManager, SpriteBatch spriteBatch) {
//
//        for (int c = 0; c < max; c++) {
//
//            // center + index * distance_between
//            float x = cx + c*dx;
//
//            // draw line between circles
//            if(c != max-1) { canvas.drawLine(x+radius, cy, x+radius*2, cy, paint); }
//
//            // draw circle
//            if(c == cur-1) { canvas.drawCircle(x, cy, radius, fillPaint); }
//            else           { canvas.drawCircle(x, cy, radius, paint);     }
//
//        }
//
//    }
//
//    @Override
//    public boolean onTouch(float x, float y) { return false; }
//
//    public void nextCur() { if(++cur > max) cur = 0; }
//
//}
