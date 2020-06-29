package com.gospell.stall.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.util.Log
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.gospell.stall.Constants
import com.gospell.stall.entity.StallInfo
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class RequestHelper {
    private constructor()
    private var spinKitView: SpinKitView? = null
    var loadDialog:LoadDialog? = null
    private var reqHandler = Handler(Handler.Callback { msg: Message ->
        if(loadDialog!=null){
            when(msg.what){
                ERROR->{
                    var message = msg.obj.toString()
                    loadDialog!!.setResultMessage(message).setLoadMessage(null).setCanCanceled(true).show()
                }
                FAIL->{
                    loadDialog!!.setResultMessage("连接服务器失败！").setCanCanceled(true).setLoadMessage(null).show()
                }

                SUCCESS->{
                    loadDialog?.dismiss()
                }
            }
        }
        true
    })
    companion object {
        const val TAG = "RequestHelper"
        private var mContext: Context? = null
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
    fun get(url: String, param: Any?,loadMessage:String?, callBack: (result: String?) -> Unit) {
        var map = mutableMapOf<String, Any>()
        var message = reqHandler.obtainMessage()
        try {
            if (param !is Map<*, *>) {
                map.putAll(JsonUtil.stringToCollect<String, Any>(JsonUtil.toJson(param)) as MutableMap)
            }
            loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
            loadDialog!!.show()
            HttpUtil.get(url,map,object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper",e.message,e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }
                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(TAG,"post请求:$url,参数:$map,\nResult=$result")
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG,e.message,e)
        }
    }
    fun post(url: String, param: Any?,loadMessage:String?, callBack: (result: String?) -> Unit) {
        var map = mutableMapOf<String, Any>()
        var message = reqHandler.obtainMessage()
        try {
            if (param !is Map<*, *>) {
                map.putAll(JsonUtil.stringToCollect<String, Any>(JsonUtil.toJson(param)) as MutableMap)
            } else {
                map = param as MutableMap<String, Any>
            }
            if(!map.containsKey("token")){
                map["token"] = Constants.token.toString()
            }
            loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
            loadDialog!!.show()
            HttpUtil.post(url,map,object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper",e.message,e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }
                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(TAG,"post请求:$url,参数:$map,\nResult=$result")
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG,e.message,e)
        }
    }
    fun updateFile(url: String, contentType: MediaType, file: File,loadMessage:String?, callBack: (result: String?) -> Unit) {
        var message = reqHandler.obtainMessage()
        loadDialog = LoadDialog(mContext!!).setLoadMessage(loadMessage).setSprite(ThreeBounce())
        loadDialog!!.show()
        try {
            HttpUtil.uploadFile(url, contentType, file, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper",e.message,e)
                    message.what = FAIL
                    reqHandler.sendMessage(message)
                }
                override fun onResponse(call: Call, response: Response) {
                    var result = response.body?.string()
                    Log.d(TAG,"文件上传ulr=:$url," +
                            "文件名:${file.name}," +
                            "文件大小:${file.length()}," +
                            "文件类型:$contentType," +
                            "\nResult=$result")
                    message.what = SUCCESS
                    reqHandler.sendMessage(message)
                    callBack.invoke(result)
                }
            })
        } catch (e: Exception) {
            message.what = ERROR
            message.obj = e.message
            reqHandler.sendMessage(message)
            Log.e(TAG,e.message,e)
        }
    }
    fun uploadImage(imageFile:File,loadMessage: String?,callBack: (fileUploadedUrl: String) -> Unit){
        "image/jpg".toMediaTypeOrNull()?.let {
            updateFile(Constants.avatarUploadUrl, it,imageFile!!, loadMessage) { result ->
                var json = JSONObject(result)
                if (json!!.getBoolean("success")) {
                    var fileUploadedUrl = json.getString("data")
                    callBack.invoke(fileUploadedUrl)
                }else{
                    ToastUtil.makeText(mContext,"图片上传失败!errorMsg:${json.getString("msg")}")
                }
            }
        }
    }
    fun loadImage(url: String,callBack: (bitmap: Bitmap?) -> Unit){
        var map = mutableMapOf<String, Any>()
        map["token"] = Constants.token.toString()
        try {
            HttpUtil.get(url,map,object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RequestHelper",e.message,e)
                }
                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG,"post请求:$url,参数:$map")
                    var bitmap = BitmapFactory.decodeStream(response.body!!.byteStream())
                    callBack.invoke(bitmap)
                }
            })
        } catch (e: Exception) {
            ToastUtil.makeText(mContext,e.message!!)
        }
    }
}