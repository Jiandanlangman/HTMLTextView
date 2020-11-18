package com.jiandanlangman.htmltextview

enum class Attribute(val value:String) {

    SRC("src"),
    SRC_TYPE("src-type"),
    ACTION("action"),
    BACKGROUND("background"),
    STYLE("style");

    enum class SrcType(val value: String) {
        IMAGE_JPEG("image/jpeg"),
        IMAGE_PNG("image/png"),
        IMAGE_WEBP_GIF("image/webp-gif"),
        UNKNOWN("")
    }

}