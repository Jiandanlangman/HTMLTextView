package com.jiandanlangman.htmltextview

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams

class ViewTagHandler : TagHandler {

    @SuppressLint("Range", "ClickableViewAccessibility")
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, style: Style) {
        val density = target.context.resources.displayMetrics.density
        if (style.fontSize >= 0)
            target.setTextSize(TypedValue.COMPLEX_UNIT_DIP, style.fontSize.toFloat())
        try {
            target.setTextColor(Color.parseColor(style.color))
        } catch (ignore: Throwable) {

        }
        target.setPadding(
            if (style.padding.left >= 0) (style.padding.left * density).toInt() else target.paddingLeft,
            if (style.padding.top >= 0) (style.padding.top * density).toInt() else target.paddingTop,
            if (style.padding.right >= 0) (style.padding.right * density).toInt() else target.paddingRight,
            if (style.padding.bottom >= 0) (style.padding.bottom * density).toInt() else target.paddingBottom
        )
        target.paint.isFakeBoldText = style.fontWeight == Style.FontWeight.BOLD
        target.updateLayoutParams<ViewGroup.LayoutParams> {
            if (style.width > 0)
                width = (style.width * density + .5f).toInt()
            if (style.height > 0)
                height = (style.height * density + .5f).toInt()
        }
        val action = attrs[Attribute.ACTION.value] ?: ""
        if (action.isNotEmpty()) {
            var pressedTarget = false
            target.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressedTarget = LinkMovementMethod.getEventActionSpan(target, target.text as Spannable, event).isEmpty()
                        if (pressedTarget && style.pressed == Style.Pressed.SCALE)
                            playScaleAnimator(target, .88f)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if(pressedTarget) {
                        if (style.pressed == Style.Pressed.SCALE)
                            playScaleAnimator(target, 1f)
                        if (event.action == MotionEvent.ACTION_UP)
                            target.onAction(action)
                    }
                }
                return@setOnTouchListener false
            }
        }
    }

    private fun playScaleAnimator(target: HTMLTextView, to: Float) {
        target.animate().cancel()
        target.animate().scaleX(to).scaleY(to).setDuration(64).start()
    }

}