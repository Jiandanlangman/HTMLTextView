package com.jiandanlangman.htmltextview

import kotlin.math.max


internal class CompoundDrawables private constructor(drawable: String) {

    companion object {

        const val KEY_DRAWABLE_LEFT = "left"
        const val KEY_DRAWABLE_TOP = "top"
        const val KEY_DRAWABLE_RIGHT = "right"
        const val KEY_DRAWABLE_BOTTOM = "bottom"
        const val KEY_DRAWABLE_PADDING = "padding"
        const val KEY_LEFT_ACTION = "left-action"
        const val KEY_TOP_ACTION = "top-action"
        const val KEY_RIGHT_ACTION = "right-action"
        const val KEY_BOTTOM_ACTION = "bottom-action"


        fun from(drawable: String) = CompoundDrawables(drawable)

    }

    private val map = drawable.split(";").map {
        val sp = it.split(":")
        sp[0] to if (sp.size > 1) sp[1] else ""
    }.toMap()
    private val drawablePadding = Util.applyDimension(map[KEY_DRAWABLE_PADDING] ?: "-1", -1)


    fun getDrawPadding() = drawablePadding

    fun getDrawables(target: HTMLTextView, callback: (drawables: Drawables?) -> Unit) {
        if (map.isEmpty() || map.keys.none { it != KEY_DRAWABLE_PADDING && it != KEY_LEFT_ACTION && it != KEY_TOP_ACTION && it != KEY_RIGHT_ACTION && it != KEY_BOTTOM_ACTION }) {
            callback.invoke(null)
            return
        }
        HTMLTagHandler.getResourcesProvider()?.let {
            var totalGetCount = 0
            if(map.keys.contains(KEY_DRAWABLE_LEFT))
                totalGetCount++
            if(map.keys.contains(KEY_DRAWABLE_TOP))
                totalGetCount++
            if(map.keys.contains(KEY_DRAWABLE_RIGHT))
                totalGetCount++
            if(map.keys.contains(KEY_DRAWABLE_BOTTOM))
                totalGetCount++
            var currentGetCount = 0
            val drawables = Drawables(null, null, null, null)
            if(map.containsKey(KEY_DRAWABLE_LEFT))
                getDrawable(target,map[KEY_DRAWABLE_LEFT] ?: "") {
                    it?.let {
                        val maxHeight = max(target.height, Util.tryCatchInvoke({target.layoutParams.height}, 0))
                        if(maxHeight > 0 && it.intrinsicHeight > maxHeight)
                            it.setBounds(0, 0, maxHeight * it.intrinsicWidth / it.intrinsicHeight, maxHeight)
                        else
                            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                    }
                    currentGetCount++
                    drawables.left = it
                    if (currentGetCount == totalGetCount)
                        callback.invoke(drawables)
                }
            if(map.containsKey(KEY_DRAWABLE_TOP))
                getDrawable(target,map[KEY_DRAWABLE_TOP] ?: "") {
                    it?.let {
                        val maxWidth = max(target.width, Util.tryCatchInvoke({target.layoutParams.width}, 0))
                        if(maxWidth > 0 && it.intrinsicWidth > maxWidth)
                            it.setBounds(0, 0, maxWidth, maxWidth * it.intrinsicHeight / it.intrinsicWidth)
                        else
                            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                    }
                    currentGetCount++
                    drawables.top = it
                    if (currentGetCount == totalGetCount)
                        callback.invoke(drawables)
                }
            if(map.containsKey(KEY_DRAWABLE_RIGHT))
                getDrawable(target,map[KEY_DRAWABLE_RIGHT] ?: "") {
                    it?.let {
                        val maxHeight = max(target.height, Util.tryCatchInvoke({target.layoutParams.height}, 0))
                        if(maxHeight > 0 && it.intrinsicHeight > maxHeight)
                            it.setBounds(0, 0, maxHeight * it.intrinsicWidth / it.intrinsicHeight, maxHeight)
                        else
                            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                    }
                    currentGetCount++
                    drawables.right = it
                    if (currentGetCount == totalGetCount)
                        callback.invoke(drawables)
                }
            if(map.containsKey(KEY_DRAWABLE_BOTTOM))
                getDrawable(target,map[KEY_DRAWABLE_BOTTOM] ?: "") {
                    it?.let {
                        val maxWidth = max(target.width, Util.tryCatchInvoke({target.layoutParams.width}, 0))
                        if(maxWidth > 0 && it.intrinsicWidth > maxWidth)
                            it.setBounds(0, 0, maxWidth, maxWidth * it.intrinsicHeight / it.intrinsicWidth)
                        else
                            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                    }
                    currentGetCount++
                    drawables.bottom = it
                    if (currentGetCount == totalGetCount)
                        callback.invoke(drawables)
                }
        } ?: callback.invoke(Drawables(null, null, null, null))
    }

    fun getDrawableActions() = DrawableActions(map[KEY_LEFT_ACTION] ?: "", map[KEY_TOP_ACTION] ?: "", map[KEY_RIGHT_ACTION] ?: "", map[KEY_BOTTOM_ACTION] ?: "")

    private fun getDrawable(target: HTMLTextView, src: String, callback: (drawable: android.graphics.drawable.Drawable?) -> Unit) {
        when {
            src.isEmpty() -> callback.invoke(null)
            else -> HTMLTagHandler.getResourcesProvider()?.getImageDrawable(target, src) {
                callback.invoke(it)
            } ?: callback.invoke(null)
        }

    }


    class Drawables(var left: android.graphics.drawable.Drawable?, var top: android.graphics.drawable.Drawable?, var right: android.graphics.drawable.Drawable?, var bottom: android.graphics.drawable.Drawable?)
    class DrawableActions(val left: String, val top: String, val right: String, val bottom: String)

}