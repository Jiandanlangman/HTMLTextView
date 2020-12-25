package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.util.Log
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toRectF

internal class ImgTagHandler : TagHandler {

    override fun handleTag(tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        output.append("\u200B")
        output.setSpan(ImgSpan(attrs, style, background), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun isSingleTag() = true

    private class ImgSpan(attrs: Map<String, String>, private val style: Style, private val background: Background) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, Drawable.Callback {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val padding = Util.toNaturalRect(style.padding)
        private val margin = Util.toNaturalRect(style.margin)
        private val textAlign = style.textAlign
        private val drawRect = Rect()
        private val drawImageRect = Rect()
        private val pressedTintColor = Util.tryCatchInvoke({ Color.parseColor(style.pressedTint) }, Color.TRANSPARENT)
        private val xferMode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        private val width = style.width
        private val height = style.height

        private var imageWidth = 0
        private var imageHeight = 0
        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f
        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }
        private var pressed = false

        private var target: HTMLTextView? = null
        private var scaleAnimator: ValueAnimator? = null
        private var drawable: Drawable? = null
        private var backgroundDrawable: Drawable? = null


        override fun getDrawable() = null

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            if (target == null || drawable == null && backgroundDrawable == null)
                return
            val currentLineHeight = Util.getCurrentLineHeight(target!!, top, bottom)
            val verticalCenterLine = top + currentLineHeight / 2f
            val rectHeight = if (height > 0) height else (imageHeight + padding.top + padding.bottom)
            val rectWidth = if (width > 0) width else (imageWidth + padding.left + padding.right)
            drawRect.left = x.toInt()
            drawRect.right = drawRect.left + rectWidth
            drawRect.top = (verticalCenterLine - rectHeight / 2f + .5f).toInt()
            drawRect.bottom = (drawRect.top + rectHeight + .5f).toInt()

            val verticalAlignOffset = when {
                textAlign.contains(Style.TextAlign.CENTER_VERTICAL) || textAlign.contains(Style.TextAlign.CENTER) -> 0
                textAlign.contains(Style.TextAlign.TOP) && textAlign.contains(Style.TextAlign.BOTTOM) -> 0
                textAlign.contains(Style.TextAlign.TOP) -> -((currentLineHeight - rectHeight) / 2)
                textAlign.contains(Style.TextAlign.BOTTOM) -> ((currentLineHeight - rectHeight) / 2)
                else -> 0
            }
            drawRect.top += verticalAlignOffset
            drawRect.bottom += verticalAlignOffset

            canvas.save()
            val saveCount = if (pressed && pressedTintColor != Color.TRANSPARENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    canvas.saveLayer(drawRect.toRectF(), paint)
                else
                    canvas.saveLayer(drawRect.toRectF(), paint, Canvas.ALL_SAVE_FLAG)
            } else
                0
            val verticalOffset = getVerticalOffset()
            if (verticalOffset != 0)
                canvas.translate(margin.left.toFloat(), margin.top.toFloat())
            else
                canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom) / 1f)
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + rectWidth / 2f, drawRect.top + rectHeight / 2f)
            backgroundDrawable?.let {
                it.setBounds(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
                it.draw(canvas)
            }
            drawable?.let {
                drawImageRect.left = drawRect.left + (drawRect.width() - imageWidth) / 2
                drawImageRect.right = drawImageRect.left + imageWidth
                drawImageRect.top = drawRect.top + (drawRect.height() - imageHeight) / 2
                drawImageRect.bottom = drawImageRect.top + imageHeight
                it.setBounds(drawImageRect.left, drawImageRect.top, drawImageRect.right, drawImageRect.bottom)
                it.draw(canvas)
            }
            if (pressed && pressedTintColor != Color.TRANSPARENT) {
                val prevColor = paint.color
                paint.color = pressedTintColor
                paint.xfermode = xferMode
                canvas.drawRect(drawRect, paint)
                canvas.restoreToCount(saveCount)
                paint.xfermode = null
                paint.color = prevColor

            }
            canvas.restore()
            canvas.translate(0f, verticalOffset.toFloat())
        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = (if (width > 0) width else (imageWidth + padding.left + padding.right)) + margin.left + margin.right

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

        override fun getVerticalOffset() :Int {
            val h =  if (height > 0) height else (imageHeight + padding.top + padding.bottom)
                if(style.lineHeight >= 0) {
                    return ((h - target!!.lineHeight) * style.lineHeight + .5f).toInt()  + margin.top + margin.bottom
                } else if(style.spanLine > 0)
                    return (h - target!!.lineHeight)  + margin.top + margin.bottom
            val targetWidth = target?.let { if(it.width > 0) it.width else if(it.measuredWidth > 0) it.measuredWidth else it.layoutParams?.width ?: -1 } ?: -1
            if(targetWidth > 0) {
                val w = if (width > 0) width else (imageWidth + padding.left + padding.right)
                    if(w >= targetWidth)
                        return (h - target!!.lineHeight)  + margin.top + margin.bottom
            }
            return 0
        }

        override fun getAction() = action

        override fun onValid(target: HTMLTextView) {
            this.target = target
            loadImage()
            loadBackground()
        }

        override fun onInvalid() {
            target = null
            removeCallbackAndRecycleRes()
        }


        override fun invalidateDrawable(who: Drawable) {
            if (target?.isShown == true)
                target?.postInvalidate(drawRect.left, drawRect.top, drawRect.right, drawRect.bottom)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            target?.scheduleDrawable(who, what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            target?.unscheduleDrawable(who, what)
        }

        private fun loadImage() = HTMLTagHandler.getResourcesProvider()?.let {
            it.getImageDrawable(target!!, src) { d ->
                if (target == null)
                    return@getImageDrawable
                d?.apply {
                    val tw = if (width < 0) 0 else width
                    val th = if (height < 0) 0 else height
                    when (tw + th) {
                        0 -> { //没有指定宽高
                            imageWidth = intrinsicWidth
                            imageHeight = intrinsicHeight
                            scaleX = 1f
                            scaleY = 1f
                        }
                        tw -> { //指定了宽度
                            imageWidth = width - padding.left - padding.right
                            imageHeight = (imageWidth * intrinsicHeight / intrinsicWidth.toFloat() + .5f).toInt()
                            scaleX = intrinsicWidth / imageWidth.toFloat()
                            scaleY = intrinsicHeight / imageHeight.toFloat()
                        }
                        th -> { //指定了高度
                            imageHeight = height - padding.top - padding.bottom
                            imageWidth = (imageHeight * intrinsicWidth / intrinsicHeight.toFloat() + .5f).toInt()
                            scaleX = intrinsicWidth / imageWidth.toFloat()
                            scaleY = intrinsicHeight / imageHeight.toFloat()
                        }
                        else -> { //都指定了
                            if (width < height) {
                                imageWidth = width - padding.left - padding.right
                                imageHeight = (imageWidth * intrinsicHeight / intrinsicWidth.toFloat() + .5f).toInt()
                                scaleX = intrinsicWidth / imageWidth.toFloat()
                                scaleY = intrinsicHeight / imageHeight.toFloat()
                            } else {
                                imageHeight = height - padding.top - padding.bottom
                                imageWidth = (imageHeight * intrinsicWidth / intrinsicHeight.toFloat() + .5f).toInt()
                                scaleX = intrinsicWidth / imageWidth.toFloat()
                                scaleY = intrinsicHeight / imageHeight.toFloat()
                            }
                        }
                    }
                    drawable = this
                    setCallback()
                }
            }
        }

        private fun loadBackground() = background.getDrawable(target!!) {
            if (target == null)
                return@getDrawable
            it?.apply {
                backgroundDrawable = this
                target!!.invalidate()
            }
        }

        private fun setCallback() {
            drawable?.let {
                it.callback = this
                Util.tryCatchInvoke { it::class.java.getMethod("start").invoke(it) }
            }
            target!!.invalidate()
        }

        private fun removeCallbackAndRecycleRes() {
            drawable?.let {
                it.callback = null
                Util.tryCatchInvoke { it::class.java.getMethod("stop").invoke(it) }
            }
            drawable = null
            backgroundDrawable = null
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
                        scaleAnimator = null
                        canvasScale = to
                    }
                }
                it.doOnEnd { scaleAnimator = null }
                it.start()
            }
        }


    }
}