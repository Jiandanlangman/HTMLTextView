package com.jiandanlangman.htmltextview

interface ActionSpan {

    fun setOnClickListener(listener:(span:ActionSpan, action:String) -> Unit)

    fun onPressed()

    fun onUnPressed(isClick: Boolean)

    fun getAction():String?

}