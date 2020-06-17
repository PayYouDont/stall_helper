package com.gospell.stall.ui.stall

import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.StallInfo
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import com.gospell.stall.util.LocationUtils
import com.gospell.stall.util.ToastUtil

class StallFragment : BaseFragment() {
    @InjectView(layout = R.layout.fragment_stall)
    private var root:View? = null
    //店铺名称
    @InjectView( id = R.id.name_text)
    private var nameText:EditText? = null
    //店铺简介
    @InjectView( id = R.id.brief_text)
    private var briefText:EditText? = null
    //店铺位置
    @InjectView( id = R.id.position_spinner)
    private var positionSpinner:Spinner? = null
    @InjectView( id = R.id.position_text)
    private var positionText:TextView? = null
    private var positionTextValue:String? = null
    //保存按钮
    @InjectView( id = R.id.save_btn)
    private var saveButton:Button? = null
    var positionArr:ArrayList<String>? = null
    override fun onCreateView() {
        var stallInfo = Constants.user?.stallInfo
        nameText?.setText(stallInfo?.name)
        briefText?.setText(stallInfo?.content)
        positionText?.text =  LocationUtils.getLocality(requireContext(),stallInfo?.latitude!!,stallInfo?.longitude!!)
        if(stallInfo!=null){
            saveButton?.text = "修改"
        }else{
            saveButton?.text = "保存"
        }
        positionArr = arrayListOf("选择摊位位置","当前位置","地图上选择")
        var positionAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,positionArr)
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        positionSpinner?.adapter = positionAdapter
        positionSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,view: View, position: Int,id: Long) {
                when(position){
                    0-> Toast.makeText (requireContext (), "请选择摊位坐标", Toast.LENGTH_SHORT).show ()
                    1-> {
                        positionText?.text = LocationUtils.getLocality(requireContext(),Constants.user?.latitude!!,Constants.user?.longitude!!)
                        positionTextValue = Constants.user?.latitude.toString()+"," +Constants.user?.longitude.toString()
                    }
                    2-> Toast.makeText (requireContext (), "待开发", Toast.LENGTH_SHORT).show ()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        saveButton?.setOnClickListener {saveStall()}
    }
    //保存摊位信息
    private fun saveStall(){
        var stall = StallInfo()
        stall.name = nameText?.checkBlank("店铺名不能为空！")?:return
        stall.content = briefText?.checkBlank("店铺简介不能为空！")?:return
        var latLng = positionText?.checkBlank("店铺坐标不能为空！")?:return
        var latLngArr = latLng.split(",")
        stall.latitude = latLngArr?.get(0)?.toDouble()
        stall.longitude = latLngArr?.get(1)?.toDouble()
        var map = mutableMapOf<String,Any>()
        map.putAll(JsonUtil.stringToCollect<String,Any>(JsonUtil.toJson(stall)) as MutableMap)
        map["userId"] = Constants.user?.id!!
        HttpUtil.post(Constants.updateStallUrl,map){
            response ->
            var json = org.json.JSONObject(response.body!!.string())
            if(json.getBoolean("success")){
                ToastUtil.makeText (requireContext (),"保存成功")
            }else{
                ToastUtil.makeText(requireContext (),"保存失败")
            }
        }
    }
    private fun TextView.checkBlank(message: String): String? {
        val text = this.text.toString()
        if (text.isBlank()||text=="") {
            Toast.makeText (requireContext (), message, Toast.LENGTH_SHORT).show ()
            return null
        }
        return text
    }
}