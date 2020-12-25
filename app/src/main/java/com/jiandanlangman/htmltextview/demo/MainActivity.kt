package com.jiandanlangman.htmltextview.demo

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.NinePatch
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiandanlangman.htmltextview.HTMLTextView
import com.jiandanlangman.htmltextview.ResourcesProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {

//    val text2 = "<base drawable=\"top:1234;\" background=\"stroke:#FF4D81;stroke-width:2;stroke-dash:8dp;stroke-gap:4dp;radius:8dp;gradient:linear;gradient-colors:#FF0000,#00FF00,#0000FF;gradient-angle:135\" action=\"我是View本身\" style=\"pressed-scale:.98;margin:16dp;padding:16dp;line-height:1.3\"/><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" /><img src=\"\" />你好呀<a action=\"你好\" style=\"color:#FF0000;font-weight:bold;text-align:center;pressed-scale:.88;pressed-tint:#FFFF00;width:28;height:12;font-size:20;padding-left:8dp;padding-right:8dp;padding-top:4dp;padding-bottom:4dp;margin:4dp\" background=\"fill:#FFA940;radius:4dp\">我是超链接</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈"
//    val text2 =  "<base style=\"font-size:16sp;line-height:1.3\"></base><img style=\"width:38dp;height:16dp\"/>尽管<space count=\"4\"/>算法不是很明显，但还是可以通过位屏蔽来查找尾数\uD83D\uDE01\uD83D\uDE01\uD83D\uDE01哈哈哈，大傻逼！<img style=\"width:38dp;height:16dp\"/>尾数由，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，<a style=\"color:#FF0000\"><font style=\"color:#00FF00\">噶嘎嘎</font>！噶嘎嘎</a>，大傻逼！哈哈哈，大傻逼！哈哈哈，大傻逼！哈哈哈，<img style=\"width:38dp;height:16dp\"/>要提取位！"
//    val text2 = "<base action='{\"action\":\"user\\/homepage\",\"params\":{\"id\":\"10460\"}}' background=\"fill:#33000000;radius:9\"><space count=\"1\"></space><img src=\"assets/api/user/rich/level_0.webp\" alt=\"\" style=\"width:38dp;height:16dp;margin-right:2dp\"></img><space count=\"1\"></space><font style=\"&quot;color:#FF94D2\">丢雷楼木</font><space count=\"1\"></space><font style=\"&quot;color:#FF94D2\">闪亮登场！</font></base>"
//    val text2 = "<base style=\"font-size:16;padding-left:32dp;padding-right:32dp;padding-top:24dp;padding-bottom:24dp;line-height:1.2\" background=\"drawable:bbbb;padding:16\"><a background=\"drawable:1111\" style=\"padding-left:4;padding-right:4;padding-top:2dp;padding-bottom:2dp;color:#00FF00;font-size:9;text-align:center;width:52;height:14;text-align:right;font-weight:bold;margin-right:16;pressed-scale:.8;pressed-tint:#00FF00\" action=\"dddd\">粉粉粉</a>尾泥马\uD83D\uDE01\uD83D\uDE01了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁泥马了隔壁<a background=\"drawable:1111\" style=\"padding-left:18dp;padding-right:4dp;padding-top:2dp;padding-bottom:2dp;color:#FFFFFF;font-size:9;text-align:center;width:52;height:14;font-weight:bold\">粉2</a><img background=\"fill:#FFFF00;radius:8\" style=\"width:0;height:14;padding:0;pressed-scale:.8;pressed-tint:#00FF00;\" action=\"123\"/>我我我</base>"
//    val text2 = "<base action='{\"action\":\"url\",\"params\":{\"link\":\"http:\\/\\/test-m.liaoke.tv\\/h5\\/new_training?title=新人主播 培训计划\\u0026timeout=5000\\u0026transparent=0\\u0026show_progress=1\\u0026show_title_bar=0\\u0026error_close_page=1\\u0026enable_pull_to_refresh=0\"}}' background=\"fill:#33000000;radius:9;font-size:16;\" style=\"width:228;color:#111111;font-size:14;drawable:assets/api/girl_manager/training1215/qipaokuang.9\"><img src=\"assets/api/girl_manager/training1215/guanfangxiaoxi-banner.png\" alt=\"\" style=\"width:228;height:120\"></img><span>点击领取聊客速配速成培训文档</span><span style=\"color:#999999;font-size:12\">坚持7天</span><space count=\"2\"></space><span>收入突破300元/天</span></base>"
//    val text2 = " <base background=\"drawable:http://asset.liaoke.tv/assets/api/girl_manager/training1215/qipaokuang.9.png\" style=\"width:228;color:#111111;font-size:14\" action=\"点击跳转的地址\">\n" +
//        "        <img src=\"http://asset.liaoke.tv/assets/api/girl_manager/training1215/guanfangxiaoxi-banner.png\" style=\"width:228;height:120\"></img>\n" +
//        "        <span>点击领取聊客速配速成培训文档</span>\n" +
//        "        <span style=\"color:#999999;font-size:12\">坚持7天<space count=\"2\"></space>收入突破300元/天</span>\n" +
//        "        </base>"
//    val text2 = """
//        <base action='{"action":"url","params":{"link":"http:\/\/test-m.liaoke.tv\/h5\/new_training?title=%E6%96%B0%E4%BA%BA%E4%B8%BB%E6%92%AD%20%E5%9F%B9%E8%AE%AD%E8%AE%A1%E5%88%92\u0026timeout=5000\u0026transparent=0\u0026show_progress=1\u0026show_title_bar=1\u0026error_close_page=1\u0026enable_pull_to_refresh=0\u0026wx=sijiawe0909\u0026wx_qrcode=girl_manager%2Fwx_qrcode%2F1eecb00a-47e1-419a-9f71-d5c4198625a7%21300x300\u0026app_name=%E8%81%8A%E5%AE%A2\u0026uid=12616\u0026manager_nickname=%E5%A5%94%E6%B3%A2%E5%84%BF%E7%81%9E"}}' background="fill:#33FF0000;radius:16" style="width:228;color:#111111;font-size:14;text-align:left,top;line-height:1.1;padding-bottom:10"><img src="https%3a%2f%2fasset.liaoke.tv%2fassets%2fapi%2fgirl_manager%2ftraining1215%2fguanfangxiaoxi-banner.png" style="width:228;height:120;text-align:top;margin-bottom:10;span-line:1"></img><font style="margin-left:16;font-size:14">点击领取聊客速配速成培训文档</font><br count="1"></br><span style="color:#999999;font-size:12;margin-left:16">坚持7天 收入突破300元/天</span></base>
//    """.trimIndent()

    val text2 = "<base background=\"fill:#33FF0000;radius:8\" style=\"width:228;color:#111111;font-size:14;text-align:left,top;line-height:1.1;padding-bottom:10\"><img src=\"https%3a%2f%2fasset.liaoke.tv%2fassets%2fapi%2fgirl_manager%2ftraining1215%2fguanfangxiaoxi-banner.png\" style=\"width:228;height:120;text-align:top;margin-bottom:10\"></img><font style=\"margin-left:16;\">点击领取聊客速配速成培训文档</font><br count=\"1\"></br><span style=\"color:#999999;font-size:12;margin-left:16\">坚持7天 收入突破300元/天</span><br></br><font style=\"color:#999999;line-height:1\">群公告</font>" +
            "<font>内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容</font></base>"
//    val text2 = """
//        <base background="fill:#33000000;radius:9;font-size:16;drawable:https%3a%2f%2fasset.liaoke.tv%2fassets/api/user/rich/qipaokuang_shoufu.9.png" action="3333" style="pressed-scale:.8" >
//            <img src="https%3a%2f%2fasset.liaoke.tv%2fassets/api/pk/zbj_fy_df.png?v=20201217" action="1" style="width:14;height:14;margin-right:2;pressed-scale:0.8"></img>
//            <img src="https%3a%2f%2fasset.liaoke.tv%2fassets/api/user/rich/level_1.webp" action="2" style="width:35;height:14;margin-right:2;pressed-scale:0.8"></img>
//            <span background="drawable:https%3a%2f%2fasset.liaoke.tv%2fassets/api/role_ext/fuhaobang_bg.png?v=20201217" style="padding-left:31;padding-right:2;padding-top:2;padding-bottom:2;color:#FFFFFF;font-size:9;text-align:center;width:44;height:14;margin-right:2;font-family:century-gothic-bold">8</span>
//            <img src="https%3a%2f%2fasset.liaoke.tv%2fassets/api/wealth_level/caifu_level_84.png?v=20201217" action="3" style="width:32;height:14;margin-right:2;pressed-scale:0.8"></img>
//            <img src="https%3a%2f%2fasset.liaoke.tv%2fassets/api/livel_level/zhibo_level_13.png?v=20201217" action="4" style="width:32;height:14;margin-right:2;pressed-scale:0.8"></img>
//            <span background="drawable:https%3a%2f%2fasset.liaoke.tv%2fassets/api/fans_club/card/tab_fst_79.png?v=20201217" style="padding-left:19;padding-right:2;padding-top:2;padding-bottom:2;color:#FFFFFF;font-size:9;text-align:center;width:52;height:14;margin-right:2">本哈哈</span>
//            <img src="https%3a%2f%2fasset.liaoke.tv%2fassets/api/vip/icon_vip_1.png?v=20201217" action="5" style="width:16;height:16;margin-right:2;pressed-scale:0.8"></img>
//            <font action='{"action":"user\/homepage","params":{"id":"14665"}}' style="color:#99D5FF;pressed-scale:.8">我应该从何说</font>
//            <space count="2"></space>
//            <font>发言消息发言消息发言消息发言消息</font>
//            <font>你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦你没得哦</font>
//        </base>
//    """.trimIndent()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val t = "http://asset.xxx.xxx/a/a.9.webp?v2"
        val webpSrcRegex = Regex("^[\\S\\s]*.[Ww][Ee][Bb][Pp]($|([\\\\?][\\S\\s]*$))")


        Log.d("MainActivity", "match:${webpSrcRegex.matches(t)}")

        val iss = assets.open("tab_fst_82.png")
        val file = File(cacheDir, "tab_fst_82.png")
        val fos = FileOutputStream(file)
        val buffer = ByteArray(8196)
        var readLength = 0
        while (iss.read(buffer).also { readLength = it } != -1)
            fos.write(buffer, 0, readLength)
        fos.close()
        iss.close()
        setContentView(R.layout.activity_main)


        val provider = object : ResourcesProvider {
            override fun getImageDrawable(target:HTMLTextView, src: String, callback: (result: Drawable?) -> Unit) {

                ImageLoader.loadImage(this@MainActivity, URLDecoder.decode(src, "UTF-8")) {
                    callback.invoke(BitmapDrawable(resources, it))
                }
                if("1234" == src) {

//                   Thread {
//                       SystemClock.sleep(2000)
//                       runOnUiThread {
//                           val drawable = ContextCompat.getDrawable(this@MainActivity, R.mipmap.ic_launcher_round)
//                           callback.invoke(drawable) }
//                   }.start()
//                    ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
//                        callback.invoke(it)
//                        it?.loopCount = -1
//                        it?.start()
//                    }
                }
//                } else if("4567" == src) {
//                    callback.invoke(null)
//                } else if("bbbb" == src) {
//                    val iss = assets.open("qipaokuang_xiaozi.9.png")
//                    val file = File(cacheDir, "qipaokuang_xiaozi.9.png")
//                    val fos = FileOutputStream(file)
//                    val buffer = ByteArray(8196)
//                    var readLength = 0
//                    while (iss.read(buffer).also { readLength = it } != -1)
//                        fos.write(buffer, 0, readLength)
//                    fos.close()
//                    iss.close()
//                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//                    val ninePatchChunk = bitmap.ninePatchChunk
//                    if(NinePatch.isNinePatchChunk(ninePatchChunk))
//                        callback.invoke(NinePatchDrawable(resources, bitmap, ninePatchChunk, Rect(), null))
//                    else
//                        callback.invoke(null)
//                }else if("1111" == src) {
//                    callback.invoke(BitmapDrawable(resources, BitmapFactory.decodeFile(file.absolutePath)))
//                }else
//                    ImageLoader.loadAnimatedWebp(this@MainActivity, "https://asset.liaoke.tv/assets/api/user/rich/level_1.webp") {
//                        callback.invoke(it)
//                    }
            }

            override fun getTypeface(name: String): Typeface? {
                return null
            }

            override fun isEmotionDrawable(text: String) = text == "\uD83D\uDE01"

            override fun getEmotionDrawable(text: String, callback: (drawable: Drawable?) -> Unit) {
                ImageLoader.loadAnimatedWebp(this@MainActivity, R.drawable.emoji) {
                    callback.invoke(it)
                }
            }

        }

        HTMLTextView.setResourcesProvider(provider)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = Adapter()



//        val textView = findViewById<HTMLTextView>(R.id.textView)
//        textView.text = "<img src=\"1234\" action=\"1233\" style=\"width:32;height:32;pressed:scale\" />哈哈<a action=\"1233\" style=\"color:#FF4D81;font-weight:bold;pressed:scale;text-align:center;padding-left:16;padding-right:8;padding-top:0;padding-bottom:8;color:#FFA940;margin:0;font-size:24;text-align:top\" background=\"radius:8;fill:#FF4D81\">哈哈哈哈</a>哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈"
    }

    private inner class ViewHolder(itemView:FrameLayout):RecyclerView.ViewHolder(itemView) {

        val textView = itemView.getChildAt(0) as HTMLTextView

        init {
//            itemView.setOnClickListener { v, action ->
//                Log.d("MainActivity", "onClick:$action")
//            }
            itemView.isFocusable = true
            itemView.isClickable = true
            itemView.setOnClickListener {
                Toast.makeText(this@MainActivity, "onClick", Toast.LENGTH_LONG).show()
            }
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
    }

    private inner class Adapter:RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val frameLayout = FrameLayout(this@MainActivity)
            frameLayout.addView(HTMLTextView(this@MainActivity), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            return ViewHolder(frameLayout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           holder.textView.text = HTMLTextView.fromHTML(this@MainActivity, "$text2$position")
        }

        override fun getItemCount() = 1000

    }

}