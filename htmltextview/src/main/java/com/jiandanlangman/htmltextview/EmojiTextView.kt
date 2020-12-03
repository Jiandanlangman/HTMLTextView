package com.jiandanlangman.htmltextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class EmojiTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        private var globalEmojiDrawableProvider: ((String) -> Drawable?)? = null

        fun setGlobalEmojiDrawableProvider(provider: ((emoji: String) -> Drawable?)?) {
            globalEmojiDrawableProvider = provider
        }
    }

    private var emojiDrawableProvider: ((String) -> Drawable?)? = null


    override fun setText(text: CharSequence?, type: BufferType?) {
        val provider = emojiDrawableProvider ?: globalEmojiDrawableProvider
        provider?.let {
            val spannable = if (text is Spannable) text else if (text !is Spanned) SpannableString(text) else null
            spannable?.let { sp -> super.setText(replaceEmojiToDrawable(provider, sp), type) } ?: super.setText(text, type)
        } ?: super.setText(text, type)
    }

    fun setEmojiDrawableProvider(provider: ((emoji: String) -> Drawable?)?) {
        emojiDrawableProvider = provider
    }

    private fun replaceEmojiToDrawable(provider: (String) -> Drawable?, spannable: Spannable): Spannable {
        val length = spannable.length
        for (i in 0 until length)
            provider.invoke(spannable.substring(i, i + 1))?.let { spannable.setSpan(EmojiSpan(it), i, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        return spannable
    }

    private inner class EmojiSpan(private val emoji: Drawable) : DynamicDrawableSpan(ALIGN_BOTTOM) {

        private val invalidateRect = Rect()

        init {
            val size = (textSize + .5f).toInt()
            emoji.setBounds(0, 0, size, size)
        }

        override fun getDrawable() = emoji

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = emoji.bounds.width()

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint)
            invalidateRect.left = x.toInt()
            invalidateRect.right = invalidateRect.left + width
            invalidateRect.top = top + ( bottom - top - height) / 2
            invalidateRect.bottom = invalidateRect.top + height
        }


    }

}