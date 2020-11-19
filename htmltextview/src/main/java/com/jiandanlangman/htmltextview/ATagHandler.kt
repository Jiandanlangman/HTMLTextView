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
            FontSizeASpan(target, action, textSize, color, isFakeBoldText, isUnderlineText, isLineThrough, padding, margin, pressed, style.textAlign, background)
        }
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class FontSizeASpan(private val target: HTMLTextView, private val action: String, private val textSize: Float, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean, private val padding: Rect, private val margin: Rect, private val pressed: Style.Pressed, private val textAlign: Style.TextAlign, val background: Background) : ReplacementSpan(), ActionSpan {

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize / 2
        private val textAlignOffset = (textSize - target.textSize) / 2
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
                val topBottomOffset = ((textSize - (bottom - top)) / 2f + .5f).toInt()
                val baseHeight = (textPaint.textSize + .5f).toInt()
                val totalHeight = baseHeight + padding.top + padding.bottom
                val totalWidth = (textPaint.measureText(text, start, end) + .5f).toInt() + padding.left + padding.right
                invalidateRect.left = x.toInt()
                invalidateRect.top = top + (baseHeight - totalHeight) / 2 - topBottomOffset
                invalidateRect.right = invalidateRect.left + totalWidth
                invalidateRect.bottom = invalidateRect.top + totalHeight
                val topBottomPaddingOffset = padding.top - padding.bottom
                if(topBottomPaddingOffset > 0)
                    invalidateRect.top -= topBottomPaddingOffset
                else
                    invalidateRect.bottom -= topBottomPaddingOffset
                val tao = when(textAlign) {
                    Style.TextAlign.CENTER -> 0f
                    Style.TextAlign.BOTTOM -> -textAlignOffset
                    Style.TextAlign.TOP -> textAlignOffset
                }
                invalidateRect.top += (tao + .5f).toInt()
                invalidateRect.bottom += (tao + .5f).toInt()
                if (top / target.lineHeight != target.lineCount - 1) {
                    invalidateRect.top = (invalidateRect.top - drawAlignCenterOffsetY + .5f).toInt()
                    invalidateRect.bottom = (invalidateRect.bottom - drawAlignCenterOffsetY + .5f).toInt()
                }

                canvas.save()
                canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom) / 2f)
                if (canvasScale != 1f)
                    canvas.scale(canvasScale, canvasScale, x + invalidateRect.width() / 2f, y.toFloat() - invalidateRect.height() / 2f)
                backgroundDrawable?.let { d ->
                    d.setBounds(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                    d.draw(canvas)
                }
                //因为有文本对齐选项，所以纵向背景必须移动
                val leftRightPaddingOffset = padding.left - padding.right
                canvas.translate(leftRightPaddingOffset.toFloat(), topBottomPaddingOffset / 2f) //TODO 是否合适？
                canvas.drawText(it, start, end, invalidateRect.centerX() - leftRightPaddingOffset / 2f.toFloat(), invalidateRect.centerY() + drawTextYOffset , textPaint)
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