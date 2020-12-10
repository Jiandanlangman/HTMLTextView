package com.jiandanlangman.htmltextview

import android.graphics.Typeface
import android.graphics.drawable.Drawable

interface ResourcesProvider {

    fun getImageDrawable(target:HTMLTextView, src:String, callback:(result: Drawable?) -> Unit)

    fun getTypeface(name:String) : Typeface?

    fun isEmotionDrawable(text:String) : Boolean

    fun getEmotionDrawable(text: String, callback:(drawable: Drawable?) -> Unit)

}