package com.jiandanlangman.htmltextview

object Constant {

    const val DIMENSION_MATCH_PARENT = -2
    const val DIMENSION_WRAP_CONTENT = -1
    const val DIMENSION_UNDEFINED = -3


     const val KEY_WIDTH = "width"                           //宽度
     const val KEY_HEIGHT = "height"                         //高度
     const val KEY_COLOR = "color"                           //颜色
     const val KEY_FONT_SIZE = "font-size"                   //字体大小
     const val KEY_PADDING = "padding"                       //内边距
     const val KEY_PADDING_LEFT = "padding-left"             //左内边距
     const val KEY_PADDING_RIGHT = "padding-right"           //右内边距
     const val KEY_PADDING_TOP = "padding-top"               //上内边距
     const val KEY_PADDING_BOTTOM = "padding-bottom"         //下内边距
     const val KEY_MARGIN = "margin"                         //外边距，仅支持View且必须要Target的LayoutParams支持margin属性
     const val KEY_MARGIN_LEFT = "margin-left"               //左外边距，仅支持View且必须要Target的LayoutParams支持margin属性
     const val KEY_MARGIN_RIGHT = "margin-right"             //上外边距，仅支持View且必须要Target的LayoutParams支持margin属性
     const val KEY_MARGIN_TOP = "margin-top"                 //右外边距，仅支持View且必须要Target的LayoutParams支持margin属性
     const val KEY_MARGIN_BOTTOM = "margin-bottom"           //下外边距，仅支持View且必须要Target的LayoutParams支持margin属性
     const val KEY_TEXT_ALIGN = "text-align"                 //文字对齐方式，View暂不支持
     const val KEY_TEXT_DECORATION = "text-decoration"       //文字修饰，View暂不支持
     const val KEY_FONT_WEIGHT = "font-weight"               //字重
     const val KEY_PRESSED_SCALE = "pressed-scale"           //按下后的缩放级别，默认1为不缩放
     const val KEY_PRESSED_TINT = "pressed-tint"             //按下后的着色颜色，默认为透明
     const val KEY_LINE_HEIGHT = "line-height"               //行间距
     const val KEY_STROKE_WIDTH = "stroke-width"             //文字描边大小
     const val KEY_STROKE = "stroke"                         //文字描边颜色
     const val KEY_FONT_FAMILY = "font-family"               //字体
    const val KEY_DRAWABLE = "drawable"                     //背景图，当此字段不为空时，其它所有属性均无效
    const val KEY_STROKE_DASH = "stroke-dash"               //边框长度
    const val KEY_STROKE_GAP = "stroke-gap"                 //边框间距
    const val KEY_RADIUS = "radius"                         //圆角大小
    const val KEY_GRADIENT = "gradient"                     //渐变类型
    const val KEY_GRADIENT_ANGLE = "gradient-angle"         //渐变角度，仅线性渐变有效，只支持能被45整除的角度
    const val KEY_GRADIENT_COLOR = "gradient-colors"        //渐变颜色
    const val KEY_GRADIENT_RADIUS = "gradient-radius"       //渐变半径，仅径向渐变有效
    const val KEY_FILL = "fill"                             //填充色


}