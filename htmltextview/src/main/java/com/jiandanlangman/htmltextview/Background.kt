package com.jiandanlangman.htmltextview

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable

class Background private constructor(private val background: String) {

    companion object {

   
        fun from(background: String) = Background(background)
    }

    private val bgAttrs = background.split(";").map {
        val sp = it.split(":")
        sp[0] to if (sp.size > 1) sp[1] else ""
    }.toMap()

    private val padding = Util.toNaturalRect(Util.getPadding(bgAttrs))

    fun getDrawable(target: HTMLTextView, callback: (drawable: Drawable?) -> Unit) {
        when {
            background.isEmpty() -> callback.invoke(null)
            else -> createDrawable(target) {
                it?.setBounds(0, 0, it.intrinsicWidth, it.intrinsicWidth)
                callback.invoke(it)
            }
        }
    }

    fun isNotBackground() = background.isEmpty()

    private fun createDrawable(target: HTMLTextView, callback: (drawable: Drawable?) -> Unit) {
        val d = bgAttrs[Constant.KEY_DRAWABLE]
        if (!d.isNullOrEmpty()) {
            HTMLTagHandler.getResourcesProvider()?.getImageDrawable(target, d) {
                val drawable = LayerDrawable(arrayOf(it))
                drawable.setLayerInset(0, padding.left, padding.top, padding.right, padding.bottom)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                callback.invoke(drawable)
            } ?: callback.invoke(null)
            return
        }
        val gradientDrawable = GradientDrawable()
        val stroke = Util.tryCatchInvoke({ Color.parseColor(bgAttrs[Constant.KEY_STROKE]) }, 0)
        val strokeWidth = Util.applyDimension(bgAttrs[Constant.KEY_STROKE_WIDTH] ?: "0", 0)
        val strokeDash = Util.applyDimension(bgAttrs[Constant.KEY_STROKE_DASH] ?: "0", 0).toFloat()
        val strokeGap = Util.applyDimension(bgAttrs[Constant.KEY_STROKE_GAP] ?: "0", 0).toFloat()
        gradientDrawable.setStroke(strokeWidth, stroke, strokeDash, strokeGap)
        gradientDrawable.cornerRadius = Util.applyDimension(bgAttrs[Constant.KEY_RADIUS] ?: "0", 0).toFloat()
        val gradient = bgAttrs[Constant.KEY_GRADIENT]
        val hasGradient = if (!gradient.isNullOrEmpty()) {
            when (gradient) {
                GradientType.LINEAR.value -> {
                    gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    val angle = Util.tryCatchInvoke({ (bgAttrs[Constant.KEY_GRADIENT_ANGLE] ?: "0").toInt() }, 0)
                    gradientDrawable.orientation = when {
                        angle < 45 -> GradientDrawable.Orientation.LEFT_RIGHT
                        angle < 90 -> GradientDrawable.Orientation.TL_BR
                        angle < 135 -> GradientDrawable.Orientation.TOP_BOTTOM
                        angle < 180 -> GradientDrawable.Orientation.TR_BL
                        angle < 225 -> GradientDrawable.Orientation.RIGHT_LEFT
                        angle < 270 -> GradientDrawable.Orientation.BR_TL
                        angle < 315 -> GradientDrawable.Orientation.BOTTOM_TOP
                        angle < 360 -> GradientDrawable.Orientation.BL_TR
                        else -> GradientDrawable.Orientation.LEFT_RIGHT
                    }
                    true
                }
                GradientType.SWEEP.value -> {
                    gradientDrawable.gradientType = GradientDrawable.SWEEP_GRADIENT
                    true
                }
                GradientType.RADIAL.value -> {
                    gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                    gradientDrawable.gradientRadius = Util.tryCatchInvoke({ (bgAttrs[Constant.KEY_GRADIENT_RADIUS] ?: "0").toFloat() }, 0f)
                    true
                }
                else -> false
            }
        } else
            false
        if (hasGradient) {
            val colorList = ArrayList<Int>()
            colorList.addAll((bgAttrs[Constant.KEY_GRADIENT_COLOR] ?: "0").split(",").map { Util.tryCatchInvoke({ Color.parseColor(it) }, 0) }.toList())
            if (colorList.size < 2)
                for (i in 0 until 2 - colorList.size)
                    colorList.add(0)
            gradientDrawable.colors = colorList.toIntArray()
        } else
            gradientDrawable.setColor(Util.tryCatchInvoke({ Color.parseColor(bgAttrs[Constant.KEY_FILL]) }, 0))
        val drawable = LayerDrawable(arrayOf(gradientDrawable))
        drawable.setLayerInset(0, padding.left, padding.top, padding.right, padding.bottom)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        callback.invoke(drawable)
    }

    enum class GradientType(val value: String) {
        LINEAR("linear"),
        SWEEP("sweep"),
        RADIAL("radial")
    }

}