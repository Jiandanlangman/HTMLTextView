package com.jiandanlangman.htmltextview.demo

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.jiandanlangman.htmltextview.HTMLTextView
import com.jiandanlangman.htmltextview.ImageGetter
import com.jiandanlangman.htmltextview.ImageLoader

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.textView)
        HTMLTextView.setImageGetter(object : ImageGetter {
            override fun getImageDrawable(src: String, callback: (result: Drawable?) -> Unit) {
                ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
                    callback.invoke(it)
                    it?.loopCount = -1
                    it?.start()
                }
            }

        })
        textView.text = "你好<img src=\"\" style=\"width:44;height:12;padding-left:8;padding-right:8;pressed:scale\" /><span>你好呀<a action=\"你好\" style=\"color:#FFFF00;font-weight:bold;text-align:center;pressed:scale;padding-left:8;padding-right:8\">我是超链接</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈</span>"
    }
}