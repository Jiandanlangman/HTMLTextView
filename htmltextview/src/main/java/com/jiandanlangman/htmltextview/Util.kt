package com.jiandanlangman.htmltextview

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.text.Spannable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.TextView

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

    fun getPadding(attrs: Map<String, String>): Rect {
        val paddingRect = Rect()
        val padding = applyDimension(attrs[Constant.KEY_PADDING] ?: "-1", -1)
        val paddingLeft = run {
            val tmp = applyDimension(attrs[Constant.KEY_PADDING_LEFT] ?: "-1", -1)
            if (tmp >= 0) tmp else padding
        }
        val paddingTop = run {
            val tmp = applyDimension(attrs[Constant.KEY_PADDING_TOP] ?: "-1", -1)
            if (tmp >= 0) tmp else padding
        }
        val paddingRight = run {
            val tmp = applyDimension(attrs[Constant.KEY_PADDING_RIGHT] ?: "-1", -1)
            if (tmp >= 0) tmp else padding
        }
        val paddingBottom = run {
            val tmp = applyDimension(attrs[Constant.KEY_PADDING_BOTTOM] ?: "-1", -1)
            if (tmp >= 0) tmp else padding
        }
        paddingRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
        return paddingRect
    }

    fun getMargin(attrs: Map<String, String>): Rect {
        val marginRect = Rect()
        val margin = applyDimension(attrs[Constant.KEY_MARGIN] ?: "-1", -1)
        val marginLeft = run {
            val tmp = applyDimension(attrs[Constant.KEY_MARGIN_LEFT] ?: "-1", -1)
            if (tmp >= 0) tmp else margin
        }
        val marginTop = run {
            val tmp = applyDimension(attrs[Constant.KEY_MARGIN_TOP] ?: "-1", -1)
            if (tmp >= 0) tmp else margin
        }
        val marginRight = run {
            val tmp = applyDimension(attrs[Constant.KEY_MARGIN_RIGHT] ?: "-1", -1)
            if (tmp >= 0) tmp else margin
        }
        val marginBottom = run {
            val tmp = applyDimension(attrs[Constant.KEY_MARGIN_BOTTOM] ?: "-1", -1)
            if (tmp >= 0) tmp else margin
        }
        marginRect.set(marginLeft, marginTop, marginRight, marginBottom)
        return marginRect
    }

    fun toNaturalRect(rect:Rect) : Rect {
        rect.set(if(rect.left < 0) 0 else rect.left, if(rect.top < 0) 0 else rect.top, if(rect.right < 0) 0 else rect.right, if(rect.bottom < 0) 0 else rect.bottom)
        return rect
    }


    fun <T> getEventSpan(widget: TextView, spannable: Spannable, eventX:Float, eventY: Float, clazz: Class<T>): Array<T>? {
        val touchedLineBounds = RectF()
        var x = eventX
        var y = eventY
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout = widget.layout
        val line = layout.getLineForVertical(y.toInt())
        val off = layout.getOffsetForHorizontal(line, x)
        touchedLineBounds.left = layout.getLineLeft(line)
        touchedLineBounds.top = layout.getLineTop(line).toFloat()
        touchedLineBounds.right = layout.getLineWidth(line) + touchedLineBounds.left
        touchedLineBounds.bottom = layout.getLineBottom(line).toFloat()
        if (touchedLineBounds.contains(x, y))
            return spannable.getSpans(off, off, clazz)
        return null
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