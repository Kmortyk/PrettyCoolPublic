//package com.kmortyk.game.ui.model.window;
//
//import com.badlogic.gdx.assets.AssetManager;
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.math.Vector2;
//import com.kmortyk.game.ui.model.Backing;
//import com.kmortyk.game.ui.model.button.ImageButtonElement;
//import com.kmortyk.game.ui.model.ElementsGroup;
//import com.kmortyk.game.ui.element.TextElement;
//
//public class HeaderWindowElement extends ElementsGroup {
//
//    private ImageButtonElement closeButton;
//    private TextElement headerText;
//
//    public HeaderWindowElement(AssetManager assetManager, float cx, float cy, float w, float h) {
//        super(cx, cy, w, h);
//        centering();
//
//        Backing backing = new Backing(bounds.x, bounds.y, w, h);
//
//        Backing header = new Backing(bounds.x, bounds.y, w, h * 0.1f);
//        header.setAlpha(255);
//        header.setColor(Color.BLUE);
//        header.setFlags(true, false);
//
//        System.out.println("Simple_window created");
//
//        headerText = new TextElement("Simple window", header.bounds.x, header.bounds.getCenter(new Vector2()).y);
//        headerText.setPadding(10f, 5f);
//
//        closeButton = new ImageButtonElement(header.bounds.right, header.bounds.centerY(),
//                                        gameResources.getDrawable(R.drawable.ic_exitbutton, 1), null );
//        closeButton.centering();
//        closeButton.bounds.offset(-closeButton.bounds.width(), 0);
//
//        addElements(backing, header, headerText, closeButton);
//    }
//
//    public void setHeaderText(String text) { headerText.setText(text); }
//
//    public void setOnClose(Runnable onTouch) { closeButton.setOnTouch(onTouch); }
//}
