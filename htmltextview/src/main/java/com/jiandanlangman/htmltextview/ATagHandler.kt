package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.View
import androidx.core.animation.doOnEnd


class ATagHandler : TagHandler {

    @SuppressLint("Range")
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style) {
        val density = target.resources.displayMetrics.density
        val action = attrs[Attribute.ACTION.value] ?: ""
        val color = Util.tryCatchInvoke({ Color.parseColor(style.color) }, target.textColors.defaultColor)
        val isFakeBoldText = when (style.fontWeight) {
            Style.FontWeight.NORMAL -> false
            Style.FontWeight.BOLD -> true
            else -> target.paint.isFakeBoldText
        }
        val textSize: Float = if (style.fontSize >= 0) style.fontSize * density else target.textSize
        val isUnderlineText = style.textDecoration == Style.TextDecoration.UNDERLINE
        val isLineThrough = !isUnderlineText && style.textDecoration == Style.TextDecoration.LINE_THROUGH
        val pressed = style.pressed
        val span = if (textSize == target.textSize && style.padding.left < 0 && style.padding.top < 0 && style.padding.right < 0 && style.padding.bottom < 0 && pressed == Style.Pressed.NONE)
            ASpan(action, color, isFakeBoldText, isUnderlineText, isLineThrough)
        else {
            val padding = RectF(
                if (style.padding.left < 0) 0f else style.padding.left * density,
                if (style.padding.top < 0) 0f else style.padding.top * density,
                if (style.padding.right < 0) 0f else style.padding.right * density,
                if (style.padding.bottom < 0) 0f else style.padding.bottom * density
            )
            FontSizeASpan(target, action, textSize, color, isFakeBoldText, isUnderlineText, isLineThrough, padding, pressed, style.textAlign)
        }
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class FontSizeASpan(private val target: HTMLTextView, private val action: String, private val textSize: Float, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean, private val padding: RectF, private val pressed: Style.Pressed, private val textAlign: Style.TextAlign) : ReplacementSpan(), ActionSpan {

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize / 2
        private val invalidateRect = Rect()

        private var canvasScale = 1f

        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null

        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    it.color = color
                    it.isFakeBoldText = isFakeBoldText
                    it.textSize = textSize
                    it.isUnderlineText = isUnderlineText
                    it.flags = if (isLineThrough) it.flags or Paint.STRIKE_THRU_TEXT_FLAG else it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val width = text?.let { getTextPaint(paint).measureText(text, start, end) } ?: 0f
            return (width + padding.left + padding.right).toInt()
        }


        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            text?.let {
                val textPaint = getTextPaint(paint)
                val fontMetricsInt = textPaint.fontMetricsInt
                val offsetY = (y + fontMetricsInt.ascent + y + fontMetricsInt.descent) / 2f - (top + bottom) / 2f + drawAlignCenterOffsetY - (padding.top - padding.bottom)
                val size = getSize(paint, text, start, end, fontMetricsInt)
                invalidateRect.left = x.toInt()
                invalidateRect.top = (y - padding.top   - offsetY - textSize ) .toInt()
                invalidateRect.right = invalidateRect.left + size
                invalidateRect.bottom = (invalidateRect.top + textSize + padding.top - offsetY * 2 + padding.bottom).toInt()
                canvas.save()
                if (canvasScale != 1f)
                    canvas.scale(canvasScale, canvasScale, x + size / 2, y.toFloat() - textSize / 2)
                canvas.drawText(it, start, end, x + padding.left, if(textAlign == Style.TextAlign.CENTER) y - offsetY else y.toFloat(), textPaint)
                canvas.restore()
            }
        }


        override fun getAction() = action

        override fun onPressed() {
            if (action.isNotEmpty())
                when (pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(1f, .88f)
                    Style.Pressed.NONE -> Unit
                }
        }

        override fun onUnPressed() {
            if (action.isNotEmpty())
                when (pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(.88f, 1f)
                    Style.Pressed.NONE -> Unit
                }
        }

        private fun playScaleAnimator(from: Float, to: Float) {
            scaleAnimator?.cancel()
            scaleAnimator = ValueAnimator.ofFloat(from, to)
            scaleAnimator!!.let {
                it.duration = 64
                it.addUpdateListener { _ ->
                    canvasScale = it.animatedValue as Float
                    target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                }
                it.doOnEnd { scaleAnimator = null }
                it.start()
            }
        }

    }


    private class ASpan(private val action: String, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean) : ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
            ds.isFakeBoldText = isFakeBoldText
            ds.isUnderlineText = isUnderlineText
            ds.flags = if (isLineThrough) ds.flags or Paint.STRIKE_THRU_TEXT_FLAG else ds.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        override fun onClick(widget: View) {
            if (action.isNotEmpty())
                (widget as HTMLTextView).onAction(action)
        }
    }

}