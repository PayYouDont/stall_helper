package com.gospell.stall.ui.info

import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gospell.stall.Constants
import com.gospell.stall.MapActivity
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.StallInfo
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.helper.TextChangedListener
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.ChoosePicDialog
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.ui.view.ItemDialog
import com.gospell.stall.util.JsonUtil
import com.gospell.stall.util.LocationUtils
import com.yalantis.ucrop.UCrop
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class StallInfoFragment : BaseFragment() {
    @InjectView(layout = R.layout.fragment_stall_info)
    private lateinit var root: View
    //店铺头像
    @InjectView(id = R.id.headImg)
    private lateinit var headImg:CircleImageView
    //店铺名称
    @InjectView(id = R.id.name_text)
    private lateinit var nameText: EditText

    //店铺简介
    @InjectView(id = R.id.brief_text)
    private lateinit var briefText: EditText

    @InjectView(id = R.id.position_text)
    private lateinit var positionText: TextView
    private lateinit var positionTextValue: String

    //保存按钮
    @InjectView(id = R.id.save_btn)
    private lateinit var saveButton: Button
    private val REQUEST_CODE = 10
    private lateinit var picDialog:ChoosePicDialog
    private lateinit var logoFile: File
    override fun onCreateView() {
        var imageUrl = Constants.user?.stallInfo?.logoImgUrl
        if(imageUrl!=null){
            imageUrl = Constants.baseUrl+imageUrl
            RequestHelper.getInstance(requireContext()).loadImage(imageUrl){bitmap ->
                requireActivity().runOnUiThread {
                    headImg?.setImageBitmap(bitmap)
                }
            }
        }
        headImg?.setOnClickListener {
            picDialog = ChoosePicDialog(this)
                .onSuccess {file->
                    logoFile = file
                    try {
                        updateLogo()
                        val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                        headImg?.setImageBitmap(bitmap)
                    } catch (e: FileNotFoundException) {
                        Log.e("uri", e.message, e)
                    }
                }.onFail {
                    val cropError = UCrop.getError(it)
                    Toast.makeText(requireContext(), cropError.toString(), Toast.LENGTH_SHORT).show()
                }
            picDialog.show()
        }
        var stallInfo = Constants.user?.stallInfo
        nameText?.setText(stallInfo?.name)
        nameText!!.addTextChangedListener(object : TextChangedListener {
            override fun afterTextChanged(s: Editable?) {
                saveButton?.isEnabled = nameText!!.text.toString()!=Constants.user!!.stallInfo?.name
            }
        })
        briefText?.setText(stallInfo?.content)
        briefText!!.addTextChangedListener(object : TextChangedListener {
            override fun afterTextChanged(s: Editable?) {
                saveButton?.isEnabled = briefText!!.text.toString()!=Constants.user!!.stallInfo?.content
            }
        })
        if (stallInfo != null) {
            positionText?.text = LocationUtils.getStreet(requireContext(),stallInfo.latitude!!,stallInfo.longitude!!)
            saveButton?.text = "修改"
        } else {
            saveButton?.text = "保存"
        }
        positionText?.setOnClickListener {
            var items = arrayListOf("当前位置","地图上选择","取消")
            ItemDialog(requireContext())
                .createLabelDialog("请选择店铺的地里位置",items)
                .setItemListener { dialog, position ->
                    when(position){
                        0 -> {
                            var lat = Constants.user?.latitude!!
                            var lng = Constants.user?.longitude!!
                            positionText?.text = LocationUtils.getStreet(requireContext(),lat,lng)
                            positionTextValue = "$lat,$lng"
                            saveButton?.isEnabled = true
                        }
                        1 -> {
                            var intent = Intent(requireActivity(),MapActivity::class.java)
                            intent.putExtra("code",REQUEST_CODE)
                            startActivityForResult(intent,REQUEST_CODE)
                        }
                    }
                    dialog.dismiss()
                }
                .show()
        }
        saveButton.isEnabled = false
        saveButton.setOnClickListener { saveStall() }
        setRadiusButton(saveButton)
    }
    private fun updateLogo(){
        RequestHelper.getInstance(requireContext()).uploadImage(logoFile,"logo上传中..."){ fileUploadedUrl ->
            var stallInfo = Constants.user!!.stallInfo
            requireActivity().runOnUiThread {
                if (stallInfo != null) {
                    var param = mutableMapOf<String, Any>()
                    param["userId"] = Constants.user?.id!!
                    param["id"] = Constants.user?.stallInfo?.id!!
                    param["logoImgUrl"] = fileUploadedUrl
                    RequestHelper.getInstance(requireContext())
                        .post(Constants.updateStallUrl, param, "正在更新Logo...") { result ->
                            var json = JSONObject(result)
                            if (json.getBoolean("success")) {
                                Constants.user!!.stallInfo!!.logoImgUrl = fileUploadedUrl
                                ToastUtil.makeText(requireContext(), "Logo更新成功!")
                            } else {
                                ToastUtil.makeText(requireContext(),"Logo更新失败!errorMsg:${json.getString("msg")}")
                            }
                        }
                } else {
                    stallInfo = StallInfo()
                    stallInfo?.logoImgUrl = fileUploadedUrl
                    Constants.user!!.stallInfo = stallInfo
                    saveButton?.isEnabled = true
                }
            }
        }
    }
    //保存摊位信息
    private fun saveStall() {
        var stall = StallInfo()
        stall.name = nameText?.checkBlank("店铺名不能为空！") ?: return
        stall.content = briefText?.checkBlank("店铺简介不能为空！") ?: return
        positionText?.checkBlank("店铺坐标不能为空！") ?: return
        if(positionTextValue.isNullOrBlank()){
            return
        }
        var latLngArr = positionTextValue?.split(",")
        stall.latitude = latLngArr?.get(0)?.toDouble()
        stall.longitude = latLngArr?.get(1)?.toDouble()
        stall.logoImgUrl = Constants.user!!.stallInfo?.logoImgUrl
        stall.id = Constants.user!!.stallInfo?.id
        var map = mutableMapOf<String, Any>()
        map.putAll(JsonUtil.stringToCollect<String, Any>(JsonUtil.toJson(stall)) as MutableMap)
        map["userId"] = Constants.user?.id!!
        RequestHelper.getInstance(requireContext())
            .post(Constants.updateStallUrl, map, "保存中...") { result ->
                var json = JSONObject(result)
                if (json.getBoolean("success")) {
                    Constants.user!!.stallInfo = stall
                    requireActivity().runOnUiThread {
                        saveButton?.isEnabled = false
                        ToastUtil.makeText(requireContext(), "保存成功")
                    }
                } else {
                    ToastUtil.makeText(requireContext(), "保存失败")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE->{
                var position = data?.getStringExtra("position")
                if(position!=null){
                    var lat = position.split(",")[0].toDouble()
                    var lng = position.split(",")[1].toDouble()
                    positionText?.text = LocationUtils.getStreet(requireContext(),lat,lng)
                    positionTextValue = position
                    saveButton?.isEnabled = true
                }
                return
            }
        }
        picDialog!!.onActivityResult(requestCode,resultCode,data)
    }
    fun onBack(){
        jumpFragment(R.id.nav_host_fragment,UserInfoFragment())
    }

}