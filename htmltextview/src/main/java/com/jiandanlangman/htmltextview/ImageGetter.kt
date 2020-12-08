package com.jiandanlangman.htmltextview

import android.graphics.drawable.Drawable

interface ImageGetter {

    fun getImageDrawable(target:HTMLTextView, src:String, callback:(result: Drawable?) -> Unit)

}