package com.jack.kotlin.firstfluttermoduleinandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import io.flutter.facade.Flutter
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StringCodec
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var count = 0
    private var basicMessageChannelPluginReply: BasicMessageChannel.Reply<String>? = null
    private var eventSink: EventChannel.EventSink? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flutterView = Flutter.createView(this, lifecycle, "initialRoute")
        container.addView(flutterView)

        // BasicMessageChannel ,Android端和Flutter端相互发送消息，并且在收到消息后返回对方信息
        val basicMessageChannel = BasicMessageChannel<String>(
            flutterView,
            "BasicMessageChannelPlugin",
            StringCodec.INSTANCE
        )
        basicMessageChannel.setMessageHandler { message, reply ->
            this.basicMessageChannelPluginReply = reply
            setTextInfo("receive dart message: $message")
            reply.reply("native: 收到消息[$message]")
        }

        val methodChannel = MethodChannel(flutterView, "app.channel.shared.data")
        val eventChannel = EventChannel(flutterView, "EventChannelPlugin")

        // 接受dart调用
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "getSharedText" -> {
                    val i = count++
                    setTextInfo("send data :$i")
                    result.success("data from native: $i")
                }
            }
        }

        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onCancel(arguments: Any?) {

            }

            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                this@MainActivity.eventSink = events
                events?.success("[EventChannel]hello dart,I am native")
            }
        })

        btnClick.setOnClickListener {
            //  getPlatformFromDart(methodChannel)
            Handler().postDelayed({
                eventSink?.success("Click,[EventChannel]hello dart,I am native1")
            }, 500)
            Handler().postDelayed({
                eventSink?.success("Click,[EventChannel]hello dart,I am native2")
            }, 1000)
            Handler().postDelayed({
                eventSink?.endOfStream()
            }, 2000)
        }
    }

    // 通过 MethodChannel 调用 dart
    private fun getPlatformFromDart(methodChannel: MethodChannel) {
        methodChannel.invokeMethod("getPlatform", null, object : MethodChannel.Result {
            override fun notImplemented() {
                setTextInfo("未实现getPlatform方法")
            }

            override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                setTextInfo("errorCode=$errorCode; errorMessage=$errorMessage")
            }

            override fun success(result: Any?) {
                result?.apply {
                    setTextInfo(toString())
                }
            }
        })
    }

    private fun setTextInfo(charSequence: CharSequence) {
        tvTitle.text = charSequence
    }

    // native 把信息发送给 dart
    private fun sendTextToDart() {

    }
}
