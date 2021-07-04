//package com.kmortyk.game.ui.legacy.effect;
//
//import com.badlogic.gdx.math.Vector2;
//import com.kmortyk.game.effect.Effect;
//import com.kmortyk.game.ui.model.InterfaceElement;
//
//public class Move extends Effect {
//
//    private static final float movingSpeed = 100;
//
//    private Vector2 curPos = new Vector2();
//    private Vector2 toPos = new Vector2();
//
//    private InterfaceElement e;
//
//    public Move(InterfaceElement e, float x, float y) {
//        this.e = e;
//        curPos.set(e.bounds.x, e.bounds.y);
//        toPos.set(x, y);
//    }
//
//    @Override
//    public boolean extend(float delta) {
//        float speed = movingSpeed * delta;
//
//        float cx = curPos.x;
//        float cy = curPos.y;
//
//        if(cx < toPos.x) cx += speed;
//        if(cx > toPos.x) cx -= speed;
//
//        if(cy < toPos.y) cy += speed;
//        if(cy > toPos.y) cy -= speed;
//
//        curPos.set(cx, cy);
//        e.offsetTo(cx, cy);
//
//        return curPos.dst(toPos) > 1;
//    }
//}
