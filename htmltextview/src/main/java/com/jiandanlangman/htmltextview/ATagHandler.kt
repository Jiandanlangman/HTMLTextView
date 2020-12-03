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
import java.lang.ref.WeakReference


class ATagHandler : TagHandler {

    @SuppressLint("Range")
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        val action = attrs[Attribute.ACTION.value] ?: ""
        val color = Util.tryCatchInvoke({ Color.parseColor(style.color) }, target.textColors.defaultColor)
        val isFakeBoldText = when (style.fontWeight) {
            Style.FontWeight.NORMAL -> false
            Style.FontWeight.BOLD -> true
            else -> target.paint.isFakeBoldText
        }
        val textSize: Float = if (style.fontSize >= 0) style.fontSize.toFloat() else target.textSize
        val isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
        val isLineThrough = style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)
        val pressedScale = style.pressedScale
        val pressedTintColor = Util.tryCatchInvoke({ Color.parseColor(style.pressedTint) }, Color.TRANSPARENT)
        val span = if (textSize == target.textSize && style.padding.left < 0 && style.padding.top < 0 && style.padding.right < 0 && style.padding.bottom < 0 && style.margin.left < 0 && style.margin.top < 0 && style.margin.right < 0 && style.margin.bottom < 0 && pressedScale == 1f && pressedTintColor == Color.TRANSPARENT && background.isNotBackground())
            ASpan(action, color, isFakeBoldText, isUnderlineText, isLineThrough)
        else
            FontSizeASpan(target, action, textSize, color, isFakeBoldText, isUnderlineText, isLineThrough, style.padding, style.margin, pressedScale, pressedTintColor, style.textAlign, background)
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class FontSizeASpan(private val target: HTMLTextView, private val action: String, private val textSize: Float, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean, private val padding: Rect, private val margin: Rect, private val pressedScale: Float, private val pressedTintColor: Int, private val textAlign: Style.TextAlign, background: Background) : ReplacementSpan(), ActionSpan {

        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * textSize / 2
        private val textAlignOffset = (textSize - target.textSize) / 2
        private val invalidateRect = Rect()

        private var canvasScale = 1f
        private var drawTextYOffset = 0f
        private var pressed = false

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }
        private var targetAttachState = 0

        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null
        private var backgroundDrawable: WeakReference<Drawable>? = null

        init {
            if (target.isAttachedToWindow)
                targetAttachState = 1
            target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    targetAttachState = 1
                }

                override fun onViewDetachedFromWindow(v: View?) {
                    targetAttachState = 2
                    target.removeOnAttachStateChangeListener(this)
                }

            })
            background.getDrawable {
                if (targetAttachState == 2)
                    return@getDrawable
                backgroundDrawable = WeakReference(it)
                it?.let {
                    if (targetAttachState == 1)
                        target.invalidate()
                }
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
                if (topBottomPaddingOffset > 0)
                    invalidateRect.top -= topBottomPaddingOffset
                else
                    invalidateRect.bottom -= topBottomPaddingOffset
                val tao = when (textAlign) {
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
                backgroundDrawable?.get()?.let { d ->
                    d.setBounds(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                    d.draw(canvas)
                }
                //因为有文本对齐选项，所以纵向背景必须移动
                val leftRightPaddingOffset = padding.left - padding.right
                canvas.translate(leftRightPaddingOffset.toFloat(), topBottomPaddingOffset / 2f) //TODO 是否合适？
                canvas.drawText(it, start, end, invalidateRect.centerX() - leftRightPaddingOffset / 2f, invalidateRect.centerY() + drawTextYOffset, textPaint)
                canvas.restore()
            }
        }


        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed() {
            if (action.isNotEmpty()) {
                pressed = true
                if (pressedScale != 1f)
                    playScaleAnimator(1f, pressedScale)
                else
                    target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
            }
        }

        override fun onUnPressed(isClick: Boolean) {
            if (action.isNotEmpty()) {
                pressed = false
                if (pressedScale != 1f)
                    playScaleAnimator(pressedScale, 1f)
                else
                    target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                listener.invoke(this, action)
            }
        }

        override fun onInvalid() {
            backgroundDrawable?.clear()
            backgroundDrawable = null
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


    private class ASpan(private val action: String, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean) : ClickableSpan(), ActionSpan {

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
            ds.isFakeBoldText = isFakeBoldText
            ds.isUnderlineText = isUnderlineText
            ds.flags = if (isLineThrough) ds.flags or Paint.STRIKE_THRU_TEXT_FLAG else ds.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        override fun onClick(widget: View) {
            if (action.isNotEmpty())
                listener.invoke(this, action)
        }

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed() = Unit

        override fun onUnPressed(isClick: Boolean) = Unit

        override fun onInvalid() = Unit
    }

}