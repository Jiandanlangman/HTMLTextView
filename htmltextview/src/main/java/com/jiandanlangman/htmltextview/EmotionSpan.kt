package com.jiandanlangman.htmltextview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan

internal class EmotionSpan(private val target: HTMLTextView, private val provider: ResourcesProvider, private val emotion: String) : DynamicDrawableSpan(ALIGN_BASELINE), TargetInvalidWatcher, Drawable.Callback {

    private val invalidateRect = Rect()
    private val size = (target.textSize + .5f).toInt()

    private var invalid = false

    private var emotionDrawable: Drawable? = null

    override fun getDrawable() = null

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = size

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (emotionDrawable == null)
            return
        val totalHeight = emotionDrawable!!.bounds.height()
        val totalWidth = emotionDrawable!!.bounds.width()

        invalidateRect.left = x.toInt()
        invalidateRect.top = top
        invalidateRect.right = invalidateRect.left + totalWidth
        invalidateRect.bottom = (top + Util.getCurrentLineHeight(target, top, bottom))

        invalidateRect.top = (invalidateRect.top + (invalidateRect.height() - totalHeight) / 2f + .5f).toInt()
        invalidateRect.bottom = invalidateRect.top + totalHeight

        canvas.save()
        canvas.translate(invalidateRect.left.toFloat(), invalidateRect.top.toFloat())
        emotionDrawable!!.setBounds(0, 0, size, size)
        emotionDrawable!!.draw(canvas)
        canvas.restore()
    }

    override fun invalidateDrawable(who: Drawable) {
        if (target.isShown)
            target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = target.scheduleDrawable(who, what, `when`)

    override fun unscheduleDrawable(who: Drawable, what: Runnable) = target.unscheduleDrawable(who, what)

    override fun onValid() {
        invalid = false
        provider.getEmotionDrawable(emotion) {
            if (invalid)
                return@getEmotionDrawable
            emotionDrawable = it
            setCallback()
        }
    }

    override fun onInvalid() {
        invalid = true
        removeCallbackAndRecycleRes()
    }


    private fun setCallback() {
        emotionDrawable?.let {
            it.callback = this
            Util.tryCatchInvoke { it::class.java.getMethod("start").invoke(it) }
            target.invalidate()
        }
    }

    private fun removeCallbackAndRecycleRes() {
        emotionDrawable?.let {
            it.callback = null
            Util.tryCatchInvoke { it::class.java.getMethod("stop").invoke(it) }
        }
        emotionDrawable = null
    }


}
