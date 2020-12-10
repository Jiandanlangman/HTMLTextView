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


internal class ATagHandler : TagHandler {

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
        val width = style.width
        val height = style.height
        val span = if (textSize == target.textSize && style.padding.left < 0 && style.padding.top < 0 && style.padding.right < 0 && style.padding.bottom < 0 && style.margin.left < 0 && style.margin.top < 0 && style.margin.right < 0 && style.margin.bottom < 0 && pressedScale == 1f && pressedTintColor == Color.TRANSPARENT && background.isNotBackground() && width <= 0 && height <= 0)
            ASpan(action, color, isFakeBoldText, isUnderlineText, isLineThrough)
        else
            FontSizeASpan(target, action, width, height, textSize, color, isFakeBoldText, isUnderlineText, isLineThrough, style.padding, style.margin, pressedScale, pressedTintColor, style.textAlign, background)
        output.setSpan(span, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun isSingleTag() = false

    private class FontSizeASpan(private val target: HTMLTextView, private val action: String, private val width: Int, private val height: Int, private val textSize: Float, private val color: Int, private val isFakeBoldText: Boolean, private val isUnderlineText: Boolean, private val isLineThrough: Boolean, private val padding: Rect, private val margin: Rect, private val pressedScale: Float, private val pressedTintColor: Int, private val textAlign: Array<Style.TextAlign>, background: Background) : ReplacementSpan(), ActionSpan, TargetInvalidWatcher {

        private val drawRect = Rect()
        private val paintTextAlign:Paint.Align
        private var drawTextRect = Rect()
        private val xferMode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)

        private var canvasScale = 1f
        private var drawTextYOffset = 0f
        private var pressed = false

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }
        private var targetAttachState = 0
        private var invalid = false

        private var paint: TextPaint? = null
        private var scaleAnimator: ValueAnimator? = null
        private var backgroundDrawable: Drawable? = null

        init {
            paintTextAlign =  when {
                textAlign.contains(Style.TextAlign.CENTER) || textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL) -> Paint.Align.CENTER
                textAlign.contains(Style.TextAlign.LEFT) && textAlign.contains(Style.TextAlign.RIGHT) -> Paint.Align.CENTER
                textAlign.contains(Style.TextAlign.LEFT) -> Paint.Align.LEFT
                textAlign.contains(Style.TextAlign.RIGHT) -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            if (padding.left < 0)
                padding.left = 0
            if (padding.top < 0)
                padding.top = 0
            if (padding.right < 0)
                padding.right = 0
            if (padding.bottom < 0)
                padding.bottom = 0
            if (margin.left < 0)
                margin.left = 0
            if (margin.top < 0)
                margin.top = 0
            if (margin.right < 0)
                margin.right = 0
            if (margin.bottom < 0)
                margin.bottom = 0
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
            background.getDrawable(target) {
                if (invalid || targetAttachState == 2)
                    return@getDrawable
                it?.apply {
                    backgroundDrawable = this
                    if (targetAttachState == 1)
                        target.invalidate()
                }
            }
        }

        private fun getTextPaint(p: Paint): TextPaint {
            if (paint == null) {
                paint = TextPaint(p)
                paint!!.let {
                    it.textAlign = paintTextAlign
                    it.isFakeBoldText = isFakeBoldText
                    it.textSize = textSize
                    it.color = color
                    it.isUnderlineText = isUnderlineText
                    it.flags = if (isLineThrough) it.flags or Paint.STRIKE_THRU_TEXT_FLAG else it.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    val fontMetrics = it.fontMetrics
                    drawTextYOffset = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
                }
            }
            return paint!!
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            if (width > 0)
                return width + margin.left + margin.right
            val width = text?.let { (getTextPaint(paint).measureText(text, start, end) + .5f).toInt() } ?: 0
            return width + padding.left + padding.right + margin.left + margin.right
        }


        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            text?.let {
                val textPaint = getTextPaint(paint)
                val currentLineHeight = Util.getCurrentLineHeight(target, top, bottom)
                val verticalCenterLine = top + currentLineHeight / 2f
                val rectHeight = (if (height > 0) height.toFloat() else textSize + padding.top + padding.bottom)
                val rectWidth = if (width > 0) width else (textPaint.measureText(text, start, end) + .5f).toInt() + padding.left + padding.right
                drawRect.left = x.toInt()
                drawRect.right = drawRect.left + rectWidth
                drawRect.top = (verticalCenterLine - rectHeight / 2f + .5f).toInt()
                drawRect.bottom = (drawRect.top + rectHeight + .5f).toInt()

                val marginOffset = (margin.top - margin.bottom) / 2
                drawRect.top += marginOffset
                drawRect.bottom += marginOffset

                val verticalAlignOffset = when {
                    textAlign.contains(Style.TextAlign.CENTER_VERTICAL) || textAlign.contains(Style.TextAlign.CENTER) -> 0
                    textAlign.contains(Style.TextAlign.TOP) && textAlign.contains(Style.TextAlign.BOTTOM) -> 0
                    textAlign.contains(Style.TextAlign.TOP) -> -((currentLineHeight - rectHeight) / 4).toInt()
                    textAlign.contains(Style.TextAlign.BOTTOM) -> ((currentLineHeight - rectHeight) / 4).toInt()
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
                canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom) / 2f)
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
                val drawTextX = when(textPaint.textAlign) {
                    Paint.Align.LEFT -> drawTextRect.left.toFloat()
                    Paint.Align.RIGHT -> drawTextRect.right.toFloat()
                    else -> drawTextRect.centerX().toFloat()
                }
                canvas.drawText(it, start, end, drawTextX, drawTextRect.centerY().toFloat() + drawTextYOffset, textPaint)
                if (pressed && pressedTintColor != Color.TRANSPARENT) {
                    val prevColor = textPaint.color
                    textPaint.color = pressedTintColor
                    textPaint.xfermode = xferMode
                    canvas.drawRect(drawTextRect, textPaint)
                    canvas.restoreToCount(saveCount)
                    textPaint.xfermode = null
                    textPaint.color = prevColor
                }
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
                    target.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
            }
        }

        override fun onUnPressed(isClick: Boolean) {
            if (action.isNotEmpty()) {
                pressed = false
                if (pressedScale != 1f)
                    playScaleAnimator(pressedScale, 1f)
                else
                    target.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
                listener.invoke(this, action)
            }
        }

        override fun getAction() = action

        override fun onInvalid() {
            invalid = true
            backgroundDrawable = null
        }

        private fun playScaleAnimator(from: Float, to: Float) {
            scaleAnimator?.cancel()
            scaleAnimator = ValueAnimator.ofFloat(from, to)
            scaleAnimator!!.let {
                it.duration = 64
                it.addUpdateListener { _ ->
                    canvasScale = it.animatedValue as Float
                    target.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
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

        override fun getAction() = action

    }

}