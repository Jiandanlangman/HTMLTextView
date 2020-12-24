package com.jiandanlangman.htmltextview

interface ActionSpan {

    fun setOnClickListener(listener:(span:ActionSpan, action:String) -> Unit)

    fun getAction():String

    fun onValid(target:HTMLTextView)

    fun onInvalid()

    fun onPressed()

    fun onUnPressed(isClick: Boolean)

    fun getVerticalOffset() : Int


}