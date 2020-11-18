package com.jiandanlangman.htmltextview

import android.text.Editable

interface TagHandler {

    fun handleTag(target:HTMLTextView, tag:String, output:Editable, start:Int, attrs: Map<String, String>, style:Style, background: Background)


}