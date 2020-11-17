package com.jiandanlangman.htmltextview.demo

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.jiandanlangman.htmltextview.HTMLTextView
import com.jiandanlangman.htmltextview.ImageGetter
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val iss = assets.open("test.9.png")
        val file = File(cacheDir, "test.9.png")
        val fos = FileOutputStream(file)
        val buffer = ByteArray(8196)
        var readLength = 0
        while (iss.read(buffer).also { readLength = it } != -1)
            fos.write(buffer, 0, readLength)
        fos.close()
        iss.close()
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.textView)
        HTMLTextView.setImageGetter(object : ImageGetter {
            override fun getImageDrawable(src: String, type: String, callback: (result: Drawable?) -> Unit) {
                if (type == "") {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    callback.invoke(NinePatchDrawable(resources, bitmap, bitmap.ninePatchChunk, Rect(), null))
                } else
                    ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
                        callback.invoke(it)
                        it?.loopCount = -1
                        it?.start()
                    }
            }

        })
        textView.text = "你好<img src=\"\" style=\"width:44;height:12;padding-left:8;padding-right:8;pressed:scale\" /><img src=\"\" style=\"width:44;height:12;padding-left:8;padding-right:8;pressed:scale\" />你好呀<a action=\"你好\" style=\"color:#FFFF00;font-weight:bold;text-align:center;pressed:scale;padding-left:8;padding-right:8\">我是超链接</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈<view background=\"stroke:#FF4D81;stroke-width:2;stroke-dash:4;stroke-gap:2;file:#99000000;radius:8;gradient:linear;gradient-colors:#FF0000,#00FF00,#0000FF;gradient-angle:135;drawable:xxx.9.png\" action=\"123\" style=\"font-size:20;pressed:scale;margin-left:16\"/>"
    }
}