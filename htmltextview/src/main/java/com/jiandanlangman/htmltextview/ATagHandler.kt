package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import androidx.core.animation.doOnEnd


class ATagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style) {
        output.setSpan(ASpan(target, attrs, style), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ASpan(private val target: HTMLTextView, attrs: Map<String, String>, private val style: Style) : ReplacementSpan(), ActionSpan {

        private val density = target.resources.displayMetrics.density
        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val paddingLeft = if (style.padding.left < 0) 0f else style.padding.left * density
        private val paddingRight = if (style.padding.right < 0) 0f else style.padding.right * density
        private val paddingTop = if (style.padding.top < 0) 0f else style.padding.top * density

        private val color: Int = try {
            Color.parseColor(style.color)
        } catch (ignore: Throwable) {
            target.textColors.defaultColor
        }
        private val isFakeBoldText: Boolean = when (style.fontWeight) {
            Style.FontWeight.NORMAL -> false
            Style.FontWeight.BOLD -> true
            else -> target.paint.isFakeBoldText
        }

        private val textSize: Float = if (style.fontSize >= 0) style.fontSize.toFloat() else target.textSize / density

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize

        private val bounds = Bounds()

        private var canvasScale = 1f

        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null

        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    it.density = density
                    it.color = color
                    it.isFakeBoldText = isFakeBoldText
                    it.textSize = textSize * it.density
                    when (style.textDecoration) {
                        Style.TextDecoration.UNDERLINE -> it.isUnderlineText = true
                        Style.TextDecoration.LINE_THROUGH -> it.flags = it.flags or Paint.STRIKE_THRU_TEXT_FLAG
                        else -> {
                            it.isUnderlineText = false
                            it.flags = it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }
                }
            }
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val width = text?.let { getTextPaint(paint).measureText(text, start, end) } ?: 0f
            return (width + paddingLeft + paddingRight).toInt()
        }

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            text?.let {
                val size = getSize(paint, text, start, end, paint.fontMetricsInt)
                bounds.left = x.toInt()
                bounds.top = y
                bounds.right = bounds.left + size
                bounds.bottom = (bounds.top + textSize).toInt()
                canvas.save()
                canvas.translate(paddingLeft, paddingTop)
                if (canvasScale != 1f)
                    canvas.scale(canvasScale, canvasScale, x + size / 2, y.toFloat() - textSize / 2)
                val textPaint = getTextPaint(paint)
                if (Style.TextAlign.CENTER == style.textAlign) {
                    val fontMetricsInt = textPaint.fontMetricsInt
                    val offsetY = (y + fontMetricsInt.ascent + y + fontMetricsInt.descent) / 2f - (top + bottom) / 2f + drawAlignCenterOffsetY
                    canvas.drawText(it, start, end, x, y - offsetY, textPaint)
                } else
                    canvas.drawText(it, start, end, x, y.toFloat(), textPaint)
                canvas.restore()
            }
        }


        override fun getAction() = action

        override fun onPressed() {
            when (style.pressed) {
                Style.Pressed.SCALE -> playScaleAnimator(1f, .88f)
                Style.Pressed.NONE -> {
                }
            }
        }

        override fun onUnPressed() {
            when (style.pressed) {
                Style.Pressed.SCALE -> playScaleAnimator(.88f, 1f)
                Style.Pressed.NONE -> {
                }
            }
        }

        private fun playScaleAnimator(from: Float, to: Float) {
            scaleAnimator?.cancel()
            scaleAnimator = ValueAnimator.ofFloat(from, to)
            scaleAnimator!!.let {
                it.duration = 64
                it.addUpdateListener { _ ->
                    canvasScale = it.animatedValue as Float
                    target.postInvalidate(bounds.left, bounds.top, bounds.right, bounds.bottom)
                }
                it.doOnEnd { scaleAnimator = null }
                it.start()
            }
        }

    }


}