package com.jiandanlangman.htmltextview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.util.Log
import android.view.View

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
        private val padding = style.padding * density
        private val paddingLeft = style.paddingLeft * density
        private val paddingRight = style.paddingRight * density
        private val paddingTop = style.paddingTop * density
        private val paddingBottom = style.paddingBottom * density
        private val bounds = Bounds()

        private var scaleX = 1f
        private var scaleY = 1f

        private var drawable: Drawable? = null

        init {
            HTMLTagHandler.getImageGetter()?.let {
                it.getImageDrawable(src) { d ->
                    drawable = d
                    if (d != null) {
                        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                        scaleX = (width - (if (paddingLeft != 0f) paddingLeft else padding) - (if (paddingRight != 0f) paddingRight else padding)) / d.intrinsicWidth
                        scaleY = (height - (if (paddingTop != 0f) paddingTop else padding) - (if (paddingBottom != 0f) paddingBottom else padding)) / d.intrinsicHeight
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

        }

        override fun onUnPressed() {

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


    }
}