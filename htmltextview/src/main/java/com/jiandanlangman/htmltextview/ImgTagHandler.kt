package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.view.View
import androidx.core.animation.doOnEnd

class ImgTagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, style), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, private val style: Style) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, Drawable.Callback, View.OnAttachStateChangeListener {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val srcType = attrs[Attribute.SRC_TYPE.value] ?: Attribute.SrcType.IMAGE_PNG.value
        private val density = target.resources.displayMetrics.density
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val width = style.width * density
        private val height = style.height * density
        private val paddingLeft = if (style.padding.left < 0) 0f else style.padding.left * density
        private val paddingRight = if (style.padding.right < 0) 0f else style.padding.right * density
        private val paddingTop = if (style.padding.top < 0) 0f else style.padding.top * density
        private val paddingBottom = if (style.padding.bottom < 0) 0f else style.padding.bottom * density
        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * target.textSize

        private val invalidateRect = Rect()

        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f

        private var scaleAnimator: ValueAnimator? = null

        private var drawable: Drawable? = null

        init {
            HTMLTagHandler.getImageGetter()?.let {
                it.getImageDrawable(src, srcType) { d ->
                    drawable = d
                    if (d != null) {
                        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                        scaleX = (width - paddingLeft - paddingRight) / d.intrinsicWidth
                        scaleY = (height - paddingTop - paddingBottom) / d.intrinsicHeight
                        d.callback = this
                    }
                }
            }
        }

        override fun getDrawable() = drawable

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            invalidateRect.left = x.toInt()
            invalidateRect.right = (x + width).toInt()
            invalidateRect.top = top
            invalidateRect.bottom = (top + height).toInt()
            if (drawable == null)
                return
            canvas.save()
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + width / 2, y.toFloat() - height / 2)
            canvas.translate(paddingLeft, paddingTop)
            canvas.translate(x, (target.textSize - height) / 2 + (bottom - y) / 4f * 3f)
            canvas.translate(0f, -drawAlignCenterOffsetY)
            canvas.scale(scaleX, scaleY)
            drawable!!.draw(canvas)
            canvas.restore()
            canvas.save()

        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            return width.toInt()
        }

        override fun getAction() = action

        override fun onPressed() {
            if (action.isNotEmpty())
                when (style.pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(1f, .88f)
                    Style.Pressed.NONE -> Unit
                }
        }

        override fun onUnPressed() {
            if (action.isNotEmpty())
                when (style.pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(.88f, 1f)
                    Style.Pressed.NONE -> Unit
                }
        }


        override fun invalidateDrawable(who: Drawable) {
            if (target.isShown)
                target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            target.scheduleDrawable(who, what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            target.unscheduleDrawable(who, what)
        }

        override fun onViewAttachedToWindow(v: View?) {
            drawable?.callback = this
        }

        override fun onViewDetachedFromWindow(v: View?) {
            drawable?.callback = null
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
}