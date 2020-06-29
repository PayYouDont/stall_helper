package com.gospell.stall.util

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException


class PhotoUtil {
    companion object{
        private const val TAG = "PhotoUtil"
        const val request_camera_code = 1
        const val request_photo = 2
        const val request_crop = 3
        var cropPhotoPath:String? = null
        fun cropPhoto(fragment: Fragment, uri: Uri) {
            //intent隐式调用启动拍照界面
            val intent = Intent("com.android.camera.action.CROP")
            //设置需要裁剪的图片地址
            intent.setDataAndType(uri, "image/*")
            //通过put（key，value）方法设置相关属相
            intent.putExtra("crop", "true")
            //设置图片宽高比例
            intent.putExtra("aspectX", 1)
            intent.putExtra("aspectY", 1)
            //设置图片宽高
            intent.putExtra("outputX", 240)
            intent.putExtra("outputY", 240)
            //该属性设置为false表示拍照后不会将数据返回到onResluet方法中（建议设置为false，这样获取的图片会比较清晰）
            intent.putExtra("return-data",false)
            //设置裁剪图片保存位置
            val stallDir = FileUtil.getDiskCacheDir(fragment.requireContext(),"stall_helper")
            Log.d(TAG, "cropPhoto: $stallDir")
            if (!stallDir!!.exists()) {
                stallDir.mkdir()
            }
            val file = File(stallDir, "user_header.jpg")
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            cropPhotoPath = file.path
            var uri:Uri
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //通过FileProvider创建一个content类型的Uri
                FileProvider.getUriForFile(fragment.requireContext(), "com.gospell.stall.fileprovider", file);
            } else{
                Uri.fromFile(file);
            }
            //该属性设置的是拍照后图片保存的位置
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            //设置输出格式
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            //是否取消人脸识别
            intent.putExtra("noFaceDetection", true)
            fragment.startActivityForResult(intent,request_crop)
        }
        fun startPhoto(fragment: Fragment) {
            //intent隐式调用启动相册界面
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*" //设置数据类型
            val componentName = intent.resolveActivity(fragment.requireContext().packageManager)
            Log.d(TAG, "startPhoto: $componentName")
            if (componentName != null) { //防止启动意图时app崩溃
                fragment.startActivityForResult(intent,request_photo)
            }
        }
        fun startCamera(fragment: Fragment) {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +File.separator + "user_header.jpg")
            //intent隐式调用启动拍照界面
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //该属性设置为false表示拍照后不会将数据返回到onResluet方法中（建议设置为false，这样获取的图片会比较清晰）
            intent.putExtra("return-data",false)
            val componentName =intent.resolveActivity(fragment.requireContext().packageManager)
            var uri:Uri
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //通过FileProvider创建一个content类型的Uri
                FileProvider.getUriForFile(fragment.requireContext(), "com.gospell.stall.fileprovider", file);
            } else{
                Uri.fromFile(file);
            }
            //该属性设置的是拍照后图片保存的位置
            intent.putExtra(MediaStore.EXTRA_OUTPUT,uri)
            //防止app启动意图时崩溃
            if (componentName != null) {
                fragment.startActivityForResult( intent,request_camera_code)
            }
        }
    }
}