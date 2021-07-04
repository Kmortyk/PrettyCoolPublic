//package com.kmortyk.game.ui.model.window;
//
//import android.graphics.Bitmap;
//
//import com.kmortyk.canekoandthechessking.R;
//import com.kmortyk.canekoandthechessking.gameinterface.model.Backing;
//import com.kmortyk.canekoandthechessking.gameinterface.model.ButtonElement;
//import com.kmortyk.canekoandthechessking.gameinterface.model.ElementsGroup;
//import com.kmortyk.canekoandthechessking.gameinterface.model.TextButtonElement;
//import com.kmortyk.canekoandthechessking.resources.GameResources;
//import com.kmortyk.canekoandthechessking.resources.ResourceManager;
//
//import java.util.LinkedList;
//
//public class WindowElement extends ElementsGroup {
//
//    private static final float BUTT_DIST = ResourceManager.pxFromDp(10);
//    private float curButDist;
//    private float butWidth = 0;
//
//    private GameResources gameResources;
//
//    private LinkedList<TextButtonElement> buttons;
//    private ButtonElement closeButton;
//    private Backing backing;
//
//    public WindowElement(WindowElement window) { this(window.gameResources, window.bounds.centerX(), window.bounds.centerY(), window.width(), window.height()); }
//
//    public WindowElement(GameResources gameResources, float cx, float cy, float w, float h) {
//        super(cx, cy, w, h);
//        centering();
//
//        this.gameResources = gameResources;
//        curButDist = bounds.centerY()*0.8f;
//        buttons = new LinkedList<>();
//
//        backing = new Backing(bounds.left, bounds.top, w, h);
//        backing.setFlags(true, true);
//        Bitmap backTexture = gameResources.getDrawable(R.drawable.ic_chessboard, 1f);
//        backing.setTexture(backTexture, backTexture.getWidth()/3, 0, 40);
//
//        // Log.d("WindowElement", "created");
//
//        closeButton = new ButtonElement(bounds.right, bounds.top,
//                                        gameResources.getDrawable(R.drawable.ic_exitbutton2, 1), null );
//        closeButton.offset(-closeButton.width()*1.5f, closeButton.height()*0.5f);
//
//        addElements(backing, closeButton);
//    }
//
//    public void setCloseButtonTexture(Bitmap texture) { closeButton.setTexture(texture); }
//
//    public void setOnClose(Runnable onTouch) { closeButton.setOnTouch(onTouch); }
//
//    public void addButton(String text, Runnable onTouch) {
//
//        TextButtonElement button = new TextButtonElement(text, bounds.centerX(), curButDist);
//        button.centering();
//        button.setOnTouch(onTouch);
//
//        addElements(button);
//        buttons.add(button);
//
//        if(button.width() > butWidth) {
//            butWidth = button.width();
//            for (TextButtonElement b: buttons) {
//                b.bounds.left = button.bounds.left;
//                b.bounds.right = button.bounds.right;
//                b.saveBounds();
//            }
//        }
//
//        curButDist += button.height() + BUTT_DIST;
//
//    }
//
//    public GameResources getGameResources() { return gameResources; }
//
//    public void onOpen() { /* Override */ }
//
//}