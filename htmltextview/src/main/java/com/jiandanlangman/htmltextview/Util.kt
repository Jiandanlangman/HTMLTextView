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


    fun applyDimension(value: String, errorReturn: Int): Int {
        val trimValue = value.trim()
        if (trimValue.isEmpty())
            return errorReturn
        val unit = getUnit(value)
        return if (unit == -1)
            tryCatchInvoke({
                val v = trimValue.toFloat()
                if (v <= 0) v.toInt() else
                    (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, metrics) + .5f).toInt()
            }, errorReturn)
        else {
            val index = value.length - 2
            tryCatchInvoke({
                val v = trimValue.substring(0, index).toFloat()
                if (v <= 0) v.toInt() else (TypedValue.applyDimension(unit, v, metrics) + .5f).toInt()
            }, errorReturn)
        }
    }

    fun getCurrentLineHeight(target: HTMLTextView, top: Int, bottom: Int): Int {
        val currentLine = top / target.lineHeight
        val lineSpacingMultiplier = if (currentLine == target.lineCount - 1) 1f else target.lineSpacingMultiplier
        val currentLineHeight = (bottom - top) / lineSpacingMultiplier + .5f
        val currentLineHeightInt = currentLineHeight.toInt()
        return if (currentLineHeight - currentLineHeightInt >= .5f) currentLineHeightInt + 1 else currentLineHeightInt
    }

    private fun getUnit(value: String) = when {
        value.endsWith("dp") -> TypedValue.COMPLEX_UNIT_DIP
        value.endsWith("sp") -> TypedValue.COMPLEX_UNIT_SP
        value.endsWith("px") -> TypedValue.COMPLEX_UNIT_PX
        value.endsWith("pt") -> TypedValue.COMPLEX_UNIT_PT
        value.endsWith("in") -> TypedValue.COMPLEX_UNIT_IN
        value.endsWith("mm") -> TypedValue.COMPLEX_UNIT_MM
        else -> -1
    }


}