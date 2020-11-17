package com.jiandanlangman.htmltextview

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import java.util.*
import kotlin.collections.ArrayList

data class Style(
    val width: Int = 0,
    val height: Int = 0,
    val color: String = "",
    val fontSize: Int = -1,
    val padding: Rect = Rect(),
    val margin:Rect = Rect(),
    val textAlign: TextAlign = TextAlign.BASELINE,
    val textDecoration: TextDecoration = TextDecoration.NONE,
    val fontWeight: FontWeight = FontWeight.NATIVE,
    val pressed: Pressed = Pressed.NONE,
    private val background: String = ""
) {

    fun createBackgroundDrawable(target: HTMLTextView): Any? {
        if (background.isEmpty())
            return null
        val bgAttrs = background.split(";").map {
            val sp = it.split(":")
            sp[0] to if (sp.size > 1) sp[1] else ""
        }.toMap()
        val d = bgAttrs["drawable"]
        if (!d.isNullOrEmpty())
            return d
        val drawable = GradientDrawable()
        val density = target.resources.displayMetrics.density
        val stroke = tryCatchInvoke({ Color.parseColor(bgAttrs["stroke"]) }, 0)
        val strokeWidth = (tryCatchInvoke({ (bgAttrs["stroke-width"] ?: "0").toInt() }, 0) * density).toInt()
        val strokeDash = tryCatchInvoke({ (bgAttrs["stroke-dash"] ?: "0").toInt() }, 0) * density
        val strokeGap = tryCatchInvoke({ (bgAttrs["stroke-gap"] ?: "0").toInt() }, 0) * density
        drawable.setStroke(strokeWidth, stroke, strokeDash, strokeGap)
        drawable.cornerRadius = tryCatchInvoke({ (bgAttrs["radius"] ?: "0").toInt() }, 0) * density
        val gradient = bgAttrs["gradient"]
        val hasGradient = if (!gradient.isNullOrEmpty()) {
            when (gradient) {
                "linear" -> {
                    drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    val angle = tryCatchInvoke({ (bgAttrs["gradient-angle"] ?: "0").toInt() }, 0)
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
                "sweep" -> {
                    drawable.gradientType = GradientDrawable.SWEEP_GRADIENT
                    true
                }
                "radial" -> {
                    drawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                    drawable.gradientRadius = tryCatchInvoke({ (bgAttrs["gradient-radius"] ?: "0").toFloat() }, 0f)
                    true
                }
                else -> false
            }
        } else
            false
        if (hasGradient) {
            val colorList = ArrayList<Int>()
            colorList.addAll((bgAttrs["gradient-colors"] ?: "0").split(",").map { tryCatchInvoke({ Color.parseColor(it) }, 0) }.toList())
            if (colorList.size < 2)
                for (i in 0 until 2 - colorList.size)
                    colorList.add(0)
            drawable.colors = colorList.toIntArray()
        } else
            drawable.setColor(tryCatchInvoke({ Color.parseColor(bgAttrs["fill"]) }, 0))
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        return drawable
    }

    companion object {

        private const val KEY_WIDTH = "width"                           //宽度
        private const val KEY_HEIGHT = "height"                         //高度
        private const val KEY_COLOR = "color"                           //颜色
        private const val KEY_FONT_SIZE = "font-size"                   //字体大小
        private const val KEY_PADDING = "padding"                       //内边距
        private const val KEY_PADDING_LEFT = "padding-left"             //左内边距
        private const val KEY_PADDING_RIGHT = "padding-right"           //右内边距
        private const val KEY_PADDING_TOP = "padding-top"               //上内边距
        private const val KEY_PADDING_BOTTOM = "padding-bottom"         //下内边距
        private const val KEY_MARGIN = "margin"                         //外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_LEFT = "margin-left"                    //左外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_RIGHT = "margin-right"                   //上外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_TOP = "margin-top"                     //右外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_BOTTOM = "margin-bottom"                  //下外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_TEXT_ALIGN = "text-align"                 //文字对齐方式，View暂不支持
        private const val KEY_TEXT_DECORATION = "text-decoration"       //文字修饰，View暂不支持
        private const val KEY_FONT_WEIGHT = "font-weight"               //字重
        private const val KEY_PRESSED = "pressed"                       //按下后的视觉反馈

        private val locale = Locale.ENGLISH

        fun from(style: String, background: String): Style {
            val map = style.toLowerCase(locale).split(";").map {
                val keyValue = it.split(":")
                keyValue[0] to if (keyValue.size > 1) keyValue[1] else ""
            }.toMap()

            val paddingRect = Rect()
            val padding = tryCatchInvoke({ (map[KEY_PADDING] ?: "-1").toInt() }, -1)
            val paddingLeft = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_LEFT] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else padding
            }
            val paddingTop = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_TOP] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else padding
            }
            val paddingRight = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_RIGHT] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else padding
            }
            val paddingBottom = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_BOTTOM] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else padding
            }
            paddingRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
            val marginRect = Rect()
            val margin = tryCatchInvoke({ (map[KEY_MARGIN] ?: "-1").toInt() }, -1)
            val marginLeft = run {
                val tmp = tryCatchInvoke({ (map[KEY_MARGIN_LEFT] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else margin
            }
            val marginTop = run {
                val tmp = tryCatchInvoke({ (map[KEY_MARGIN_TOP] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else margin
            }
            val marginRight = run {
                val tmp = tryCatchInvoke({ (map[KEY_MARGIN_RIGHT] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else margin
            }
            val marginBottom = run {
                val tmp = tryCatchInvoke({ (map[KEY_MARGIN_BOTTOM] ?: "-1").toInt() }, -1)
                if (tmp != -1) tmp else margin
            }
            marginRect.set(marginLeft, marginTop, marginRight, marginBottom)
            return Style(
                tryCatchInvoke({ (map[KEY_WIDTH] ?: "0").toInt() }, 0),
                tryCatchInvoke({ (map[KEY_HEIGHT] ?: "0").toInt() }, 0),
                map[KEY_COLOR] ?: "",
                tryCatchInvoke({ (map[KEY_FONT_SIZE] ?: "-1").toInt() }, -1),
                paddingRect,
                marginRect,
                tryCatchInvoke({
                    val value = map[KEY_TEXT_ALIGN] ?: ""
                    TextAlign.values().firstOrNull { it.value == value } ?: TextAlign.BASELINE
                }, TextAlign.BASELINE),
                tryCatchInvoke({
                    val value = map[KEY_TEXT_DECORATION] ?: ""
                    TextDecoration.values().firstOrNull { it.value == value } ?: TextDecoration.NONE
                }, TextDecoration.NONE),
                tryCatchInvoke({
                    val value = map[KEY_FONT_WEIGHT] ?: ""
                    FontWeight.values().firstOrNull { it.value == value } ?: FontWeight.NATIVE
                }, FontWeight.NATIVE),
                tryCatchInvoke({
                    val value = map[KEY_PRESSED] ?: ""
                    Pressed.values().firstOrNull { it.value == value } ?: Pressed.NONE
                }, Pressed.NONE),
                background
            )
        }

        private fun <T> tryCatchInvoke(runnable: () -> T, errorReturn: T): T {
            return try {
                runnable.invoke()
            } catch (ignore: Throwable) {
                errorReturn
            }
        }

    }

    enum class TextAlign(val value: String) {
        BASELINE("baseline"),                       //默认值，基线对齐
        CENTER("center"),                            //垂直居中对齐


    }

    enum class TextDecoration(val value: String) {
        NONE("none"),                               //默认值，无任何修饰
        UNDERLINE("underline"),                     //显示下划线
        LINE_THROUGH("line-through")                //显示删除线
    }


    enum class FontWeight(val value: String) {
        NATIVE("native"),                           //默认值，原生，由TextView本身决定
        NORMAL("normal"),                           //正常字体
        BOLD("bold")                                //粗体
    }

    enum class Pressed(val value: String) {
        NONE("none"),                               //默认值，按下无反馈
        SCALE("scale")                              //按下缩放反馈
    }

}