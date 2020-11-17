package com.jiandanlangman.htmltextview

import android.graphics.RectF
import android.text.Spannable
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView


internal class LinkMovementMethod : android.text.method.LinkMovementMethod() {

    companion object {

        private val INSTANCE = LinkMovementMethod()

        private val touchedLineBounds = RectF()

        fun getInstance() = INSTANCE

        fun getEventActionSpan(widget: TextView, spannable: Spannable, event: MotionEvent) : Array<ActionSpan> {
            var x = event.x
            var y = event.y
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y.toInt())
            val off = layout.getOffsetForHorizontal(line, x)
            touchedLineBounds.left = layout.getLineLeft(line)
            touchedLineBounds.top = layout.getLineTop(line).toFloat()
            touchedLineBounds.right = layout.getLineWidth(line) + touchedLineBounds.left
            touchedLineBounds.bottom = layout.getLineBottom(line).toFloat()
            if (touchedLineBounds.contains(x, y))
                return spannable.getSpans(off, off, ActionSpan::class.java)
            return arrayOf()
        }

    }

    private val touchedLineBounds = RectF()
    private val pressedSpans = HashMap<TextView, ArrayList<ActionSpan>>()

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        Log.d("HTMLTextView", "LinkMovementMethod")
        if(event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
            pressedSpans.remove(widget)?.forEach { sp ->  sp.onUnPressed() }
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
            val actionSpans = getEventActionSpan(widget, buffer, event)
            if (!actionSpans.isNullOrEmpty()) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val spans = ArrayList<ActionSpan>()
                    actionSpans.forEach {
                        it.onPressed()
                        spans.add(it)
                    }
                    pressedSpans[widget] = spans
                } else
                    actionSpans.forEach { (widget as? HTMLTextView)?.onAction(it.getAction()) }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

}