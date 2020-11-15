package com.jiandanlangman.htmltextview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ReplacementSpan


class ATagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, styles: Map<String, String>) {
        output.setSpan(ASpan(target, attrs, styles), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ASpan(target: HTMLTextView, attrs: Map<String, String>, styles: Map<String, String>) : ReplacementSpan(), ActionSpan {

        private val density = target.resources.displayMetrics.density
        private val action = attrs[Attributes.ACTION.name] ?: ""

        private val color: Int = try {
            Color.parseColor(styles["color"])
        } catch (ignore: Throwable) {
            target.textColors.defaultColor
        }
        private val isFakeBoldText: Boolean = "bold".equals(styles["font-weight"], target.paint.isFakeBoldText)
        private val isUnderlineText: Boolean = "underline".equals(styles["text-decoration"], target.paint.isUnderlineText)
        private val isThrLineText: Boolean = "line-through".equals(styles["text-decoration"], true)
        private val textSize: Float = try {
            (styles["font-size"] ?: error("")).toFloat()
        } catch (ignore: Throwable) {
            target.textSize
        }
        private val alignCenter = "center".equals(styles["text-align"], true)

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize

        private var paint: TextPaint? = null

        init {

        }

        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    it.density = density
                    it.color = color
                    it.isFakeBoldText = isFakeBoldText
                    it.textSize = textSize * it.density
                    it.isUnderlineText = isUnderlineText
                    it.flags = if (isThrLineText) it.flags or Paint.STRIKE_THRU_TEXT_FLAG else it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = text?.let { getTextPaint(paint).measureText(text, start, end).toInt() } ?: 0

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            text?.let {
                val textPaint = getTextPaint(paint)
                if (alignCenter) {
                    val fontMetricsInt = textPaint.fontMetricsInt
                    val offsetY = (y + fontMetricsInt.ascent + y + fontMetricsInt.descent) / 2f - (top + bottom) / 2f + drawAlignCenterOffsetY
                    canvas.drawText(it, start, end, x, y - offsetY, textPaint)
                } else
                    canvas.drawText(it, start, end, x, y.toFloat(), textPaint)
            }
        }


        override fun getAction() = action

        override fun onPressed() = Unit

        override fun onUnPressed() = Unit

    }


}