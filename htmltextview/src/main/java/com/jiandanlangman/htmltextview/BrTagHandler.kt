package com.jiandanlangman.htmltextview

import android.text.Editable

class BrTagHandler : TagHandler {
    override fun handleTag(tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        val count =  Util.tryCatchInvoke({ (attrs[Attribute.COUNT.value] ?: "1") .toInt()}, 1)
        for(i in 0 until count)
            output.append("\n")
    }

    override fun isSingleTag() = true
}