package com.jiandanlangman.htmltextview

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toRectF

class ImgTagHandler : TagHandler {

    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, style, background), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, private val style: Style, background: Background) : DynamicDrawableSpan(ALIGN_BOTTOM), ActionSpan, TargetInvalidWatcher, Drawable.Callback {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val srcType = attrs[Attribute.SRC_TYPE.value] ?: Attribute.SrcType.IMAGE_PNG.value
        private val drawAlignCenterOffsetY = (target.lineSpacingMultiplier - 1) * target.textSize
        private val src = attrs[Attribute.SRC.value] ?: ""
        private val padding = style.padding
        private val margin = style.margin
        private val invalidateRect = Rect()
        private val pressedTintColor = Util.tryCatchInvoke({ Color.parseColor(style.pressedTint) }, Color.TRANSPARENT)
        private val xferMode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)

        private var width = style.width
        private var height = style.height
        private var scaleX = 1f
        private var scaleY = 1f
        private var canvasScale = 1f
        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }
        private var targetAttachState = 0
        private var pressed = false

        private var scaleAnimator: ValueAnimator? = null

        private var drawable: Drawable? = null

        private var backgroundDrawable: Drawable? = null

        init {
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
            HTMLTagHandler.getImageGetter()?.let {
                if (target.isAttachedToWindow)
                    targetAttachState = 1
                target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        targetAttachState = 1
                        setCallback()
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        targetAttachState = 2
                        target.removeOnAttachStateChangeListener(this)
                    }

                })
                it.getImageDrawable(src, srcType) { d ->
                    if (targetAttachState == 2)
                        return@getImageDrawable
                    d?.apply {
                        if (width == 0)
                            width = intrinsicWidth
                        if (height == 0)
                            height = intrinsicHeight
                        scaleX = width / intrinsicWidth.toFloat()
                        scaleY = height / intrinsicHeight.toFloat()
                        drawable = this
                        if (targetAttachState == 1)
                            setCallback()
                    }
                }
            }
            background.getDrawable {
                if (targetAttachState == 2)
                    return@getDrawable
                it?.apply {
                    backgroundDrawable = this
                    if (targetAttachState == 1)
                        target.invalidate()
                }
            }
        }

        override fun getDrawable() = null

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            if (drawable == null && backgroundDrawable == null)
                return
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
            canvas.save()
            val saveCount = if (pressed && pressedTintColor != Color.TRANSPARENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    canvas.saveLayer(invalidateRect.toRectF(), paint)
                else
                    canvas.saveLayer(invalidateRect.toRectF(), paint, Canvas.ALL_SAVE_FLAG)
            } else
                0
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
            if (pressed && pressedTintColor != Color.TRANSPARENT) {
                val prevColor = paint.color
                paint.color = pressedTintColor
                paint.xfermode = xferMode
                canvas.drawRect(invalidateRect, paint)
                canvas.restoreToCount(saveCount)
                paint.xfermode = null
                paint.color = prevColor

            }
            canvas.restore()
        }


        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = width + padding.left + padding.right + margin.left + margin.right

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed() {
            if (action.isNotEmpty()) {
                pressed = true
                if (style.pressedScale != 1f)
                    playScaleAnimator(1f, style.pressedScale)
                else
                    target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
            }
        }

        override fun onUnPressed(isClick: Boolean) {
            if (action.isNotEmpty()) {
                pressed = false
                if (style.pressedScale != 1f)
                    playScaleAnimator(style.pressedScale, 1f)
                else
                    target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
                listener.invoke(this, action)
            }
        }

        override fun onInvalid() = removeCallbackAndRecycleRes()


        override fun invalidateDrawable(who: Drawable) {
            if (target.isShown)
                target.postInvalidate(invalidateRect.left, invalidateRect.top, invalidateRect.right, invalidateRect.bottom)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = target.scheduleDrawable(who, what, `when`)

        override fun unscheduleDrawable(who: Drawable, what: Runnable) =  target.unscheduleDrawable(who, what)


        private fun setCallback() {
            drawable?.let {
                it.callback = this
                Util.tryCatchInvoke { it::class.java.getMethod("start").invoke(it) }
            }
            target.invalidate()
        }

        private fun removeCallbackAndRecycleRes() {
            drawable?.let {
                it.callback = null
                Util.tryCatchInvoke {  it::class.java.getMethod("stop").invoke(it) }
            }
            drawable = null
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