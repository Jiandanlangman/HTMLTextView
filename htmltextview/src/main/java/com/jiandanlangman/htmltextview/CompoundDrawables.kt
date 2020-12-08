package com.jiandanlangman.htmltextview


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
    private val drawableCache = HashMap<String, android.graphics.drawable.Drawable>()


    fun getDrawPadding() = drawablePadding

    fun getDrawables(target: HTMLTextView, callback: (drawables: Drawables?) -> Unit) {
        if (map.isEmpty() || map.keys.none { it != KEY_DRAWABLE_PADDING && it != KEY_LEFT_ACTION && it != KEY_TOP_ACTION && it != KEY_RIGHT_ACTION && it != KEY_BOTTOM_ACTION }) {
            callback.invoke(null)
            return
        }
        HTMLTagHandler.getImageGetter()?.let {
            val totalGetCount = map.keys.filter {  it != KEY_DRAWABLE_PADDING && it != KEY_LEFT_ACTION && it != KEY_TOP_ACTION && it != KEY_RIGHT_ACTION && it != KEY_BOTTOM_ACTION }.size
            var currentGetCount = 0
            val drawables = Drawables(null, null, null, null)
            getDrawable(target,map[KEY_DRAWABLE_LEFT] ?: "") {
                currentGetCount++
                drawables.left = it
                if (currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable(target,map[KEY_DRAWABLE_TOP] ?: "") {
                currentGetCount++
                drawables.top = it
                if (currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable(target,map[KEY_DRAWABLE_RIGHT] ?: "") {
                currentGetCount++
                drawables.right = it
                if (currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable(target,map[KEY_DRAWABLE_BOTTOM] ?: "") {
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
            drawableCache[src] != null -> callback.invoke(drawableCache[src])
            else -> HTMLTagHandler.getImageGetter()?.getImageDrawable(target, src) {
                if (it != null) {
                    drawableCache[src] = it
                    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                }
                callback.invoke(it)
            } ?: callback.invoke(null)
        }

    }


    class Drawables(var left: android.graphics.drawable.Drawable?, var top: android.graphics.drawable.Drawable?, var right: android.graphics.drawable.Drawable?, var bottom: android.graphics.drawable.Drawable?)
    class DrawableActions(val left: String, val top: String, val right: String, val bottom: String)

}