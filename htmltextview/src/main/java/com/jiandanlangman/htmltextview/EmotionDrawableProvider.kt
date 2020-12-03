package com.jiandanlangman.htmltextview

import android.graphics.drawable.Drawable

interface EmotionDrawableProvider {

    fun isEmotionDrawable(text:String) : Boolean

    fun getEmotionDrawable(text: String, callback:(drawable:Drawable?) -> Unit)

}