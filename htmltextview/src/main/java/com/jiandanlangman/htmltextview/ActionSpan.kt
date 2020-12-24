package com.jiandanlangman.htmltextview

interface ActionSpan {

    fun setOnClickListener(listener:(span:ActionSpan, action:String) -> Unit)

    fun getAction():String

    fun onValid(target:HTMLTextView)

    fun onInvalid()

    fun onPressed(x:Float, y:Float) : Boolean

    fun onUnPressed(x:Float, y:Float, cancel:Boolean)

    fun getVerticalOffset() : Int


}