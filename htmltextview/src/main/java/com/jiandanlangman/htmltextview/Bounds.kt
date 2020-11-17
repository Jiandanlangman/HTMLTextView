package com.jiandanlangman.htmltextview

class Bounds(var left:Int = 0, var top:Int = 0, var right:Int = 0, var bottom:Int = 0) {
    fun set(left:Int, top:Int, right:Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }
}