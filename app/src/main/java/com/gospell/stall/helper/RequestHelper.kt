package com.gospell.stall.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.FileUtil
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class RequestHelper {
    private constructor()

    private var spinKitView: SpinKitView? = null
    var loadDialog: LoadDialog? = null
    private var reqHandler = Handler(Handler.Callback { msg: Message ->
        if (loadDialog != null) {
            when (msg.what) {
                ERROR -> {
                    var message = msg.obj.toString()
                    loadDialog!!.setResultMessage(message).setLoadMessage(null).setCanCanceled(true)
                        .show()
                }
                FAIL -> {
                    loadDialog!!.setResultMessage("连接服务器失败！").setCanCanceled(true)
                        .setLoadMessage(null).show()
                }

                SUCCESS -> {
                    loadDialog?.dismiss()
                }
            }
        }
        true
    })

    companion object {
        const val TAG = "RequestHelper"
        private lateinit var mContext: Context
        private var helper: RequestHelper? = null
        const val ERROR = 0
        const val SUCCESS = 1
        const val FAIL = 2
        fun getInstance(context: Context): RequestHelper {
            mContext = context
            if (helper == null) {
                synchronized(RequestHelper::class.java) {
                    if (helper == null) {
                        helper = RequestHelper()
                    }
                }
            }
            return helper!!
        }
    }

    fun get(url: String, param: Any?, loadMessage: String?, callBack: (result: String?) -> Unit) {
        var map = mutableMapOf<String, Any>()
        var message = reqHandler.obtainMessage()
        try {
            if (param != null && param !is Map<*, *>) {
                map.putAll(JsonUtil.stringToCollect<String, Any>(JsonUtil.toJson(param)) as MutableMap)
            } else if (param != null) {
                map = param as MutableMap<String, Any>
            }
            if (!map.containsKey("token") && !Constants.token.isNullOrBlank()) {
                map["token"] = Constants.token.toString()
            }
            loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
            loadDialog!!.show()
            HttpUtil.get(url, map, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper", e.message, e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }

                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(TAG, "post请求:$url,参数:$map,\nResult=$result")
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG, e.message, e)
        }
    }

    fun post(url: String, param: Any?, loadMessage: String?, callBack: (result: String?) -> Unit) {
        var map = mutableMapOf<String, Any>()
        var message = reqHandler.obtainMessage()
        try {
            if (param != null && param !is Map<*, *>) {
                map.putAll(JsonUtil.stringToCollect<String, Any>(JsonUtil.toJson(param)) as MutableMap)
            } else if (param != null) {
                map = param as MutableMap<String, Any>
            }
            if (!map.containsKey("token") && !Constants.token.isNullOrBlank()) {
                map["token"] = Constants.token.toString()
            }
            loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
            loadDialog!!.show()
            HttpUtil.post(url, map, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper", e.message, e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }

                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(TAG, "post请求:$url,参数:$map,\nResult=$result")
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG, e.message, e)
        }
    }

    fun updateFile(
        url: String,
        contentType: MediaType,
        file: File,
        loadMessage: String?,
        callBack: (result: String?) -> Unit
    ) {
        var message = reqHandler.obtainMessage()
        loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
        loadDialog!!.show()
        try {
            HttpUtil.uploadFile(url, contentType, file, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper", e.message, e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }
                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(
                        TAG, "文件上传ulr=:$url," +
                                "文件名:${file.name}," +
                                "文件大小:${file.length()}," +
                                "文件类型:$contentType," +
                                "\nResult=$result"
                    )
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG, e.message, e)
        }
    }

    fun uploadImage(
        imageFile: File,
        loadMessage: String?,
        callBack: (fileUploadedUrl: String) -> Unit
    ) {
        "image/jpg".toMediaTypeOrNull()?.let {
            updateFile(Constants.avatarUploadUrl, it, imageFile!!, loadMessage) { result ->
                var json = JSONObject(result)
                if (json!!.getBoolean("success")) {
                    var fileUploadedUrl = json.getString("data")
                    callBack.invoke(fileUploadedUrl)
                } else {
                    ToastUtil.makeText(mContext, "图片上传失败!errorMsg:${json.getString("msg")}")
                }
            }
        }
    }

    fun loadImage(url: String, callBack: (bitmap: Bitmap?) -> Unit) {
        var map = mutableMapOf<String, Any>()
        map["token"] = Constants.token.toString()
        try {
            HttpUtil.get(url, map, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper", e.message, e)
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "post请求:$url,参数:$map")
                    var bitmap = BitmapFactory.decodeStream(response.body!!.byteStream())
                    callBack.invoke(bitmap)
                }
            })
        } catch (e: Exception) {
            ToastUtil.makeText(mContext, e.message!!)
        }
    }
    fun downloadFile(url: String,title: String,fileName:String,fileSize:Long, callBack: (apkFile: File) -> Unit){
        var dialog = LoadDialog(mContext)
        var pause = false
        var file = FileUtil.createFile(mContext,fileName,"apk")
        var downloadLength = file.length()
        //上次下载时间
        var lastDownTime:Long = 0
        //1秒钟内下载的总量
        var lastDownloadCount:Long = 0
        dialog.createProgressDialog(title,fileName,"${String.format("%.1f",fileSize.toDouble()/1024/1024)}Mb",View.OnClickListener {
                pause = !pause
                if(pause){
                    (it as ImageView).setImageResource(R.drawable.ic_status_play)
                }else{
                    (it as ImageView).setImageResource(R.drawable.ic_status_pause)
                }
            })
            .show()
        var handler = Handler(Handler.Callback { msg ->
            when(msg.what){
                0->{//开始下载
                    lastDownTime = System.currentTimeMillis()
                    var progress = (downloadLength*100/fileSize).toInt()
                    lastDownloadCount = downloadLength
                    dialog.setProgress(progress){views ->
                        (views[0] as TextView).text = "进度:${progress}%"
                        (views[1] as TextView).text = "下载速度:0M/s"
                    }
                }
                1->{//正在下载
                    //进度
                    var progress = (downloadLength*100/fileSize).toInt()
                    dialog.setProgress(progress){views ->
                        //当前时间
                        var now = System.currentTimeMillis()
                        var second = (now - lastDownTime)/1000
                        //下载速度
                        if(second>=1){
                            var secondDownloadCount = downloadLength - lastDownloadCount
                            (views[0] as TextView).text = "进度:${progress}%"
                            var rate =(secondDownloadCount.toDouble()/1024/1024)/second
                            var rateStr = "当前下载速度:"
                            rateStr += if(rate>=1){
                                "${String.format("%.1f",rate)}M/s"
                            }else{
                                "${String.format("%.1f",rate*1024)}kb/s"
                            }
                            (views[1] as TextView).text = rateStr
                            lastDownTime = System.currentTimeMillis()
                            lastDownloadCount = downloadLength
                        }
                    }
                }
                2->{//暂停下载
                    lastDownTime = System.currentTimeMillis()
                }
                3->{//下载取消
                    dialog.dismiss()
                    LoadDialog(mContext).setResultMessage("${fileName}已取消下载!").setCanCanceled(true).show()
                }
                4->{//下载完成
                    dialog.dismiss()
                    callBack.invoke(file)
                }
                5->{//下载错误
                    dialog.dismiss()
                    LoadDialog(mContext).setResultMessage("下载${fileName}时发生错误!").setCanCanceled(true).show()
                }
            }
            true
        })
        var message = handler.obtainMessage()
        message.what = 0
        handler.sendMessage(message)
        HttpUtil.downloadFile(url,downloadLength,object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RequestHelper", e.message, e)
                var message = handler.obtainMessage()
                message.what = 5
                reqHandler.sendMessage(message)
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "下载请求:$url")
                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null
                try {
                    inputStream = response.body!!.byteStream()
                    outputStream = FileOutputStream(file, true)
                    val buffer = ByteArray(2048) //缓冲数组2kB
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        while (pause){
                            var message = handler.obtainMessage()
                            message.what = 2
                            handler.sendMessage(message)
                            Thread.sleep(1000)
                        }
                        outputStream!!.write(buffer, 0, len)
                        downloadLength += len
                        var message = handler.obtainMessage()
                        message.what = 1
                        handler.sendMessage(message)
                    }
                    outputStream!!.flush()
                    var message = handler.obtainMessage()
                    message.what = 4
                    handler.sendMessage(message)
                }catch (e:Exception){
                    Log.e(TAG,e.message,e)
                    var message = handler.obtainMessage()
                    message.what = 5
                    handler.sendMessage(message)
                } finally {
                    //关闭IO流
                    try {
                        inputStream?.close()
                        outputStream?.close()
                    }catch (e:Exception){
                        Log.e(TAG,e.message,e)
                        var message = handler.obtainMessage()
                        message.what = 5
                        handler.sendMessage(message)
                    }
                }
            }
        })
    }
}