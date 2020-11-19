package com.jiandanlangman.htmltextview

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import java.util.HashMap

class HTMLTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {

        fun registerTagHandler(tag: String, handler: TagHandler) = HTMLTagHandler.registerTagHandler(tag, handler)

        fun setImageGetter(imageGetter: ImageGetter?) = HTMLTagHandler.setImageGetter(imageGetter)

    }

    private var sourceText: CharSequence = ""
    private var onClickListener: ((HTMLTextView, String) -> Unit)? = null

    init {
        movementMethod = LinkMovementMethod.getInstance()
        super.setHighlightColor(Color.TRANSPARENT)
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        sourceText = text ?: ""
        val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(sourceText.toString(), Html.FROM_HTML_MODE_LEGACY, null, HTMLTagHandler(this)) else Html.fromHtml(sourceText.toString(), null, HTMLTagHandler(this))
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
                        onAction(act)
            }
        if(eventDrawable != null && !drawableActions[eventDrawable].isNullOrEmpty())
            return true
        return super.dispatchTouchEvent(event)
    }


    fun setOnClickListener(onClickListener: ((v: HTMLTextView, action: String) -> Unit)?) {
        this.onClickListener = onClickListener
    }

    internal fun onAction(action: String) = onClickListener?.invoke(this, action)

    private val drawableActions = HashMap<Drawable, String>()

    internal fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?, leftAction: String?, topAction: String?, rightAction: String?, bottomAction: String?) {
        left?.let { drawableActions[it] = leftAction ?: "" }
        top?.let { drawableActions[it] = topAction ?: "" }
        right?.let { drawableActions[it] = rightAction ?: "" }
        bottom?.let { drawableActions[it] = bottomAction ?: "" }
        setCompoundDrawables(left, top, right, bottom)
    }


}