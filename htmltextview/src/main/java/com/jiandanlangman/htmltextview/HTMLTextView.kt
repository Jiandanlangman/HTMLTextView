package com.jiandanlangman.htmltextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.style.DynamicDrawableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans
import java.util.*


class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        private var emotionDrawableProvider: EmotionDrawableProvider? = null

        fun registerTagHandler(tag: String, handler: TagHandler) = HTMLTagHandler.registerTagHandler(tag, handler)

        fun unRegisterTagHandler(tag: String) = HTMLTagHandler.unRegisterTagHandler(tag)

        fun setImageGetter(imageGetter: ImageGetter?) = HTMLTagHandler.setImageGetter(imageGetter)

        fun setEmotionDrawableProvider(provider: EmotionDrawableProvider?) {
            emotionDrawableProvider = provider
        }

    }

    private val onSpanClickListener: ((ActionSpan, String) -> Unit) = { _, action -> onClickListener?.invoke(this, action) }

    private var sourceText: CharSequence = ""
    private var onClickListener: ((HTMLTextView, String) -> Unit)? = null

    init {
        Util.init(context)
        movementMethod = LinkMovementMethod.getInstance()
        super.setHighlightColor(Color.TRANSPARENT)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        text = sourceText
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (text as? Spannable)?.let { it.getSpans(0, it.length, TargetInvalidWatcher::class.java)?.forEach { a -> a.onInvalid() } }
        super.setText("", BufferType.NORMAL)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        (getText() as? Spannable)?.let { it.getSpans(0, it.length, TargetInvalidWatcher::class.java)?.forEach { a -> a.onInvalid() } }
        sourceText = text ?: ""
        val spannedText = replaceEmotionToDrawable((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(sourceText.toString(), Html.FROM_HTML_MODE_LEGACY, null, HTMLTagHandler(this)) else Html.fromHtml(sourceText.toString(), null, HTMLTagHandler(this))) as Spannable)
        val spans = spannedText.getSpans<ActionSpan>(0, spannedText.length)
        spans.forEach { it.setOnClickListener(onSpanClickListener) }
        super.setText(spannedText, type)
    }

    @Deprecated("")
    override fun setOnClickListener(l: OnClickListener?) {

    }

    @Deprecated("")
    override fun setHighlightColor(color: Int) {

    }

    private val location = IntArray(2)
    private val rect = Rect()
    private val drawableRect = Rect()
    private var eventDrawable: Drawable? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP)
            return super.dispatchTouchEvent(event)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        if (action == MotionEvent.ACTION_DOWN) {
            getLocationOnScreen(location)
            rect.set(location[0], location[1], location[0] + width, location[1] + height)
            eventDrawable = null
            compoundDrawables[0]?.let {
                val bounds = it.bounds
                drawableRect.left = rect.left + paddingLeft
                drawableRect.top = rect.top + paddingTop + (rect.height() - paddingTop - paddingBottom - bounds.height()) / 2
                drawableRect.right = drawableRect.left + bounds.width()
                drawableRect.bottom = drawableRect.top + bounds.height()
                if (drawableRect.contains(x, y))
                    eventDrawable = it
            }
            if (eventDrawable == null)
                compoundDrawables[1]?.let {
                    val bounds = it.bounds
                    drawableRect.left = rect.left + paddingLeft + (rect.width() - paddingLeft - paddingRight - bounds.width()) / 2
                    drawableRect.top = rect.top + paddingTop
                    drawableRect.right = drawableRect.left + bounds.width()
                    drawableRect.bottom = drawableRect.top + bounds.height()
                    if (drawableRect.contains(x, y))
                        eventDrawable = it
                }
            if (eventDrawable == null)
                compoundDrawables[2]?.let {
                    val bounds = it.bounds
                    drawableRect.right = rect.right - paddingRight
                    drawableRect.left = drawableRect.right - bounds.width()
                    drawableRect.top = rect.top + paddingTop + (rect.height() - paddingTop - paddingBottom - bounds.height()) / 2
                    drawableRect.bottom = drawableRect.top + bounds.height()
                    if (drawableRect.contains(x, y))
                        eventDrawable = it
                }
            if (eventDrawable == null)
                compoundDrawables[3]?.let {
                    val bounds = it.bounds
                    drawableRect.left = rect.left + paddingLeft + (rect.width() - paddingLeft - paddingRight - bounds.width()) / 2
                    drawableRect.right = drawableRect.left + bounds.width()
                    drawableRect.bottom = rect.bottom - paddingBottom
                    drawableRect.top = drawableRect.bottom - bounds.height()
                    if (drawableRect.contains(x, y))
                        eventDrawable = it
                }
        } else
            if (eventDrawable != null && drawableRect.contains(x, y)) {
                val act = drawableActions[eventDrawable]
                if (!act.isNullOrEmpty())
                    onClickListener?.invoke(this, act)
            }
        if (eventDrawable != null && !drawableActions[eventDrawable].isNullOrEmpty())
            return true
        return super.dispatchTouchEvent(event)
    }


    fun setOnClickListener(onClickListener: ((v: HTMLTextView, action: String) -> Unit)?) {
        this.onClickListener = onClickListener
    }


    private val drawableActions = HashMap<Drawable, String>()

    internal fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?, leftAction: String?, topAction: String?, rightAction: String?, bottomAction: String?) {
        left?.let { drawableActions[it] = leftAction ?: "" }
        top?.let { drawableActions[it] = topAction ?: "" }
        right?.let { drawableActions[it] = rightAction ?: "" }
        bottom?.let { drawableActions[it] = bottomAction ?: "" }
        setCompoundDrawables(left, top, right, bottom)
    }

    private fun replaceEmotionToDrawable(spannable: Spannable): Spannable {
        emotionDrawableProvider?.let {
            val text = spannable.toString()
            val textLength = text.length
            var startIndex = 0
            var prevPointCount = text.codePointCount(0, textLength)
            for (i in 0 until textLength) {
                val pointCount = text.codePointCount(i, textLength)
                if (pointCount != prevPointCount) {
                    val ch = text.substring(startIndex, i)
                    if (it.isEmotionDrawable(ch))
                        spannable.setSpan(EmotionSpan(this, it, ch), startIndex, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    startIndex = i
                    prevPointCount = pointCount
                }
            }
        }
        return spannable
    }


    private class EmotionSpan(private val target: HTMLTextView, provider: EmotionDrawableProvider, emotion: String) : DynamicDrawableSpan(ALIGN_BASELINE), TargetInvalidWatcher, Drawable.Callback {

        private val invalidateRect = Rect()
        private val size = (target.textSize  + .5f).toInt()

        private var targetAttachState = if (target.isAttachedToWindow) 1 else 0

        private var emotionDrawable: Drawable? = null

        init {
            target.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    targetAttachState = 1
                    setCallback()
                }

                override fun onViewDetachedFromWindow(v: View) {
                    targetAttachState = 2
                    target.removeOnAttachStateChangeListener(this)
                }

            })
            provider.getEmotionDrawable(emotion) {
                if (targetAttachState == 2)
                    return@getEmotionDrawable
                emotionDrawable = it
                setCallback()
            }
        }

        override fun getDrawable() = null

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = size

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            if (emotionDrawable == null)
                return
            invalidateRect.left = x.toInt()
            invalidateRect.right = invalidateRect.left + emotionDrawable!!.bounds.width()
            invalidateRect.top = top + (bottom - top - emotionDrawable!!.bounds.height()) / 2
            invalidateRect.bottom = invalidateRect.top + emotionDrawable!!.bounds.height()
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

        override fun onInvalid() = removeCallbackAndRecycleRes()

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


}