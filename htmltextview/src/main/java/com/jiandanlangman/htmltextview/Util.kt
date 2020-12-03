package com.jiandanlangman.htmltextview

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

internal object Util {


    private var isInit = false

    private lateinit var metrics: DisplayMetrics

    fun init(context: Context) {
        if (isInit)
            return
        metrics = context.resources.displayMetrics
        isInit = true
    }

    fun <T> tryCatchInvoke(runnable: () -> T, errorReturn: T): T {
        return try {
            runnable.invoke()
        } catch (ignore: Throwable) {
            errorReturn
        }
    }

    fun tryCatchInvoke(runnable: () -> Unit) {
        try {
            runnable.invoke()
        } catch (ignore: Throwable) {

        }
    }


    fun applyDimension(value: String, errorReturn:Int): Int {
        val trimValue = value.trim()
        if(trimValue.isEmpty())
            return errorReturn
        val unit = getUnit(value)
        return  if(unit == -1)
            tryCatchInvoke({ (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, trimValue.toFloat(), metrics) + .5f).toInt() }, errorReturn)
         else {
            val index = value.length - 2
            tryCatchInvoke({ (TypedValue.applyDimension(unit, trimValue.substring(0, index).toFloat(), metrics) + .5f).toInt() }, errorReturn)
        }
    }

    private fun getUnit(value:String) = when {
        value.endsWith("dp") -> TypedValue.COMPLEX_UNIT_DIP
        value.endsWith("sp") -> TypedValue.COMPLEX_UNIT_SP
        value.endsWith("px") -> TypedValue.COMPLEX_UNIT_PX
        value.endsWith("pt") -> TypedValue.COMPLEX_UNIT_PT
        value.endsWith("in") -> TypedValue.COMPLEX_UNIT_IN
        value.endsWith("mm") -> TypedValue.COMPLEX_UNIT_MM
        else -> -1
    }


}