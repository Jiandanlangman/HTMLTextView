package com.jiandanlangman.htmltextview


internal class Drawable private constructor(drawable: String) {

    companion object {

        const val KEY_DRAWABLE_LEFT = "drawable-left"
        const val KEY_DRAWABLE_TOP = "drawable-top"
        const val KEY_DRAWABLE_RIGHT = "drawable-right"
        const val KEY_DRAWABLE_BOTTOM = "drawable-bottom"
        const val KEY_DRAWABLE_PADDING = "drawable-padding"


        fun from(drawable: String) = Drawable(drawable)

    }

    private val map = drawable.split(";").map {
        val sp = it.split(":")
        sp[0] to if (sp.size > 1) sp[1] else ""
    }.toMap()
    private val drawablePadding = Util.tryCatchInvoke({ (map[KEY_DRAWABLE_PADDING] ?: "-1").toInt() }, -1)
    private val drawableCache = HashMap<String, android.graphics.drawable.Drawable>()


    fun getDrawPadding(target: HTMLTextView) = (drawablePadding * target.resources.displayMetrics.density).toInt()

    fun getDrawables(callback: (drawables: Drawables?) -> Unit) {
        if (map.isEmpty() || map.keys.none { it != KEY_DRAWABLE_PADDING }) {
            callback.invoke(null)
            return
        }
        HTMLTagHandler.getImageGetter()?.let {
            val totalGetCount = if(map.keys.contains(KEY_DRAWABLE_PADDING)) map.size - 1 else map.size
            var currentGetCount = 0
            val drawables = Drawables(null, null, null, null)
            getDrawable(map[KEY_DRAWABLE_LEFT] ?: "") {
                currentGetCount++
                drawables.left = it
                if(currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable( map[KEY_DRAWABLE_TOP] ?: "") {
                currentGetCount++
                drawables.top = it
                if(currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable( map[KEY_DRAWABLE_RIGHT] ?: "") {
                currentGetCount++
                drawables.right = it
                if(currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
            getDrawable( map[KEY_DRAWABLE_BOTTOM] ?: "") {
                currentGetCount++
                drawables.bottom = it
                if(currentGetCount == totalGetCount)
                    callback.invoke(drawables)
            }
        } ?: callback.invoke(Drawables(null, null, null, null))
    }

    private fun getDrawable(src:String, callback: (drawable: android.graphics.drawable.Drawable?) -> Unit) {
        when {
            src.isEmpty() -> callback.invoke(null)
            drawableCache[src] != null -> callback.invoke(drawableCache[src])
            else -> HTMLTagHandler.getImageGetter()?.getImageDrawable(src, Attribute.SrcType.UNKNOWN.value) {
                if(it != null) {
                    drawableCache[src] = it
                    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                }
                callback.invoke(it)
            } ?: callback.invoke(null)
        }

    }


    class Drawables(var left: android.graphics.drawable.Drawable?, var top: android.graphics.drawable.Drawable?, var right: android.graphics.drawable.Drawable?, var bottom: android.graphics.drawable.Drawable?)


}