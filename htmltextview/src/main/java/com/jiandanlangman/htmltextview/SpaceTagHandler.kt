package com.jiandanlangman.htmltextview

import android.text.Editable

class SpaceTagHandler : TagHandler {
    override fun handleTag(tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        val count = Util.tryCatchInvoke({(attrs[Attribute.COUNT.value] ?: "0").toInt() }, 0)
        for(i in 0 until count)
            output.append(" ")
    }

    override fun isSingleTag() = true


}