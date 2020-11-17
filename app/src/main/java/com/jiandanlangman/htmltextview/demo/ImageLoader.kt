package com.jiandanlangman.htmltextview.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import java.io.File


/**
 * glide图片加载工具类
 */
object ImageLoader {

    const val LIST_AVATAR_SIZE = 168
    const val ALBUM_THUMB_SIZE = 360
    const val BLUR_PARAMS = "sharpen,160/bright,16/contrast,16/blur,r_50,s_24"

    private val crossFadeTransition = DrawableTransitionOptions.with(DrawableCrossFadeFactory.Builder(200).setCrossFadeEnabled(true).build())


    /**
     * @param with 可传入Activity, Fragment或Context(不推荐用Context)
     * with尽量不要Context，Glide会有严重的性能问题，仅限拿不到activity或fragment时使用(例如Service中，Application中)
     * @param showPlaceHolder 加载时是否显示占位图，默认true
     * @param width 用于oss缩放图片，0表示加载原图, 默认加载原图
     * @param height 用于oss缩放图片，0表示加载原图 默认加载原图
     */
    @SuppressLint("CheckResult")
    fun displayImage(with: Any, url: Any, target: ImageView, showPlaceHolder: Boolean, width: Int, height: Int, params: String) {
        val request = (with(with) ?: return).load(if (url is String) getOSSUrl(url, width, height, params) else url)
        if (showPlaceHolder)
//            request.placeholder(R.drawable.image_default)
//            request.placeholder(R.drawable.vshow_placeholder)
//        request.error(R.drawable.image_default)
        request.transition(crossFadeTransition).into(target)
    }

    fun displayImage(with: Any, url: Any, target: ImageView, showPlaceHolder: Boolean) = displayImage(with, url, target, showPlaceHolder, 0, 0, "")

    fun displayImage(with: Any, url: Any, target: ImageView, width: Int, height: Int) = displayImage(with, url, target, true, width, height, "")

    fun displayImage(with: Any, url: Any, target: ImageView) = displayImage(with, url, target, true, 0, 0, "")

    fun displayAnimatedWebp(with: Any, url: Any, target: ImageView) = with(with)?.load(if (url is String) getFullUrl(url) else url)?.dontAnimate()?.into(target)

    fun loadImage(with: Any, url: Any, width: Int, height: Int, params: String, callback: (bitmap: Bitmap?) -> Unit) = with(with)?.asBitmap()?.load(if (url is String) getOSSUrl(url, width, height, params) else url)?.dontAnimate()?.into(object : CustomTarget<Bitmap>() {

        override fun onLoadCleared(placeholder: Drawable?) = Unit

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) = callback.invoke(resource)

        override fun onLoadFailed(errorDrawable: Drawable?) = callback.invoke(null)

    })

    fun loadImage(with: Any, url: Any, width: Int, height: Int, callback: (bitmap: Bitmap?) -> Unit) = loadImage(with, url, width, height, "", callback)

    fun loadImage(with: Any, url: Any, callback: (bitmap: Bitmap?) -> Unit) = loadImage(with, url, 0, 0, callback)


    fun loadAnimatedWebp(with: Any, url: Any, callback: (drawable: WebpDrawable?) -> Unit) = with(with)?.load(if (url is String) getFullUrl(url) else url)?.dontAnimate()?.into(object : CustomTarget<Drawable>() {

        override fun onLoadCleared(placeholder: Drawable?) = Unit

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) = callback.invoke(resource as WebpDrawable)

        override fun onLoadFailed(errorDrawable: Drawable?) = callback.invoke(null)

    })

    private fun getFullUrl(url: String) = if (url.contains("://") || File(url).exists()) url else "$url"

    private fun getOSSUrl(url: String, width: Int, height: Int, params: String): String {
        val sb = StringBuilder()
        sb.append(getFullUrl(url))
        if ((width > 0 || height > 0) && sb.startsWith("http", true)) {
            sb.append("?x-oss-process=image/resize")
            if (height > 0)
                sb.append(",h_").append(height)
            if (width > 0)
                sb.append(",w_").append(width)
        } else if (params.isNotEmpty())
            sb.append("?x-oss-process=image")
        if (params.isNotEmpty())
            sb.append(if (params.startsWith("/")) params else "/$params")
        return sb.toString()
    }


    private fun with(with: Any): RequestManager? = when (with) {
        is Activity -> Glide.with(with)
        is Fragment -> Glide.with(with)
        is View -> Glide.with(with)
        is Context -> Glide.with(with)
        else -> null
    }

}