package com.jiandanlangman.htmltextview

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

class Background private constructor(private val background: String) {

    companion object {

        const val KEY_DRAWABLE = "drawable"                 //背景图，当此字段不为空时，其它所有属性均无效
        const val KEY_STROKE = "stroke"                     //边框颜色
        const val KEY_STROKE_WIDTH = "stroke-width"         //边框宽度
        const val KEY_STROKE_DASH = "stroke-dash"           //边框长度
        const val KEY_STROKE_GAP = "stroke-gap"             //边框间距
        const val KEY_RADIUS = "radius"                     //圆角大小
        const val KEY_GRADIENT = "gradient"                 //渐变类型
        const val KEY_GRADIENT_ANGLE = "gradient-angle"     //渐变角度，仅线性渐变有效，只支持能被45整除的角度
        const val KEY_GRADIENT_COLOR = "gradient-colors"    //渐变颜色
        const val KEY_GRADIENT_RADIUS = "gradient-radius"   //渐变半径，仅径向渐变有效
        const val KEY_FILL = "fill"

        fun from(background: String) = Background(background)
    }


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

    private fun createDrawable(target:HTMLTextView, callback: (drawable: Drawable?) -> Unit) {
        val bgAttrs = background.split(";").map {
            val sp = it.split(":")
            sp[0] to if (sp.size > 1) sp[1] else ""
        }.toMap()
        val d = bgAttrs[KEY_DRAWABLE]
        if (!d.isNullOrEmpty()) {
            HTMLTagHandler.getImageGetter()?.getImageDrawable(target, d) {
                callback.invoke(it)
            } ?: callback.invoke(null)
            return
        }
        val drawable = GradientDrawable()
        val stroke = Util.tryCatchInvoke({ Color.parseColor(bgAttrs[KEY_STROKE]) }, 0)
        val strokeWidth =  Util.applyDimension(bgAttrs[KEY_STROKE_WIDTH] ?: "0", 0)
        val strokeDash = Util.applyDimension(bgAttrs[KEY_STROKE_DASH] ?: "0", 0).toFloat()
        val strokeGap =Util.applyDimension(bgAttrs[KEY_STROKE_GAP] ?: "0", 0).toFloat()
        drawable.setStroke(strokeWidth, stroke, strokeDash, strokeGap)
        drawable.cornerRadius = Util.applyDimension(bgAttrs[KEY_RADIUS] ?: "0", 0).toFloat()
        val gradient = bgAttrs[KEY_GRADIENT]
        val hasGradient = if (!gradient.isNullOrEmpty()) {
            when (gradient) {
                GradientType.LINEAR.value -> {
                    drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    val angle = Util.tryCatchInvoke({ (bgAttrs[KEY_GRADIENT_ANGLE] ?: "0").toInt() }, 0)
                    drawable.orientation = when {
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
                    drawable.gradientType = GradientDrawable.SWEEP_GRADIENT
                    true
                }
                GradientType.RADIAL.value -> {
                    drawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                    drawable.gradientRadius = Util.tryCatchInvoke({ (bgAttrs[KEY_GRADIENT_RADIUS] ?: "0").toFloat() }, 0f)
                    true
                }
                else -> false
            }
        } else
            false
        if (hasGradient) {
            val colorList = ArrayList<Int>()
            colorList.addAll((bgAttrs[KEY_GRADIENT_COLOR] ?: "0").split(",").map { Util.tryCatchInvoke({ Color.parseColor(it) }, 0) }.toList())
            if (colorList.size < 2)
                for (i in 0 until 2 - colorList.size)
                    colorList.add(0)
            drawable.colors = colorList.toIntArray()
        } else
            drawable.setColor(Util.tryCatchInvoke({ Color.parseColor(bgAttrs[KEY_FILL]) }, 0))
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        callback.invoke(drawable)
    }

    enum class GradientType(val value: String) {
        LINEAR("linear"),
        SWEEP("sweep"),
        RADIAL("radial")
    }

}