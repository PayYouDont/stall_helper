package com.gospell.stall.util

import android.content.Context
import android.os.Environment
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
    }
}