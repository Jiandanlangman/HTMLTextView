package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.View
import androidx.core.animation.doOnEnd


class ATagHandler : TagHandler {

    @SuppressLint("Range")
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        val density = target.resources.displayMetrics.density
        val action = attrs[Attribute.ACTION.value] ?: ""
        val color = Util.tryCatchInvoke({ Color.parseColor(style.color) }, target.textColors.defaultColor)
        val isFakeBoldText = when (style.fontWeight) {
            Style.FontWeight.NORMAL -> false
            Style.FontWeight.BOLD -> true
            else -> target.paint.isFakeBoldText
        }
        val textSize: Float = if (style.fontSize >= 0) Util.dpToPx(style.fontSize, density).toFloat() else target.textSize
        val isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
        val isLineThrough = style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)
        val pressed = style.pressed
        val span = if (textSize == target.textSize && style.padding.left < 0 && style.padding.top < 0 && style.padding.right < 0 && style.padding.bottom < 0 && style.margin.left < 0 && style.margin.top < 0 && style.margin.right < 0 && style.margin.bottom < 0 && pressed == Style.Pressed.NONE && background.isNotBackground())
            ASpan(action, color, isFakeBoldText, isUnderlineText, isLineThrough)
        else {
            val padding = Rect(
                if (style.padding.left < 0) 0 else Util.dpToPx(style.padding.left, density),
                if (style.padding.top < 0) 0 else Util.dpToPx(style.padding.top, density),
                if (style.padding.right < 0) 0 else Util.dpToPx(style.padding.right, density),
                if (style.padding.bottom < 0) 0 else Util.dpToPx(style.padding.bottom, density)
            )
            val margin = Rect(
                if (style.margin.left < 0) 0 else Util.dpToPx(style.margin.left, density),
                if (style.margin.top < 0) 0 else Util.dpToPx(style.margin.top, density),
                if (style.margin.right < 0) 0 else Util.dpToPx(style.margin.right, density),
                if (style.margin.bottom < 0) 0 else Util.dpToPx(style.margin.bottom, density)
            )
            FontSizeASpan(target, action, textSize, color, isFakeBoldText, isUnderlineText, isLineThrough, padding,margin,  pressed, style.textAlign, background)
        }
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class FontSizeASpan(private val target: HTMLTextView, private val action: String, private val textSize: Float, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean, private val padding: Rect, private val margin:Rect, private val pressed: Style.Pressed, private val textAlign: Style.TextAlign, val background: Background) : ReplacementSpan(), ActionSpan {

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize
        private val textAlignOffset = (textSize - target.textSize) / 2
        private val datumRect = Rect()
        private val invalidateRect = Rect()

        private var canvasScale = 1f
        private var drawTextYOffset = 0f


        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null
        private var backgroundDrawable: Drawable? = null

        init {
            background.getDrawable(target) {
                backgroundDrawable = it
                it?.let { target.invalidate() }
            }
        }

        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    it.textAlign = Paint.Align.CENTER
                    it.color = color
                    it.isFakeBoldText = isFakeBoldText
                    it.textSize = textSize
                    it.isUnderlineText = isUnderlineText
                    it.flags = if (isLineThrough) it.flags or Paint.STRIKE_THRU_TEXT_FLAG else it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    val fontMetrics = it.fontMetrics
                    drawTextYOffset = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
                }
            }
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val width = text?.let { (getTextPaint(paint).measureText(text, start, end) + .5f).toInt() } ?: 0
            return width + padding.left + padding.right + margin.left + margin.right
        }


        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            text?.let {
                val textPaint = getTextPaint(paint)
                val size = textPaint.measureText(text, start, end)
                val topBottomOffset = ((textSize - (bottom - top)) / 2f + .5f).toInt()
                datumRect.left = (x + margin.left + .5f).toInt()
                datumRect.top = top - topBottomOffset - padding.top + (margin.top - margin.bottom)
                datumRect.right = (datumRect.left + size + .5f + padding.left + padding.right).toInt()
                if (target.lineCount > 1) //TODO，判断是否是最后一行
                    datumRect.bottom = (bottom + topBottomOffset - drawAlignCenterOffsetY + padding.bottom + (margin.top - margin.bottom)).toInt()
                else
                    datumRect.bottom = bottom + topBottomOffset + padding.bottom + (margin.top - margin.bottom)
                invalidateRect.left = datumRect.left
                invalidateRect.right = datumRect.right
                val offset = when (textAlign) {
                    Style.TextAlign.TOP -> {
                        val offsetInt = (textAlignOffset + .5f).toInt()
                        invalidateRect.top = datumRect.top + offsetInt
                        invalidateRect.bottom = datumRect.bottom + offsetInt
                        textAlignOffset
                    }
                    Style.TextAlign.BOTTOM -> {
                        val offsetInt = (textAlignOffset + .5f).toInt()
                        invalidateRect.top = datumRect.top - offsetInt
                        invalidateRect.bottom = datumRect.bottom - offsetInt
                        -textAlignOffset
                    }
                    else -> {
                        invalidateRect.top = datumRect.top
                        invalidateRect.bottom = datumRect.bottom
                        0f
                    }
                }
                canvas.save()
                if (canvasScale != 1f)
                    canvas.scale(canvasScale, canvasScale, x + size / 2, y.toFloat() - textSize / 2)
                backgroundDrawable?.let { d ->
                    d.setBounds(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                    d.draw(canvas)
                }
                canvas.drawText(it, start, end, datumRect.centerX().toFloat(), datumRect.centerY() + drawTextYOffset + offset, textPaint)
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