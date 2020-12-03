package com.jiandanlangman.htmltextview

import android.graphics.Rect
import java.util.*
import kotlin.collections.ArrayList

data class Style(
    val width: Int = 0,
    val height: Int = 0,
    val color: String = "",
    val fontSize: Int = -1,
    val padding: Rect = Rect(),
    val margin: Rect = Rect(),
    val textAlign: TextAlign = TextAlign.CENTER,
    val textDecoration: Array<TextDecoration> = emptyArray(),
    val fontWeight: FontWeight = FontWeight.NATIVE,
    val pressedScale:Float = 1f,
    val pressedTint:String = "",
    val lineHeight: Float = -1f
) {

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
        private const val KEY_MARGIN_LEFT = "margin-left"               //左外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_RIGHT = "margin-right"             //上外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_TOP = "margin-top"                 //右外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_MARGIN_BOTTOM = "margin-bottom"           //下外边距，仅支持View且必须要Target的LayoutParams支持margin属性
        private const val KEY_TEXT_ALIGN = "text-align"                 //文字对齐方式，View暂不支持
        private const val KEY_TEXT_DECORATION = "text-decoration"       //文字修饰，View暂不支持
        private const val KEY_FONT_WEIGHT = "font-weight"               //字重
        private const val KEY_PRESSED_SCALE = "pressed-scale"           //按下后的缩放级别，默认1为不缩放
        private const val KEY_PRESSED_TINT = "pressed-tint"             //按下后的着色颜色，默认为透明
        private const val KEY_LINE_HEIGHT = "line-height"               //行间距

        private val locale = Locale.ENGLISH

        fun from(style: String): Style {
            val map = style.toLowerCase(locale).split(";").map {
                val keyValue = it.split(":")
                keyValue[0] to if (keyValue.size > 1) keyValue[1] else ""
            }.toMap()

            val paddingRect = Rect()
            val padding = Util.applyDimension(map[KEY_PADDING] ?: "-1", -1)
            val paddingLeft = run {
                val tmp = Util.applyDimension(map[KEY_PADDING_LEFT] ?: "-1", -1)
                if (tmp >= 0) tmp else padding
            }
            val paddingTop = run {
                val tmp = Util.applyDimension(map[KEY_PADDING_TOP] ?: "-1", -1)
                if (tmp >= 0) tmp else padding
            }
            val paddingRight = run {
                val tmp = Util.applyDimension(map[KEY_PADDING_RIGHT] ?: "-1", -1)
                if (tmp >= 0) tmp else padding
            }
            val paddingBottom = run {
                val tmp = Util.applyDimension(map[KEY_PADDING_BOTTOM] ?: "-1", -1)
                if (tmp >= 0) tmp else padding
            }
            paddingRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
            val marginRect = Rect()
            val margin = Util.applyDimension(map[KEY_MARGIN] ?: "-1", -1)
            val marginLeft = run {
                val tmp = Util.applyDimension(map[KEY_MARGIN_LEFT] ?: "-1", -1)
                if (tmp >= 0) tmp else margin
            }
            val marginTop = run {
                val tmp = Util.applyDimension(map[KEY_MARGIN_TOP] ?: "-1", -1)
                if (tmp >= 0) tmp else margin
            }
            val marginRight = run {
                val tmp = Util.applyDimension(map[KEY_MARGIN_RIGHT] ?: "-1", -1)
                if (tmp >= 0) tmp else margin
            }
            val marginBottom = run {
                val tmp = Util.applyDimension(map[KEY_MARGIN_BOTTOM] ?: "-1", -1)
                if (tmp >= 0) tmp else margin
            }
            marginRect.set(marginLeft, marginTop, marginRight, marginBottom)

            val tdList = ArrayList<TextDecoration>()
            (map[KEY_TEXT_DECORATION] ?: "").split(",").forEach {
                val td = TextDecoration.values().firstOrNull { e -> e.value == it }
                if (td != null)
                    tdList.add(td)
            }
            return Style(
                Util.applyDimension(map[KEY_WIDTH] ?: "0", 0),
                Util.applyDimension(map[KEY_HEIGHT] ?: "0", 0),
                map[KEY_COLOR] ?: "",
                Util.applyDimension(map[KEY_FONT_SIZE] ?: "-1", -1),
                paddingRect,
                marginRect,
                Util.tryCatchInvoke({
                    val value = map[KEY_TEXT_ALIGN] ?: ""
                    TextAlign.values().firstOrNull { it.value == value } ?: TextAlign.CENTER
                }, TextAlign.CENTER),
                tdList.toTypedArray(),
                Util.tryCatchInvoke({
                    val value = map[KEY_FONT_WEIGHT] ?: ""
                    FontWeight.values().firstOrNull { it.value == value } ?: FontWeight.NATIVE
                }, FontWeight.NATIVE),
                Util.tryCatchInvoke ({ (map[KEY_PRESSED_SCALE] ?: "1").toFloat()  }, 1f),
                map[KEY_PRESSED_TINT] ?: "",
                Util.tryCatchInvoke({ (map[KEY_LINE_HEIGHT] ?: "-1").toFloat() }, -1f)
            )

        }


    }

    enum class TextAlign(val value: String) {
        CENTER("center"),                            //默认值，垂直居中对齐
        TOP("top"),                                  //顶对齐
        BOTTOM("bottom")                             //底对齐


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
        SCALE("scale"),                             //按下缩放反馈
        TINT("tint")                                //染色
    }

}