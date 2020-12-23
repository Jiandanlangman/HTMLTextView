package com.jiandanlangman.htmltextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.getSpans
import androidx.core.text.toSpannable


class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        private var resourcesProvider: ResourcesProvider? = null

        fun registerTagHandler(tag: String, handler: TagHandler) = HTMLTagHandler.registerTagHandler(tag, handler)

        fun unRegisterTagHandler(tag: String) = HTMLTagHandler.unRegisterTagHandler(tag)

        fun setResourcesProvider(provider: ResourcesProvider?) {
            this.resourcesProvider = provider
            HTMLTagHandler.setResourcesProvider(provider)
        }


    }


    private val onSpanClickListener: ((ActionSpan, String) -> Unit) = { _, action -> onClickListener?.invoke(this, action) }
    private val pressedSpans = ArrayList<ActionSpan>()

    private var pressedSpan = false
    private var sourceText: CharSequence = ""
    private var onClickListener: ((HTMLTextView, String) -> Unit)? = null
    private var targetInvalidWatchers : ArrayList<TargetInvalidWatcher> ?= null

    init {
        Util.init(context)
        super.setHighlightColor(Color.TRANSPARENT)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        targetInvalidWatchers?.forEach { it.onValid() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        targetInvalidWatchers?.forEach { it.onInvalid() }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        val t = text ?: ""
        if (t == sourceText)
            return
        sourceText = t
        targetInvalidWatchers?.forEach { it.onInvalid() }
        targetInvalidWatchers = null
        val spannedText = replaceEmotionToDrawable((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(sourceText.toString(), Html.FROM_HTML_MODE_LEGACY, null, HTMLTagHandler(this)) else Html.fromHtml(sourceText.toString(), null, HTMLTagHandler(this))) as Spannable)
        val spans = spannedText.getSpans<ActionSpan>(0, spannedText.length)
        spans.forEach { it.setOnClickListener(onSpanClickListener) }
        spannedText.getSpans<IBaseSpan>(0, spannedText.length).let {
            if (!it.isNullOrEmpty())
                it[0]
            else
                null
        }?.bindAttrs(this)
        super.setText(spannedText, type)
        spannedText.getSpans(0, spannedText.length, TargetInvalidWatcher::class.java)?.let {
            targetInvalidWatchers = ArrayList()
            targetInvalidWatchers!!.addAll(it)
        }
        if(isAttachedToWindow)
            targetInvalidWatchers?.forEach { it.onValid() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        (text as? Spannable)?.let {
            var height = 0
            it.getSpans(0, it.length, ActionSpan::class.java)?.forEach { span ->
                height += span.getOffset()
            }
            setMeasuredDimension(measuredWidth, measuredHeight + height)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpans.clear()
                pressedSpan = false
                val actionSpans = Util.getEventSpan(this, text.toSpannable(), event, ActionSpan::class.java)
                if (!actionSpans.isNullOrEmpty()) {
                    pressedSpans.addAll(actionSpans)
                    actionSpans.forEach {
                        it.onPressed()
                        if (!it.getAction().isNullOrEmpty())
                            pressedSpan = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                pressedSpans.forEach { it.onUnPressed(true) }
                pressedSpans.clear()
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedSpans.forEach { it.onUnPressed(false) }
                pressedSpans.clear()
            }
        }
        return if (pressedSpan) pressedSpan else super.onTouchEvent(event)
    }


    @Deprecated("")
    override fun setHighlightColor(color: Int) {

    }


    fun setOnClickListener(onClickListener: ((v: HTMLTextView, action: String) -> Unit)?) {
        this.onClickListener = onClickListener
    }


    private fun replaceEmotionToDrawable(spannable: Spannable): Spannable {
        resourcesProvider?.let {
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


}