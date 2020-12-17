package com.jiandanlangman.htmltextview

import android.graphics.Rect
import android.graphics.Typeface
import java.util.*
import kotlin.collections.ArrayList

data class Style(
    val width: Int = 0,
    val height: Int = 0,
    val color: String = "",
    val fontSize: Int = -1,
    val padding: Rect = Rect(),
    val margin: Rect = Rect(),
    val textAlign: Array<TextAlign> = arrayOf(TextAlign.LEFT, TextAlign.CENTER_VERTICAL),
    val textDecoration: Array<TextDecoration> = arrayOf(TextDecoration.NONE),
    val fontWeight: FontWeight = FontWeight.NATIVE,
    val pressedScale: Float = 1f,
    val pressedTint: String = "",
    val lineHeight: Float = -1f,
    val strokeWidth: Float = 0f,
    val stroke: String = "",
    val typeface: Typeface? = null,
    val spanLine:Int = 0
) {

    companion object {

        private val locale = Locale.ENGLISH

        fun from(style: String): Style {
            val map = style.toLowerCase(locale).split(";").map {
                val keyValue = it.split(":")
                keyValue[0] to if (keyValue.size > 1) keyValue[1] else ""
            }.toMap()
            val textAlignList = ArrayList<TextAlign>()
            (map[Constant.KEY_TEXT_ALIGN] ?: "").split(",").forEach {
                val align = TextAlign.values().firstOrNull { e -> e.value == it }
                if (align != null)
                    textAlignList.add(align)
            }
            if (textAlignList.isEmpty()) {
                textAlignList.add(TextAlign.LEFT)
                textAlignList.add(TextAlign.CENTER_VERTICAL)
            }
            val tdList = ArrayList<TextDecoration>()
            (map[Constant.KEY_TEXT_DECORATION] ?: "").split(",").forEach {
                val td = TextDecoration.values().firstOrNull { e -> e.value == it }
                if (td != null)
                    tdList.add(td)
            }
            if (tdList.isEmpty())
                tdList.add(TextDecoration.NONE)
            return Style(
                Util.applyDimension(map[Constant.KEY_WIDTH] ?: Constant.DIMENSION_UNDEFINED.toString(), Constant.DIMENSION_UNDEFINED),
                Util.applyDimension(map[Constant.KEY_HEIGHT] ?: Constant.DIMENSION_UNDEFINED.toString(), Constant.DIMENSION_UNDEFINED),
                map[Constant.KEY_COLOR] ?: "",
                Util.applyDimension(map[Constant.KEY_FONT_SIZE] ?: "-1", -1),
                Util.getPadding(map),
                Util.getMargin(map),
                textAlignList.toTypedArray(),
                tdList.toTypedArray(),
                Util.tryCatchInvoke({
                    val value = map[Constant.KEY_FONT_WEIGHT] ?: ""
                    FontWeight.values().firstOrNull { it.value == value } ?: FontWeight.NATIVE
                }, FontWeight.NATIVE),
                Util.tryCatchInvoke({ (map[Constant.KEY_PRESSED_SCALE] ?: "1").toFloat() }, 1f),
                map[Constant.KEY_PRESSED_TINT] ?: "",
                Util.tryCatchInvoke({ (map[Constant.KEY_LINE_HEIGHT] ?: "-1").toFloat() }, -1f),
                Util.tryCatchInvoke({ (map[Constant.KEY_STROKE_WIDTH] ?: "0").toFloat() }, 0f),
                map[Constant.KEY_STROKE] ?: "",
                HTMLTagHandler.getResourcesProvider()?.getTypeface(map[Constant.KEY_FONT_FAMILY] ?: ""),
                Util.tryCatchInvoke({(map[Constant.KEY_SPAN_LINE]?: "0").toInt()  }, 0)
            )

        }


    }

    enum class TextAlign(val value: String) {
        CENTER("center"),                            //默认值，居中对齐
        TOP("top"),                                  //顶对齐
        BOTTOM("bottom"),                            //底对齐
        CENTER_VERTICAL("center-vertical"),          //纵向居中
        CENTER_HORIZONTAL("center-horizontal"),      //横向居中
        LEFT("left"),                                //左对齐
        RIGHT("right")                               //右对齐


    }

    enum class TextDecoration(val value: String) {
        NONE("none"),                               //默认值，无任何修饰
        UNDERLINE("underline"),                     //显示下划线
        LINE_THROUGH("line-through"),                //显示删除线
        STROKE("stroke")                            //描边
    }


    enum class FontWeight(val value: String) {
        NATIVE("native"),                           //默认值，原生，由TextView本身决定
        NORMAL("normal"),                           //正常字体
        BOLD("bold")                                //粗体
    }

}