package com.gospell.stall.util

import com.amap.api.mapcore.util.`is`
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
        fun get(url: String, param: Map<String, Any?>?, callback: Callback) {
            var url = url
            if (param != null && param.isNotEmpty()) {
                val buffer = StringBuffer()
                if (url.indexOf("?") == -1) {
                    buffer.append("?")
                } else {
                    buffer.append("&")
                }
                param.forEach(BiConsumer { key: String?, value: Any? ->
                    buffer.append(key)
                    buffer.append("=")
                    buffer.append(value.toString())
                    buffer.append("&")
                })
                buffer.deleteCharAt(buffer.length - 1)
                url += buffer.toString()
            }
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val builder = Request.Builder().url(url)
            builder.method("GET", null)
            val call = client.newCall(builder.build())
            call.enqueue(callback)
        }

        open fun get(url: String, callback: Callback) {
            get(url, null, callback)
        }

        /**
         * @Author peiyongdong
         * @Description ( post请求 )
         * @Date 11:28 2020/3/20
         * @Param [url, param, listener]
         * @return void
         */
        fun post(url: String, param: MutableMap<String, Any>?, callback: Callback) {
            val formBody = FormBody.Builder()
            param?.forEach { (key: String, value: Any) ->
                if (value != null) {
                    formBody.add(key, value.toString())
                }
            }
            val body: RequestBody = formBody.build()
            val request = Request.Builder().post(body).url(url!!).build()
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val call = client.newCall(request)
            call.enqueue(callback)
        }

        fun post(url: String, callback: Callback) {
            post(url, null, callback)
        }

        fun uploadFile(url: String, contentType: MediaType, file: File, callback: Callback) {
            var fileBody = file.asRequestBody(contentType)
            var multipartBody = MultipartBody.Builder().addFormDataPart("file", file.name, fileBody).build()
            val request = Request.Builder().post(multipartBody).url(url!!).build()
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val call = client.newCall(request)
            call.enqueue(callback)
        }

        /**
         * 下载文件，downloadLength：已下载文件长度，contentLength：文件总长度
         */
        fun downloadFile(url: String,downloadLength:Long,callback: Callback){
            val request = Request.Builder() //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .addHeader("RANGE", "bytes=$downloadLength-")
                .url(url)
                .build()
            val client = OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build()
            val call = client.newCall(request)
            call.enqueue(callback)
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