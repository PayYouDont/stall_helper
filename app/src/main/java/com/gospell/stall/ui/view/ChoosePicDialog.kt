package com.gospell.stall.ui.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.UCrop
import java.io.File

class ChoosePicDialog : ItemDialog {
    var list = arrayListOf("选择本地照片", "拍照", "取消")
    private val fragment:Fragment
    private var avatarFile: File? = null
    private var successListener: ((File) -> Unit)? = null
    private var failListener: ((Intent) -> Unit)? = null
    companion object {
        private const val CHOOSE_PICTURE = 0
        private const val TAKE_PICTURE = 1
        private var avatarFileName = "avatar.jpg"
        private var avatarOriginFileName = "avatarOrigin.jpg"
    }

    constructor(fragment:Fragment) : super(fragment.requireContext()) {
        this.fragment = fragment
        setTitle("选择头像")
        createLabelDialog("请上传一张您的照片作为头像", list)
        setItemListener { dialog, position ->
            when (position) {
                0 -> {// 选择本地照片
                    dialog.dismiss()
                    val intent = Intent(Intent.ACTION_PICK)//返回被选中项的URI
                    intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*"
                    )//得到所有图片的URI
                    fragment.startActivityForResult(intent, CHOOSE_PICTURE)
                }
                1 -> {// 拍照
                    dialog.dismiss()
                    val openCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val file = getAvatarFile(avatarOriginFileName)
                    if (file.exists()) {
                        file.delete()
                    }
                    val fileUri = getUriByOsVersion(file)
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                    openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    openCameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    fragment.startActivityForResult(openCameraIntent, TAKE_PICTURE)
                }
                2 -> dialog.dismiss()
            }
        }
    }
    fun onSuccess(successListener: (File) -> Unit):ChoosePicDialog{
        this.successListener = successListener
        return this
    }
    fun onFail(failListener: ((Intent) -> Unit)):ChoosePicDialog{
        this.failListener = failListener
        return this
    }
    //裁剪照片
    private fun cutImageByuCrop(uri: Uri?) {
        val outputImage = getAvatarFile(avatarFileName)
        val outputUri = Uri.fromFile(outputImage)

        uri?.let {
            UCrop.of(it, outputUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(256, 256)
                .start(fragment.requireActivity(), fragment)
        }
    }

    //获取头像
    private fun getAvatarFile(filename: String): File {
        // 使用 APP 内部储存空间
        val appDir = File(fragment.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).absolutePath,"Avatar")
        if (!appDir.exists())
            appDir.mkdir()
        return File(appDir, filename)
    }

    //获取uri
    private fun getUriByOsVersion(file: File): Uri {
        val currentApiVersion = android.os.Build.VERSION.SDK_INT
        return if (currentApiVersion < 24) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(fragment.requireContext(), "com.gospell.stall.provider", file)
        }
    }
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // 对拍照返回的图片进行裁剪处理
                TAKE_PICTURE -> {
                    val imgUriSel = getUriByOsVersion(getAvatarFile(avatarOriginFileName))
                    cutImageByuCrop(imgUriSel)
                }
                // 对在图库选择的图片进行裁剪处理
                CHOOSE_PICTURE -> cutImageByuCrop(data?.data)
                // 上传裁剪成功的文件
                UCrop.REQUEST_CROP -> {
                    data?.let {
                        avatarFile = getAvatarFile(avatarFileName)
                        successListener!!.invoke(avatarFile!!)
                    }
                }
                // 输出裁剪
                UCrop.RESULT_ERROR -> {
                    failListener?.invoke(data!!)
                }
            }
        }
    }
}