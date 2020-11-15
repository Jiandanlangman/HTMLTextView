package com.jiandanlangman.htmltextview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan

class ImgTagHandler : TagHandler {
    override fun handleTag(target: HTMLTextView, tag: String, output: Editable, start: Int, attrs: Map<String, String>, styles: Map<String, String>) {
        output.append("\u200B")
        output.setSpan(ImgSpan(target, attrs, styles), start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private class ImgSpan(val target: HTMLTextView, attrs: Map<String, String>, styles: Map<String, String>) : DynamicDrawableSpan(ALIGN_BASELINE) {


        override fun getDrawable(): Drawable? {
            return null
        }

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {

        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            return 54
        }



    }
}