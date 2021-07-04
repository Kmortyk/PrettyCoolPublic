package com.kmortyk.game.ui

import com.badlogic.gdx.math.Rectangle

fun Rectangle.right() : Float {
    return this.x + this.width
}

fun Rectangle.left() : Float {
    return this.x
}

fun Rectangle.bottom() : Float {
    return this.y
}

fun Rectangle.top() : Float {
    return this.y + this.height
}

fun Rectangle.centerX() : Float {
    return this.x + this.width*0.5f
}

fun Rectangle.centerY() : Float {
    return this.y + this.height*0.5f
}

fun Rectangle.adjust(offset: Int, sign: Int) {
    this.x -= offset*sign
    this.y -= offset*sign
    this.width += offset*sign
    this.height += offset*sign
}