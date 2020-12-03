package com.jiandanlangman.htmltextview.demo

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.jiandanlangman.htmltextview.EmotionDrawableProvider
import com.jiandanlangman.htmltextview.HTMLTextView
import com.jiandanlangman.htmltextview.ImageGetter
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    val text = "<base drawable=\"left:1234;top:1234;right:1234;bottom:1234;padding:8;left-action:2222;top-action:2222;right-action:2222;bottom-action:2222\" background=\"stroke:#FF4D81;stroke-width:2;stroke-dash:8dp;stroke-gap:4dp;radius:8dp;gradient:linear;gradient-colors:#FF0000,#00FF00,#0000FF;gradient-angle:135\" action=\"我是View本身\" style=\"pressed-scale:.98;margin:16dp;padding:16dp;line-height:1.3\"/><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" />你好呀<a action=\"你好\" style=\"color:#FF0000;font-weight:bold;text-align:center;pressed-scale:.88;pressed-tint:#FFFF00;width:28;height:12;font-size:20;padding-left:8dp;padding-right:8dp;padding-top:4dp;padding-bottom:4dp;margin:4dp\" background=\"fill:#FFA940;radius:4dp\">我是超链接</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈"
    val text2 =  "<base style=\"font-size:24sp;\" /><img/>哈哈\uD83D\uDE01\uD83D\uDE01\uD83D\uDE01哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！"

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
        val imageGetter = object : ImageGetter {
            override fun getImageDrawable(src: String, type: String, callback: (result: Drawable?) -> Unit) {
                if("1234" == src) {
                    val drawable = ContextCompat.getDrawable(this@MainActivity, R.mipmap.ic_launcher_round)
                    callback.invoke(drawable)
//                    ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
//                        callback.invoke(it)
//                        it?.loopCount = -1
//                        it?.start()
//                    }
                } else if("4567" == src) {
                    callback.invoke(null)
                } else if (type == "") {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    callback.invoke(NinePatchDrawable(resources, bitmap, bitmap.ninePatchChunk, Rect(), null))
                } else
                    ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
                        callback.invoke(it)
                    }
            }

        }


        HTMLTextView.setImageGetter(imageGetter)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = Adapter()
        HTMLTextView.setEmotionDrawableProvider(object : EmotionDrawableProvider {
            override fun isEmotionDrawable(text: String) = text == "\uD83D\uDE01"

            override fun getEmotionDrawable(text: String, callback: (drawable: Drawable?) -> Unit) {
                ImageLoader.loadAnimatedWebp(this@MainActivity, R.drawable.emoji) {
                    callback.invoke(it)
                }
            }

        })


//        val textView = findViewById<HTMLTextView>(R.id.textView)
//        textView.text = "<img src=\"1234\" action=\"1233\" style=\"width:32;height:32;pressed:scale\" />哈哈<a action=\"1233\" style=\"color:#FF4D81;font-weight:bold;pressed:scale;text-align:center;padding-left:16;padding-right:8;padding-top:0;padding-bottom:8;color:#FFA940;margin:0;font-size:24;text-align:top\" background=\"radius:8;fill:#FF4D81\">哈哈哈哈</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈"
    }

    private inner class ViewHolder(itemView:HTMLTextView):RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { v, action ->
                Log.d("MainActivity", "onClick:$action")
            }
        }
    }

    private inner class Adapter:RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(HTMLTextView(this@MainActivity))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder.itemView as HTMLTextView).text = text2
        }

        override fun getItemCount() = 1000

    }

}