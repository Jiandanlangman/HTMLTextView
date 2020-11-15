package com.jiandanlangman.htmltextview

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        fun registerTagHandler(tag: String, handler: TagHandler) = HTMLTagHandler.registerTagHandler(tag, handler)

    }

    private var sourceText : CharSequence = ""

    init {

    }



    override fun setText(text: CharSequence?, type: BufferType?) {
        sourceText = text ?: ""
        val spannedText =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(sourceText.toString(), Html.FROM_HTML_MODE_LEGACY, null, HTMLTagHandler(this)) else Html.fromHtml(sourceText.toString(), null, HTMLTagHandler(this))
        super.setText(spannedText, type)
    }

}