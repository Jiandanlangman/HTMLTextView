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
import java.lang.ref.WeakReference

class ImgTagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, style, background), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, private val style: Style, background: Background) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, Drawable.Callback {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val srcType = attrs[Attribute.SRC_TYPE.value] ?: Attribute.SrcType.IMAGE_PNG.value
        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * target.textSize
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val padding = style.padding
        private val margin = style.margin
        private val invalidateRect = Rect()

        private var width = style.width
        private var height = style.height
        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f
        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }
        private var targetAttachState = 0

        private var scaleAnimator: ValueAnimator? = null

        private var drawable: WeakReference<Drawable>? = null

        private var backgroundDrawable: WeakReference<Drawable>? = null

        init {
            HTMLTagHandler.getImageGetter()?.let {
                if (target.isAttachedToWindow)
                    targetAttachState = 1
                target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View?) {
                        targetAttachState = 1
                        setCallback()
                    }

                    override fun onViewDetachedFromWindow(v: View?) {
                        targetAttachState = 2
                        target.removeOnAttachStateChangeListener(this)
                    }

                })
                it.getImageDrawable(src, srcType) { d ->
                    if (targetAttachState == 2)
                        return@getImageDrawable
                    drawable = WeakReference(d)
                    d?.apply {
                        if (width == 0)
                            width = intrinsicWidth
                        if (height == 0)
                            height = intrinsicHeight
                        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                        scaleX = width / intrinsicWidth.toFloat()
                        scaleY = height / intrinsicHeight.toFloat()
                        if (targetAttachState == 1)
                            setCallback()
                    }
                }
            }
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

        override fun getDrawable() = null

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            val totalHeight = padding.top + padding.bottom + height
            val baseHeight = bottom - top
            val totalWidth = padding.left + padding.right + width
            invalidateRect.left = x.toInt()
            invalidateRect.right = invalidateRect.left + totalWidth
            invalidateRect.top = top + (baseHeight - totalHeight) / 2
            invalidateRect.bottom = invalidateRect.top + totalHeight
            if (top / target.lineHeight != target.lineCount - 1) {
                invalidateRect.top = (invalidateRect.top - drawAlignCenterOffsetY / 2 + .5f).toInt()
                invalidateRect.bottom = (invalidateRect.bottom - drawAlignCenterOffsetY / 2 + .5f).toInt()
            }
            if (drawable == null && backgroundDrawable == null)
                return
            canvas.save()
            canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom) / 2f)
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + width / 2f, y.toFloat() - height / 2f)
            backgroundDrawable?.get()?.let {
                it.setBounds(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                it.draw(canvas)
            }
            drawable?.get()?.let {
                val l = invalidateRect.left + padding.left
                val t = invalidateRect.top + padding.top
                it.setBounds(l, t, l + width, t + height)
                it.draw(canvas)
            }
            canvas.restore()
        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = width + padding.left + padding.right + margin.left + margin.right

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed() {
            if (action.isNotEmpty())
                when (style.pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(1f, .88f)
                    Style.Pressed.NONE -> Unit
                }
        }

        override fun onUnPressed(isClick: Boolean) {
            if (action.isNotEmpty()) {
                when (style.pressed) {
                    Style.Pressed.SCALE -> playScaleAnimator(.88f, 1f)
                    Style.Pressed.NONE -> Unit
                }
                listener.invoke(this, action)
            }
        }

        override fun onInvalid() = removeCallbackAndRecycleRes()


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


        private fun setCallback() {
            drawable?.get()?.let {
                it.callback = this
                Util.tryCatchInvoke { it::class.java.getMethod("start").invoke(it) }
            }
            target.invalidate()
        }

        private fun removeCallbackAndRecycleRes() {
            drawable?.get()?.let {
                it.callback = null
                val clazz = it::class.java
                Util.tryCatchInvoke { clazz.getMethod("stop").invoke(it) }
            }
            drawable?.clear()
            drawable = null
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
}