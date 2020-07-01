package com.gospell.stall.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileUtil {
    companion object{
        fun getDiskCacheDir(context: Context, uniqueName: String): File? {
            val cachePath: String = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()) {
                context.externalCacheDir.path
            } else {
                context.cacheDir.path
            }
            return File(cachePath + File.separator.toString() + uniqueName)
        }
        //获取头像
        fun createFile(context: Context,filename: String,dirName:String): File {
            // 使用 APP 内部储存空间
            val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).absolutePath,dirName)
            if (!appDir.exists())
                appDir.mkdir()
            return File(appDir, filename)
        }
        fun getUriByOsVersion(context: Context,file: File): Uri {
            val currentApiVersion = android.os.Build.VERSION.SDK_INT
            return if (currentApiVersion < 24) {
                Uri.fromFile(file)
            } else {
                FileProvider.getUriForFile(context, "com.gospell.stall.provider", file)
            }
        }
    }
}