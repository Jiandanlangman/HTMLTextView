package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toRectF
import androidx.core.text.toSpannable


internal class ATagHandler : TagHandler {

    @SuppressLint("Range")
    override fun handleTag(tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        if (attrs.isEmpty() && style.isEmpty && background.isNotBackground())
            return
        val action = attrs[Attribute.ACTION.value] ?: ""
        val pressedTintColor = Util.tryCatchInvoke({ Color.parseColor(style.pressedTint) }, Color.TRANSPARENT)
        val span = if (style.fontSize <= 0 && style.padding.left < 0 && style.padding.top < 0 && style.padding.right < 0 && style.padding.bottom < 0 && style.margin.left < 0 && style.margin.top < 0 && style.margin.right < 0 && style.margin.bottom < 0 && style.pressedScale == 1f && pressedTintColor == Color.TRANSPARENT && background.isNotBackground() && style.width <= 0 && style.height <= 0 && !style.textDecoration.contains(Style.TextDecoration.STROKE) && style.lineHeight < 0)
            ASpan(action, style)
        else
            FontSizeASpan(action, style, background)
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (style.lineHeight >= 0)
            output.append("\n")
    }

    override fun isSingleTag() = false

    private class FontSizeASpan(private val action: String, private val style: Style, private val background: Background) : ReplacementSpan(), ActionSpan {

        private val drawRect = Rect()
        private val drawTextRect = Rect()
        private val padding = Util.toNaturalRect(style.padding)
        private val margin = Util.toNaturalRect(style.margin)
        private val pressedTintColor = Util.tryCatchInvoke({ Color.parseColor(style.pressedTint) }, Color.TRANSPARENT)
        private val strokeColor = Util.tryCatchInvoke({ Color.parseColor(style.stroke) }, Color.TRANSPARENT)
        private val paintTextAlign: Paint.Align = when {
            style.textAlign.contains(Style.TextAlign.CENTER) || style.textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL) -> Paint.Align.CENTER
            style.textAlign.contains(Style.TextAlign.LEFT) && style.textAlign.contains(Style.TextAlign.RIGHT) -> Paint.Align.CENTER
            style.textAlign.contains(Style.TextAlign.LEFT) -> Paint.Align.LEFT
            style.textAlign.contains(Style.TextAlign.RIGHT) -> Paint.Align.RIGHT
            else -> Paint.Align.CENTER
        }
        private val isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
        private val isThrough = style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)
        private val stroke = style.textDecoration.contains(Style.TextDecoration.STROKE)
        private val xferMode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)

        private var color = Color.TRANSPARENT
        private var isFakeBoldText = false

        private var canvasScale = 1f
        private var pressed = false

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }

        private var target: HTMLTextView? = null
        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null
        private var backgroundDrawable: Drawable? = null


        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    style.typeface?.apply { it.typeface = this }
                    it.textAlign = paintTextAlign
                    it.isUnderlineText = isUnderlineText
                    it.flags = if (isThrough) it.flags or Paint.STRIKE_THRU_TEXT_FLAG else it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            if (style.fontSize > 0)
                paint!!.textSize = style.fontSize.toFloat()
            paint!!.isFakeBoldText = isFakeBoldText
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            if (style.width > 0)
                return style.width + margin.left + margin.right
            val width = text?.let { (getTextPaint(paint).measureText(text, start, end) + .5f).toInt() } ?: 0
            return width + padding.left + padding.right + margin.left + margin.right
        }


        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            if (target == null || text == null)
                return
            val textPaint = getTextPaint(paint)
            val currentLineHeight = Util.getCurrentLineHeight(target!!, top, bottom)
            val verticalCenterLine = top + currentLineHeight / 2f
            val rectHeight = if (style.height > 0) style.height.toFloat() else (textPaint.textSize + padding.top + padding.bottom)
            val rectWidth = if (style.width > 0) style.width else ((textPaint.measureText(text, start, end) + .5f).toInt() + padding.left + padding.right)
            drawRect.left = x.toInt()
            drawRect.right = drawRect.left + rectWidth
            drawRect.top = (verticalCenterLine - rectHeight / 2f + .5f).toInt()
            drawRect.bottom = (drawRect.top + rectHeight + .5f).toInt()

            val verticalAlignOffset = when {
                style.textAlign.contains(Style.TextAlign.CENTER_VERTICAL) || style.textAlign.contains(Style.TextAlign.CENTER) -> 0
                style.textAlign.contains(Style.TextAlign.TOP) && style.textAlign.contains(Style.TextAlign.BOTTOM) -> 0
                style.textAlign.contains(Style.TextAlign.TOP) -> -((currentLineHeight - rectHeight) / 4).toInt()
                style.textAlign.contains(Style.TextAlign.BOTTOM) -> ((currentLineHeight - rectHeight) / 4).toInt()
                else -> 0
            }
            drawRect.top += verticalAlignOffset
            drawRect.bottom += verticalAlignOffset

            canvas.save()
            val saveCount = if (pressed && pressedTintColor != Color.TRANSPARENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    canvas.saveLayer(drawRect.toRectF(), textPaint)
                else
                    canvas.saveLayer(drawRect.toRectF(), textPaint, Canvas.ALL_SAVE_FLAG)
            } else
                0
            canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom).toFloat())
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + drawRect.width() / 2f, y.toFloat() - drawRect.height() / 2f)
            backgroundDrawable?.let { d ->
                d.setBounds(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
                d.draw(canvas)
            }
            drawTextRect.left = drawRect.left + padding.left
            drawTextRect.right = drawRect.right - padding.right
            drawTextRect.top = drawRect.top + padding.top
            drawTextRect.bottom = drawRect.bottom - padding.bottom
            val drawTextX = when (textPaint.textAlign) {
                Paint.Align.LEFT -> drawTextRect.left.toFloat()
                Paint.Align.RIGHT -> drawTextRect.right.toFloat()
                else -> drawTextRect.centerX().toFloat()
            }
            val fontMetrics = textPaint.fontMetrics
            val drawTextYOffset = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            if (stroke) {
                textPaint.style = Paint.Style.STROKE
                textPaint.strokeWidth = style.strokeWidth
                textPaint.color = strokeColor
                canvas.drawText(text, start, end, drawTextX, drawTextRect.centerY().toFloat() + drawTextYOffset, textPaint)
            }
            textPaint.style = Paint.Style.FILL
            textPaint.color = color
            canvas.drawText(text, start, end, drawTextX, drawTextRect.centerY().toFloat() + drawTextYOffset, textPaint)
            if (pressed && pressedTintColor != Color.TRANSPARENT) {
                val prevColor = textPaint.color
                textPaint.color = pressedTintColor
                textPaint.xfermode = xferMode
                canvas.drawRect(drawRect, textPaint)
                canvas.restoreToCount(saveCount)
                textPaint.xfermode = null
                textPaint.color = prevColor
            }
            canvas.restore()
            canvas.translate(0f, getVerticalOffset().toFloat())
        }


        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed(x: Float, y: Float): Boolean {
            if (action.isNotEmpty() && drawRect.contains(x.toInt(), y.toInt())) {
                pressed = true
                if (style.pressedScale != 1f)
                    playScaleAnimator(1f, style.pressedScale)
                else
                    target?.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
                return true
            }
            return false
        }

        override fun onUnPressed(x: Float, y: Float, cancel: Boolean) {
            pressed = false
            if (canvasScale != 1f)
                playScaleAnimator(canvasScale, 1f)
            else
                target?.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
            if (action.isNotEmpty() && drawRect.contains(x.toInt(), y.toInt()))
                listener.invoke(this, action)
        }


        override fun getAction() = action

        @SuppressLint("Range")
        override fun onValid(target: HTMLTextView) {
            this.target = target
            color = Util.tryCatchInvoke({ Color.parseColor(style.color) }, target.textColors.defaultColor)
            isFakeBoldText = when (style.fontWeight) {
                Style.FontWeight.NORMAL -> false
                Style.FontWeight.BOLD -> true
                else -> target.paint.isFakeBoldText
            }
            target.requestLayout()
            background.getDrawable(target) {
                if (this.target == null)
                    return@getDrawable
                it?.apply {
                    backgroundDrawable = this
                    target.invalidate()
                }
            }

        }


        override fun onInvalid() {
            target = null
            backgroundDrawable = null
        }

        override fun getVerticalOffset(): Int {
            if (style.lineHeight >= 0)
               return (target!!.lineHeight * (style.lineHeight - 1) + .5f).toInt()
            return 0
        }

        private fun playScaleAnimator(from: Float, to: Float) {
            scaleAnimator?.end()
            scaleAnimator = ValueAnimator.ofFloat(from, to)
            scaleAnimator!!.let {
                it.duration = 64
                it.addUpdateListener { _ ->
                    canvasScale = it.animatedValue as Float
                    if (target != null)
                        target!!.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
                    else {
                        it.cancel()
                        canvasScale = 1f
                        scaleAnimator = null
                    }
                }
                it.doOnEnd { scaleAnimator = null }
                it.start()
            }
        }

    }


    private class ASpan(private val action: String, val style: Style) : ClickableSpan(), ActionSpan {

        private val isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
        private val isLineThrough = style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)

        private var color = Color.TRANSPARENT
        private var isFakeBoldText = false

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }

        private var target: HTMLTextView? = null


        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            style.typeface?.apply { ds.typeface = this }
            ds.color = color
            ds.isFakeBoldText = isFakeBoldText
            ds.isUnderlineText = isUnderlineText
            ds.flags = if (isLineThrough) ds.flags or Paint.STRIKE_THRU_TEXT_FLAG else ds.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        override fun onClick(widget: View) = Unit

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }


        override fun getAction() = action

        @SuppressLint("Range")
        override fun onValid(target: HTMLTextView) {
            this.target = target
            color = Util.tryCatchInvoke({ Color.parseColor(style.color) }, target.textColors.defaultColor)
            isFakeBoldText = when (style.fontWeight) {
                Style.FontWeight.NORMAL -> false
                Style.FontWeight.BOLD -> true
                else -> target.paint.isFakeBoldText
            }
        }

        override fun onInvalid() {
            target = null
        }

        override fun onPressed(x: Float, y: Float): Boolean {
            target?.let {
                return action.isNotEmpty() && Util.getEventSpan(it, it.text.toSpannable(), x, y, ASpan::class.java)?.contains(this) ?: false
            }
            return false
        }

        override fun onUnPressed(x: Float, y: Float, cancel: Boolean) {
            if (action.isNotEmpty() && !cancel)
                listener.invoke(this, action)
        }

        override fun getVerticalOffset() = 0


    }

}