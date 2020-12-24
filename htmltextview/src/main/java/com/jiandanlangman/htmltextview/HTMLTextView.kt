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


class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        const val VERSION = 1021

        private val replaceWith = Regex(">\\s*<")
        private val replaceTo = "><"

        private var resourcesProvider: ResourcesProvider? = null

        fun registerTagHandler(tag: String, handler: TagHandler) = HTMLTagHandler.registerTagHandler(tag, handler)

        fun unRegisterTagHandler(tag: String) = HTMLTagHandler.unRegisterTagHandler(tag)

        fun setResourcesProvider(provider: ResourcesProvider?) {
            this.resourcesProvider = provider
            HTMLTagHandler.setResourcesProvider(provider)
        }

        fun fromHTML(context: Context, text: String): Spannable {
            Util.init(context)
            val formattedText = text.replace(replaceWith, replaceTo)
            return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY, null, HTMLTagHandler()) else Html.fromHtml(formattedText, null, HTMLTagHandler())) as Spannable
        }

    }


    private val onSpanClickListener: ((ActionSpan, String) -> Unit) = { _, action -> onClickListener?.invoke(this, action) }
    private val pressedSpans = ArrayList<ActionSpan>()

    private var pressedSpan = false
    private var sourceText: CharSequence = ""
    private var onClickListener: ((HTMLTextView, String) -> Unit)? = null
    private var actionSpans: ArrayList<ActionSpan>? = null

    init {
        super.setHighlightColor(Color.TRANSPARENT)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        actionSpans?.forEach { it.onValid(this) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        actionSpans?.forEach { it.onInvalid() }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        val t = text ?: ""
        if (t == sourceText)
            return
        actionSpans?.forEach { it.onInvalid() }
        actionSpans = null
        super.setText("", type)
        sourceText = t
        val spannedText = replaceEmotionToDrawable(sourceText as? Spannable ?: fromHTML(context, sourceText.toString()))
        val spans = spannedText.getSpans<ActionSpan>(0, spannedText.length)
        spans.forEach { it.setOnClickListener(onSpanClickListener) }
        actionSpans = ArrayList()
        actionSpans!!.addAll(spans)
        super.setText(spannedText, type)
        if (isAttachedToWindow)
            actionSpans?.forEach { it.onValid(this) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var height = 0
        actionSpans?.forEach { height += it.getVerticalOffset() }
        setMeasuredDimension(measuredWidth, measuredHeight + height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpans.clear()
                pressedSpan = false
                actionSpans?.forEach {
                    if (it.onPressed(event.x, event.y)) {
                        pressedSpan = true
                        pressedSpans.add(it)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val cancel = event.action == MotionEvent.ACTION_CANCEL
                pressedSpans.forEach { it.onUnPressed(event.x, event.y, cancel) }
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
            val textSize = (textSize + .5f).toInt()
            val text = spannable.toString()
            val textLength = text.length
            var startIndex = 0
            var prevPointCount = text.codePointCount(0, textLength)
            for (i in 0 until textLength) {
                val pointCount = text.codePointCount(i, textLength)
                if (pointCount != prevPointCount) {
                    val ch = text.substring(startIndex, i)
                    if (it.isEmotionDrawable(ch))
                        spannable.setSpan(EmotionSpan(it, ch, textSize), startIndex, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    startIndex = i
                    prevPointCount = pointCount
                }
            }
        }
        return spannable
    }


}