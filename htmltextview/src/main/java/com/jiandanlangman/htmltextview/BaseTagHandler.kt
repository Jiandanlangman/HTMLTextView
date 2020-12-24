package com.jiandanlangman.htmltextview

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.core.view.updateLayoutParams

internal class BaseTagHandler : TagHandler {


    private companion object {

        fun setMargin(params: ViewGroup.LayoutParams, style: Style) {
            val clazz = params::class.java
            fun set(name: String, value: Int) {
                Util.tryCatchInvoke {
                    val field = clazz.getField(name)
                    field.set(params, value)
                }
            }
            if (style.margin.left >= 0)
                set("leftMargin", style.margin.left)
            if (style.margin.right >= 0)
                set("rightMargin", style.margin.right)
            if (style.margin.top >= 0)
                set("topMargin", style.margin.top)
            if (style.margin.left >= 0)
                set("bottomMargin", style.margin.bottom)
        }

        fun playScaleAnimator(target: HTMLTextView, to: Float) {
            target.animate().cancel()
            target.animate().scaleX(to).scaleY(to).setDuration(64).start()
        }


    }


    @SuppressLint("Range", "ClickableViewAccessibility")
    override fun handleTag(tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        val baseSpan = BaseSpan(attrs, style, background)
        output.append("\u200B")
        output.setSpan(baseSpan, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun isSingleTag() = true

    internal class BaseSpan(attrs: Map<String, String>, private val style: Style, private val background: Background) : ForegroundColorSpan(Color.TRANSPARENT), ActionSpan {

        private val action = attrs[Attribute.ACTION.value] ?: ""
        private val drawable = CompoundDrawables.from(attrs[Attribute.DRAWABLE.value] ?: "")

        private var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }

        private var target: HTMLTextView? = null

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }


        override fun getAction() = action

        @SuppressLint("Range")
        override fun onValid(target: HTMLTextView) {
            this.target = target.apply {
                if (style.fontSize >= 0)
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, style.fontSize.toFloat())
                Util.tryCatchInvoke { setTextColor(Color.parseColor(style.color)) }
                setPadding(
                    if (style.padding.left >= 0) style.padding.left else paddingLeft,
                    if (style.padding.top >= 0) style.padding.top else paddingTop,
                    if (style.padding.right >= 0) style.padding.right else paddingRight,
                    if (style.padding.bottom >= 0) style.padding.bottom else paddingBottom
                )
                paint.isFakeBoldText = style.fontWeight == Style.FontWeight.BOLD
                if (style.lineHeight >= 0)
                    setLineSpacing(lineSpacingExtra, style.lineHeight)
                var gravity = 0
                if (style.textAlign.contains(Style.TextAlign.CENTER))
                    gravity = gravity or Gravity.CENTER
                if (style.textAlign.contains(Style.TextAlign.CENTER_VERTICAL))
                    gravity = gravity or Gravity.CENTER_VERTICAL
                if (style.textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL))
                    gravity = gravity or Gravity.CENTER_HORIZONTAL
                if (style.textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL))
                    gravity = gravity or Gravity.CENTER_HORIZONTAL
                if (style.textAlign.contains(Style.TextAlign.LEFT))
                    gravity = gravity or Gravity.START
                if (style.textAlign.contains(Style.TextAlign.RIGHT))
                    gravity = gravity or Gravity.END
                if (style.textAlign.contains(Style.TextAlign.TOP))
                    gravity = gravity or Gravity.TOP
                if (style.textAlign.contains(Style.TextAlign.BOTTOM))
                    gravity = gravity or Gravity.BOTTOM
                setGravity(gravity)
                if (style.textDecoration.isNotEmpty()) {
                    paint.isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
                    paint.flags = if (style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)) paint.flags or Paint.STRIKE_THRU_TEXT_FLAG else paint.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                style.typeface?.let { typeface = it }
                fun updateLayoutParams(v: View) {
                    v.updateLayoutParams<ViewGroup.LayoutParams> {
                        if (style.width > Constant.DIMENSION_UNDEFINED)
                            width = style.width
                        if (style.height > Constant.DIMENSION_UNDEFINED)
                            height = style.height
                        setMargin(this, style)
                    }
                }

                var targetAttachState = if (isAttachedToWindow) 1 else 0
                if (targetAttachState == 1)
                    updateLayoutParams(this)
                addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View?) {
                        targetAttachState = 1
                        updateLayoutParams(this@apply)
                    }

                    override fun onViewDetachedFromWindow(v: View?) {
                        targetAttachState = 2
                        removeOnAttachStateChangeListener(this)
                    }

                })
                this@BaseSpan.background.getDrawable(this) {
                    if (targetAttachState == 2)
                        return@getDrawable
                    it?.let { background = it }
                }
                val drawablePadding = drawable.getDrawPadding()
                if (drawablePadding >= 0)
                    compoundDrawablePadding = drawablePadding
                drawable.getDrawables(this) {
                    if (targetAttachState == 2)
                        return@getDrawables
                    it?.let {
                        setCompoundDrawables(it.left, it.top, it.right, it.bottom)
                    }
                }
            }
        }

        override fun onInvalid() {
            target = null
        }

        private val location = IntArray(2)
        private val rect = Rect()
        private val drawableRect = Rect()
        private var eventDrawable: Drawable? = null
        private var drawableAction: String? = null
        private var pressedTarget = false


        override fun onPressed(x: Float, y: Float): Boolean {
            target?.let {
                eventDrawable = null
                drawableAction = null
                it.getLocationInWindow(location)
                rect.set(location[0], location[1], location[0] + it.width, location[1] + it.height)
                val xInt = x.toInt()
                val yInt = y.toInt()
                it.compoundDrawables[0]?.let { d ->
                    val bounds = d.bounds
                    drawableRect.left = rect.left + it.paddingLeft
                    drawableRect.top = rect.top + it.paddingTop + (rect.height() - it.paddingTop - it.paddingBottom - bounds.height()) / 2
                    drawableRect.right = drawableRect.left + bounds.width()
                    drawableRect.bottom = drawableRect.top + bounds.height()
                    if (drawableRect.contains(xInt, yInt)) {
                        eventDrawable = d
                        drawableAction = drawable.getDrawableActions().left
                    }
                }
                if (eventDrawable == null)
                    it.compoundDrawables[1]?.let { d ->
                        val bounds = d.bounds
                        drawableRect.left = rect.left + it.paddingLeft + (rect.width() - it.paddingLeft - it.paddingRight - bounds.width()) / 2
                        drawableRect.top = rect.top + it.paddingTop
                        drawableRect.right = drawableRect.left + bounds.width()
                        drawableRect.bottom = drawableRect.top + bounds.height()
                        if (drawableRect.contains(xInt, yInt)) {
                            eventDrawable = d
                            drawableAction = drawable.getDrawableActions().top
                        }
                    }
                if (eventDrawable == null)
                    it.compoundDrawables[2]?.let { d ->
                        val bounds = d.bounds
                        drawableRect.right = rect.right - it.paddingRight
                        drawableRect.left = drawableRect.right - bounds.width()
                        drawableRect.top = rect.top + it.paddingTop + (rect.height() - it.paddingTop - it.paddingBottom - bounds.height()) / 2
                        drawableRect.bottom = drawableRect.top + bounds.height()
                        if (drawableRect.contains(xInt, yInt)) {
                            eventDrawable = d
                            drawableAction = drawable.getDrawableActions().right
                        }
                    }
                if (eventDrawable == null)
                    it.compoundDrawables[3]?.let { d ->
                        val bounds = d.bounds
                        drawableRect.left = rect.left + it.paddingLeft + (rect.width() - it.paddingLeft - it.paddingRight - bounds.width()) / 2
                        drawableRect.right = drawableRect.left + bounds.width()
                        drawableRect.bottom = rect.bottom - it.paddingBottom
                        drawableRect.top = drawableRect.bottom - bounds.height()
                        if (drawableRect.contains(xInt, yInt)) {
                            eventDrawable = d
                            drawableAction = drawable.getDrawableActions().bottom
                        }
                    }
                if ((eventDrawable == null || drawableAction.isNullOrEmpty()) && action.isNotEmpty()) {
                    val actionSpans = Util.getEventSpan(it, it.text.toSpannable(), x, y, ActionSpan::class.java)
                    pressedTarget = actionSpans?.let { sp -> sp.firstOrNull { s -> s.getAction().isNotEmpty() } == null } ?: true
                    if (pressedTarget && style.pressedScale != 1f)
                        playScaleAnimator(it, style.pressedScale)
                }
                return pressedTarget || (eventDrawable != null && !drawableAction.isNullOrEmpty())
            }
            return false
        }

        override fun onUnPressed(x: Float, y: Float, cancel: Boolean) {
            target?.let {
                if (cancel) {
                    if (action.isNotEmpty() && pressedTarget && style.pressedScale != 1f)
                        playScaleAnimator(it, 1f)
                } else {
                    if (eventDrawable != null && drawableRect.contains(x.toInt(), y.toInt()) && !drawableAction.isNullOrEmpty())
                        listener.invoke(this, drawableAction!!)
                    else if (action.isNotEmpty() && pressedTarget) {
                        if (style.pressedScale != 1f)
                            playScaleAnimator(it, 1f)
                        listener.invoke(this, action)
                    }
                }
            }
        }

        override fun getVerticalOffset() = 0

    }


}