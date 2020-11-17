package com.jiandanlangman.htmltextview

import java.util.*

data class Style(
    val width: Int = 0,
    val height: Int = 0,
    val color: String = "",
    val fontSize: Int = -1,
    val padding : Bounds =  Bounds(-1, -1, -1, -1),
    val textAlign: TextAlign = TextAlign.BASELINE,
    val textDecoration: TextDecoration = TextDecoration.NONE,
    val fontWeight: FontWeight = FontWeight.NATIVE,
    val pressed: Pressed = Pressed.NONE


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
        private const val KEY_TEXT_ALIGN = "text-align"                 //文字对齐方式，View暂不支持
        private const val KEY_TEXT_DECORATION = "text-decoration"       //文字修饰，View暂不支持
        private const val KEY_FONT_WEIGHT = "font-weight"               //字重
        private const val KEY_PRESSED = "pressed"                       //按下后的视觉反馈

        private val locale = Locale.ENGLISH

        fun from(style: String): Style {
            val map = style.toLowerCase(locale).split(";").map {
                val keyValue = it.split(":")
                keyValue[0] to if (keyValue.size > 1) keyValue[1] else ""
            }.toMap()
            val paddingBounds = Bounds()
            val padding = tryCatchInvoke({ (map[KEY_PADDING] ?: "-1").toInt() }, -1)
            val paddingLeft = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_LEFT] ?: "-1").toInt() }, -1)
                if(tmp != -1) tmp else padding
            }
            val paddingTop = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_TOP] ?: "-1").toInt() }, -1)
                if(tmp != -1) tmp else padding
            }
            val paddingRight = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_RIGHT] ?: "-1").toInt() }, -1)
                if(tmp != -1) tmp else padding
            }
            val paddingBottom = run {
                val tmp = tryCatchInvoke({ (map[KEY_PADDING_BOTTOM] ?: "-1").toInt() }, -1)
                 if(tmp != -1) tmp else padding
            }
            paddingBounds.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
            return Style(
                tryCatchInvoke({ (map[KEY_WIDTH] ?: "0").toInt() }, 0),
                tryCatchInvoke({ (map[KEY_HEIGHT] ?: "0").toInt() }, 0),
                map[KEY_COLOR] ?: "",
                tryCatchInvoke({ (map[KEY_FONT_SIZE] ?: "-1").toInt() }, -1),
                paddingBounds,
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
                },  FontWeight.NATIVE),
                tryCatchInvoke({
                    val value = map[KEY_PRESSED] ?: ""
                    Pressed.values().firstOrNull { it.value == value } ?: Pressed.NONE
                },  Pressed.NONE)
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