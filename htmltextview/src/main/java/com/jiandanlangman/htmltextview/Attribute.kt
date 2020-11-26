package com.jiandanlangman.htmltextview

enum class Attribute(val value:String) {

    SRC("src"),                                     //资源地址
    SRC_TYPE("src-type"),                           //资源类型
    ACTION("action"),                               //点击后触发的回调事件
    BACKGROUND("background"),                       //背景
    DRAWABLE("drawable"),                           //上下左右需要绘制的图片，仅base标签支持
    STYLE("style");                                 //样式

    enum class SrcType(val value: String) {
        IMAGE_JPEG("image/jpeg"),
        IMAGE_PNG("image/png"),
        IMAGE_WEBP_GIF("image/webp-gif"),
        UNKNOWN("")
    }

}