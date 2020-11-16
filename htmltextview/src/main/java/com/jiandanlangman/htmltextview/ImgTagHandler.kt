package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd

class ImgTagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, style), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, private val style: Style) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, Drawable.Callback, View.OnAttachStateChangeListener {

        private val density = target.resources.displayMetrics.density
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val width = style.width * density
        private val height = style.height * density
        private val paddingLeft = if(style.paddingLeft != 0) style.paddingLeft * density else style.padding * density
        private val paddingRight = if(style.paddingRight != 0) style.paddingRight * density else style.padding * density
        private val paddingTop = if(style.paddingTop != 0) style.paddingTop * density else style.padding * density
        private val paddingBottom = if(style.paddingBottom != 0) style.paddingBottom * density else style.paddingBottom * density

        private val bounds = Bounds()

        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f

        private var scaleAnimator: ValueAnimator? = null

        private var drawable: Drawable? = null

        init {
            HTMLTagHandler.getImageGetter()?.let {
                it.getImageDrawable(src) { d ->
                    drawable = d
                    if (d != null) {
                        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                        scaleX = (width - paddingLeft - paddingRight)/ d.intrinsicWidth
                        scaleY = (height -paddingTop - paddingBottom) / d.intrinsicHeight
                        d.callback = this
                    }
                }
            }
        }

        override fun getDrawable() = drawable

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            bounds.left = x.toInt()
            bounds.right = (x + width).toInt()
            bounds.top = top
            bounds.bottom = (top + height).toInt()
            if (drawable == null)
                return
            canvas.save()
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + width / 2, y.toFloat() - height / 2)
            canvas.translate(paddingLeft, paddingTop)
            canvas.translate(x, (target.textSize - height) / 2 + (bottom - y) / 5f * 4f)
            canvas.scale(scaleX, scaleY)
            drawable!!.draw(canvas)
            canvas.restore()
            canvas.save()

        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            return width.toInt()
        }

        override fun getAction() = ""

        override fun onPressed() {
            when (style.pressed) {
                Style.Pressed.SCALE -> playScaleAnimator(1f, .88f)
                Style.Pressed.NONE -> { }
            }
        }

        override fun onUnPressed() {
            when (style.pressed) {
                Style.Pressed.SCALE -> playScaleAnimator(.88f, 1f)
                Style.Pressed.NONE -> { }
            }
        }


        override fun invalidateDrawable(who: Drawable) {
            if (target.isShown) {
                target.postInvalidate(bounds.left, bounds.top, bounds.right, bounds.bottom)
                Log.d("ImgTagHandler", "invalidateDrawable${who.state}")
            }
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
                    target.postInvalidate(bounds.left, bounds.top, bounds.right, bounds.bottom)
                }
                it.doOnEnd { scaleAnimator = null }
                it.start()
            }
        }


    }
}