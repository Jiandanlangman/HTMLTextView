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

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, style, background), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, private val style: Style, background: Background) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, Drawable.Callback, View.OnAttachStateChangeListener {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val srcType = attrs[Attribute.SRC_TYPE.value] ?: Attribute.SrcType.IMAGE_PNG.value
        private val density = target.resources.displayMetrics.density
        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * target.textSize
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val padding = Rect(
            if (style.padding.left < 0) 0 else Util.dpToPx(style.padding.left, density),
            if (style.padding.top < 0) 0 else Util.dpToPx(style.padding.top, density),
            if (style.padding.right < 0) 0 else Util.dpToPx(style.padding.right, density),
            if (style.padding.bottom < 0) 0 else Util.dpToPx(style.padding.bottom, density)
        )
        private val margin = Rect(
            if (style.margin.left < 0) 0 else Util.dpToPx(style.margin.left, density),
            if (style.margin.top < 0) 0 else Util.dpToPx(style.margin.top, density),
            if (style.margin.right < 0) 0 else Util.dpToPx(style.margin.right, density),
            if (style.margin.bottom < 0) 0 else Util.dpToPx(style.margin.bottom, density)
        )

        private val invalidateRect = Rect()

        private var width = Util.dpToPx(style.width, density)
        private var height = Util.dpToPx(style.height, density)
        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f

        private var scaleAnimator: ValueAnimator? = null

        private var drawable: Drawable? = null
        private var backgroundDrawable: Drawable? = null

        init {
            HTMLTagHandler.getImageGetter()?.let {
                it.getImageDrawable(src, srcType) { d ->
                    drawable = d
                    if (d != null) {
                        if (width == 0)
                            width = d.intrinsicWidth
                        if (height == 0)
                            height = d.intrinsicHeight
                        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                        scaleX = width / d.intrinsicWidth.toFloat()
                        scaleY = height / d.intrinsicHeight.toFloat()
                        d.callback = this
                    }
                    target.invalidate()
                }
            }
            background.getDrawable(target) {
                backgroundDrawable = it
                it?.let { target.invalidate() }
            }
        }

        override fun getDrawable() = drawable

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            val totalHeight = padding.top + padding.bottom + height
            val baseHeight = bottom - top
            val totalWidth = padding.left + padding.right + width
            invalidateRect.left = x.toInt()
            invalidateRect.right = invalidateRect.left + totalWidth
            invalidateRect.top = top + (baseHeight - totalHeight) / 2
            invalidateRect.bottom = invalidateRect.top +totalHeight
            if (target.lineCount > 1) {//TODO，判断是否是最后一行
                invalidateRect.top = (invalidateRect.top - drawAlignCenterOffsetY / 2 + .5f).toInt()
                invalidateRect.bottom = (invalidateRect.bottom - drawAlignCenterOffsetY / 2 + .5f).toInt()
            }
            if (drawable == null && backgroundDrawable == null)
                return
            canvas.save()
            canvas.translate(margin.left.toFloat(), (margin.top - margin.bottom) / 2f)
            if (canvasScale != 1f)
                canvas.scale(canvasScale, canvasScale, x + width / 2f, y.toFloat() - height / 2f)
            backgroundDrawable?.let {
                it.setBounds(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                it.draw(canvas)
            }
            drawable?.let {
                val l = invalidateRect.left + padding.left
                val t = invalidateRect.top + padding.top
                it.setBounds(l, t, l + width, t + height)
                it.draw(canvas)
            }
            canvas.restore()
        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = width + padding.left + padding.right + margin.left + margin.right

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