package com.gospell.stall.util

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

class HttpUtil {
    companion object {
        /**
         * @Author peiyongdong
         * @Description ( get请求 )
         * @Date 11:26 2020/3/20
         * @Param [url, param, listener]
         * @return void
         */
        open fun get(
            url: String,
            isSync: Boolean,
            param: Map<String?, String?>?,
            listener: (response: Response) -> Unit
        ) {
            var url = url
            if (param != null && param.isNotEmpty()) {
                val buffer = StringBuffer()
                if (url.indexOf("?") == -1) {
                    buffer.append("?")
                } else {
                    buffer.append("&")
                }
                param.forEach(BiConsumer { key: String?, value: String? ->
                    buffer.append(key)
                    buffer.append("=")
                    buffer.append(value)
                    buffer.append("&")
                })
                buffer.deleteCharAt(buffer.length - 1)
                url += buffer.toString()
            }
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val builder = Request.Builder().url(url)
            builder.method("GET", null)
            val call = client.newCall(builder.build())
            if (isSync) {
                listener.invoke(call.execute())
            } else {
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("HttpUtil.get()", e.message, e)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        //Log.d("HttpUtil.get()", response.message)
                        listener.invoke(response)
                    }
                })
            }
        }

        open fun get(url: String, isSync: Boolean, listener: (response: Response) -> Unit) {
            get(url!!, isSync, null, listener)
        }
        open fun get(url: String, listener: (response: Response) -> Unit) {
            get(url!!, false, null, listener)
        }
        open fun get(url: String,param: Map<String?, String?>?, listener: (response: Response) -> Unit) {
            get(url!!, false, param, listener)
        }
        /**
         * @Author peiyongdong
         * @Description ( post请求 )
         * @Date 11:28 2020/3/20
         * @Param [url, param, listener]
         * @return void
         */
        open fun post(
            url: String,
            isSync: Boolean,
            param: MutableMap<String, Any>?,
            listener: (response: Response) -> Unit
        ) {
            val formBody = FormBody.Builder()
            if (param!!.isNotEmpty()) {
                param.forEach { (key: String, value: Any) ->
                    if(value!=null){
                        formBody.add(key,value.toString())
                    }
                }
            }
            val body: RequestBody = formBody.build()
            val request = Request.Builder().post(body).url(url!!).build()
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val call = client.newCall(request)
            if (isSync) {
                listener.invoke(call.execute())
            } else {
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("HttpUtil.post()", e.message, e)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        listener.invoke(response)
                    }
                })
            }
        }

        fun post(url: String, isSync: Boolean, listener: (response: Response) -> Unit) {
            post(url, isSync, null, listener)
        }

        fun post(url: String, listener: (response: Response) -> Unit) {
            post(url, false, null, listener)
        }
        fun post(url: String,param: MutableMap<String, Any>?, listener: (response: Response) -> Unit) {
            post(url, false, param, listener)
        }
        /**
         * @Author peiyongdong
         * @Description ( 获取服务返回的文件名称，前提是服务器消息头中有filename字段 )
         * @Date 11:29 2020/3/20
         * @Param [response]
         * @return java.lang.String
         */
        fun getHeaderFileName(response: Response): String? {
            var dispositionHeader = response.header("Content-Disposition")
            if (!dispositionHeader.isNullOrEmpty()) {
                dispositionHeader!!.replace("attachment;filename=", "")
                dispositionHeader.replace("filename*=utf-8", "")
                val strings =
                    dispositionHeader.split("; ".toRegex()).toTypedArray()
                if (strings.size > 1) {
                    dispositionHeader = strings[1].replace("filename=", "")
                    dispositionHeader = dispositionHeader.replace("\"", "")
                    return dispositionHeader
                }
                return ""
            }
            return ""
        }
    }
}