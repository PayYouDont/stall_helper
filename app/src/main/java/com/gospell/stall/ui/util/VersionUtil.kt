package com.gospell.stall.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File


class VersionUtil {
   companion object{
       /**
        * 检查是否存在SDCard
        *
        * @return
        */
       fun hasSdcard(): Boolean {
           val state = Environment.getExternalStorageState()
           return state == Environment.MEDIA_MOUNTED
       }

       /**
        * 2 * 获取版本
        */
       fun getVersion(context: Context): Int {
           try {
               val manager = context.packageManager
               val info = manager.getPackageInfo(context.packageName, 0)
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                   return info.longVersionCode.toInt()
               }
               return info.versionCode
           } catch (e: Exception) {
               Log.e("VersionUtils", e.message,e)
           }
           return 0
       }

       /**
        * 2 * 获取版本名称
        */
       fun getVersionName(context: Context): String? {
           try {
               val manager = context.packageManager
               val info = manager.getPackageInfo(context.packageName, 0)
               return info.versionName
           } catch (e: Exception) {
               Log.e("VersionUtils", e.message,e)
           }
           return null
       }
       fun installApk(context:Context,apkFile:File){
           val intent = Intent(Intent.ACTION_VIEW)
           //判读版本是否在7.0以上
           if (Build.VERSION.SDK_INT >= 24) {
               //provider authorities
               val apkUri = FileProvider.getUriForFile(context, "com.gospell.stall.provider", apkFile)
               //Granting Temporary Permissions to a URI
               // 给目标应用一个临时授权
               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
               intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
           } else {
               intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
           }
           context.startActivity(intent)
       }
   }
}