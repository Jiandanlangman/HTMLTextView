package com.jiandanlangman.htmltextview

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams

internal class BaseTagHandler : TagHandler {

    @SuppressLint("Range", "ClickableViewAccessibility")
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style, background: Background) {
        if (style.fontSize >= 0)
            target.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.fontSize.toFloat())
        Util.tryCatchInvoke { target.setTextColor(Color.parseColor(style.color)) }
        target.setPadding(
            if (style.padding.left >= 0) style.padding.left else target.paddingLeft,
            if (style.padding.top >= 0) style.padding.top else target.paddingTop,
            if (style.padding.right >= 0) style.padding.right else target.paddingRight,
            if (style.padding.bottom >= 0) style.padding.bottom else target.paddingBottom
        )
        target.paint.isFakeBoldText = style.fontWeight == Style.FontWeight.BOLD
        if (style.lineHeight >= 0)
            target.setLineSpacing(target.lineSpacingExtra, style.lineHeight)
        var gravity = 0
        if(style.textAlign.contains(Style.TextAlign.CENTER))
            gravity = gravity or Gravity.CENTER
        if(style.textAlign.contains(Style.TextAlign.CENTER_VERTICAL))
            gravity = gravity or Gravity.CENTER_VERTICAL
        if(style.textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL))
            gravity = gravity or Gravity.CENTER_HORIZONTAL
        if(style.textAlign.contains(Style.TextAlign.CENTER_HORIZONTAL))
            gravity = gravity or Gravity.CENTER_HORIZONTAL
        if(style.textAlign.contains(Style.TextAlign.LEFT))
            gravity = gravity or Gravity.START
        if(style.textAlign.contains(Style.TextAlign.RIGHT))
            gravity = gravity or Gravity.END
        if(style.textAlign.contains(Style.TextAlign.TOP))
            gravity = gravity or Gravity.TOP
        if(style.textAlign.contains(Style.TextAlign.BOTTOM))
            gravity = gravity or Gravity.BOTTOM
        target.gravity = gravity
        if (style.textDecoration.isNotEmpty()) {
            val paint = target.paint
            paint.isUnderlineText = style.textDecoration.contains(Style.TextDecoration.UNDERLINE)
            paint.flags = if (style.textDecoration.contains(Style.TextDecoration.LINE_THROUGH)) paint.flags or Paint.STRIKE_THRU_TEXT_FLAG else paint.flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        val action = attrs[Attribute.ACTION.value] ?: ""
        if (action.isNotEmpty()) {
            val baseSpan = BaseSpan(action)
            output.append("\u200B")
            output.setSpan(baseSpan, start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            var pressedTarget = false
            target.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val actionSpans = LinkMovementMethod.getEventActionSpan(target, target.text as Spannable, event, ActionSpan::class.java)
                        pressedTarget =  actionSpans?.let { it.firstOrNull { s -> !s.getAction().isNullOrEmpty() } == null } ?: true
                        if (pressedTarget && style.pressedScale != 1f)
                            playScaleAnimator(target, style.pressedScale)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (pressedTarget) {
                        if (style.pressedScale != 1f)
                            playScaleAnimator(target, 1f)
                        if (event.action == MotionEvent.ACTION_UP)
                            baseSpan.listener.invoke(baseSpan, action)
                    }
                }
                return@setOnTouchListener false
            }
        }
        fun updateLayoutParams(v: View) {
            v.updateLayoutParams<ViewGroup.LayoutParams> {
                if (style.width > 0)
                    width = style.width
                if (style.height > 0)
                    height = style.height
                setMargin(this, style)
            }
        }

        var targetAttachState = if (target.isAttachedToWindow) 1 else 0
        if (targetAttachState == 1)
            updateLayoutParams(target)
        target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                targetAttachState = 1
                updateLayoutParams(target)
            }

            override fun onViewDetachedFromWindow(v: View?) {
                targetAttachState = 2
                target.removeOnAttachStateChangeListener(this)
            }

        })
        background.getDrawable(target) {
            if (targetAttachState == 2)
                return@getDrawable
            it?.let { target.background = it }
        }
        val drawable = CompoundDrawables.from(attrs[Attribute.DRAWABLE.value] ?: "")
        val drawablePadding = drawable.getDrawPadding()
        if (drawablePadding >= 0)
            target.compoundDrawablePadding = drawablePadding
        drawable.getDrawables(target) {
            if (targetAttachState == 2)
                return@getDrawables
            it?.let {
                val actions = drawable.getDrawableActions()
                target.setCompoundDrawables(it.left, it.top, it.right, it.bottom, actions.left, actions.top, actions.right, actions.bottom)
            }
        }
    }

    override fun isSingleTag() = true

    private fun playScaleAnimator(target: HTMLTextView, to: Float) {
        target.animate().cancel()
        target.animate().scaleX(to).scaleY(to).setDuration(64).start()
    }

    private fun setMargin(params: ViewGroup.LayoutParams, style: Style) {
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

    private class BaseSpan(private val action:String) : ClickableSpan(), ActionSpan {

        var listener: ((ActionSpan, String) -> Unit) = { _, _ -> }

        override fun onClick(widget: View) = Unit

        override fun setOnClickListener(listener: (span: ActionSpan, action: String) -> Unit) {
            this.listener = listener
        }

        override fun onPressed() = Unit

        override fun onUnPressed(isClick: Boolean) = Unit

        override fun getAction() = action

    }

}