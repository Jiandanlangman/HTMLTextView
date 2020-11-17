package com.jiandanlangman.htmltextview

import android.graphics.drawable.Drawable

interface ImageGetter {

    fun getImageDrawable(src:String, type:String, callback:(result: Drawable?) -> Unit)

}