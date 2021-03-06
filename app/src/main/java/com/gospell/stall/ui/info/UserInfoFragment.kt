package com.gospell.stall.ui.info

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.view.setPadding
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.ui.util.DhUtil
import com.gospell.stall.ui.util.DrawableUtil
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.ChoosePicDialog
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.ui.view.ItemDialog
import com.gospell.stall.ui.view.LoadDialog
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class UserInfoFragment : BaseFragment() {
    @InjectView(layout = R.layout.fragment_user_info)
    private lateinit var root: View
    //头像
    @InjectView(id = R.id.headImg)
    private lateinit var headImg: CircleImageView
    //账号
    @InjectView(id = R.id.account_text)
    private lateinit var accountText: EditText
    //昵称
    @InjectView(id = R.id.nick_text)
    private lateinit var nickText: EditText
    //手机
    @InjectView(id = R.id.phone_text)
    private lateinit var phoneText: EditText
    //修改个人资料按钮
    @InjectView(id = R.id.edit_user_btn)
    private lateinit var editUserBtn: Button
    //修改店铺信息按钮
    @InjectView(id = R.id.edit_stall_btn)
    private lateinit var editStallBtn: Button
    private lateinit var picDialog:ChoosePicDialog
    private lateinit var itemDialog: ItemDialog
    override fun onCreateView() {
        var imageUrl = Constants.user?.headimgurl!!
        if(imageUrl.indexOf("http")==-1){
            imageUrl = Constants.baseUrl + imageUrl
        }
        RequestHelper.getInstance(requireContext()).loadImage(imageUrl){bitmap ->
            requireActivity().runOnUiThread {
                headImg.setImageBitmap(bitmap)
            }
        }
        accountText.setText(Constants.user?.account)
        nickText.setText(Constants.user?.nickname)
        phoneText.setText(Constants.user?.phoneNumber)
        editUserBtn.setOnClickListener{createEditDialog()}
        setRadiusButton(editUserBtn)
        setRadiusButton(editStallBtn)
        headImg.setOnClickListener {
            picDialog = ChoosePicDialog(this)
                .onSuccess {file->
                    try {
                        val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                        headImg.setImageBitmap(bitmap)
                        updateAvatar(file)
                    } catch (e: FileNotFoundException) {
                        Log.e("uri", e.message, e)
                    }
                }.onFail {
                    val cropError = UCrop.getError(it)
                    Toast.makeText(requireContext(), cropError.toString(), Toast.LENGTH_SHORT).show()
                }
            picDialog.show()
        }
        editStallBtn.setOnClickListener {
            var stallInfo = Constants.user?.stallInfo
            if(stallInfo==null){
                LoadDialog(requireContext())
                    .setResultMessage("还没有地摊信息,去创建一个吧!")
                    .setCancel("取消")
                    .setCancelListener{ _, dialog ->
                        dialog.dismiss()
                    }
                    .setConfirm("创建地摊")
                    .setConfirmListener { _, dialog ->
                        dialog.dismiss()
                        jumpFragment(R.id.nav_host_fragment,StallInfoFragment(),"stallInfoFragment")
                    }.show()
            }else{
                jumpFragment(R.id.nav_host_fragment,StallInfoFragment(),"stallInfoFragment")
            }
        }
    }
    private fun createEditDialog(){
        itemDialog = ItemDialog(requireContext())
            .createDialog(R.layout.fragment_user_info)
            .setViewListener {view ->
                view.setBackgroundColor(requireContext().getColor(R.color.colorTextLight))
                var accountText = view.findViewById<EditText>(R.id.account_text)
                accountText.text = this.accountText.text
                var nickText = view.findViewById<EditText>(R.id.nick_text)
                nickText.text = this.nickText.text
                nickText.isEnabled = true
                var phoneText = view.findViewById<EditText>(R.id.phone_text)
                phoneText.text = this.phoneText.text
                phoneText.isEnabled = true
                view.findViewById<LinearLayout>(R.id.headImg_layout).visibility = View.GONE
                var cancelBtn = view.findViewById<Button>(R.id.edit_stall_btn)
                cancelBtn.text = "取消"
                setRadiusButton(cancelBtn)
                cancelBtn.setOnClickListener {itemDialog.dismiss()}
                var saveBtn = view.findViewById<Button>(R.id.edit_user_btn)
                saveBtn.text = "保存"
                setRadiusButton(saveBtn)
                saveBtn.setOnClickListener {
                    var nick = nickText?.checkBlank("昵称不能为空") ?: (return@setOnClickListener).toString()
                    var phoneNumber = phoneText?.checkBlank("电话号码不能为空") ?: (return@setOnClickListener).toString()
                    updateUser(nick,phoneNumber)
                }
            }
            .setGravity(Gravity.BOTTOM)
            .setCanCanceled(true)
        itemDialog.show()
    }
    private fun updateUser(nick:String,phoneNumber:String){
        var param = mutableMapOf<String, Any>()
        param["id"] =  Constants.user?.id!!
        param["nickname"] = nick
        param["phoneNumber"] = phoneNumber
        RequestHelper.getInstance(requireContext()).post(Constants.updateUrl, param, "信息修改中...") { result ->
            var json = JSONObject(result)
            if (json!!.getBoolean("success")) {
                requireActivity().runOnUiThread {
                    Constants.user?.nickname = nick
                    Constants.user?.phoneNumber = phoneNumber
                    nickText.setText(Constants.user?.nickname)
                    phoneText.setText(Constants.user?.phoneNumber)
                    itemDialog.dismiss()
                    ToastUtil.makeText(requireContext(),"信息更新成功!")
                }
            }else{
                ToastUtil.makeText(requireContext(),"信息更新失败!")
            }
        }
    }
    private fun updateAvatar(avatarFile: File){
        RequestHelper.getInstance(requireContext()).uploadImage(avatarFile,"logo上传中..."){ fileUploadedUrl ->
            requireActivity().runOnUiThread {
                var param = mutableMapOf<String, Any>()
                param["id"] = Constants.user?.id!!
                param["headimgurl"] = fileUploadedUrl
                RequestHelper.getInstance(requireContext()).post(Constants.updateUrl, param, "正在更新头像...") { result ->
                    var json = JSONObject(result)
                    if(json.getBoolean("success")){
                        Constants.user?.headimgurl = fileUploadedUrl
                        ToastUtil.makeText(requireContext(),"头像更新成功!")
                    }else{
                        ToastUtil.makeText(requireContext(),"头像更新失败!errorMsg:${json.getString("msg")}")
                    }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        picDialog!!.onActivityResult(requestCode,resultCode,data)
    }
}