package com.gospell.stall.entity

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class User{
    var id: Int? = null
    var nickname: String? = null
    var phoneNumber: String? = null
    var deviceId: String? = null
    var account: String? = null
    var password: String? = null
    var createTime: Date? = null
    var updateTime: Date? = null
    var status: Int? = null
    var sex: Int? = null
    var headimgurl: String? = null

    //经度
    var latitude: Double? = null

    //纬度
    var longitude: Double? = null

    //店铺信息
    var stallInfo: StallInfo? = null
    open fun parseWXUserInfo(response: String?): User? {
        try {
            val jsonObject = JSONObject(response)

            /**
             * 微信：
             * 这里可以返回如下数据
             * openid	普通用户的标识，对当前开发者帐号唯一
             * nickname	普通用户昵称
             * sex	普通用户性别，1为男性，2为女性
             * province	普通用户个人资料填写的省份
             * city	普通用户个人资料填写的城市
             * country	国家，如中国为CN
             * headimgurl	用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空
             * privilege	用户特权信息，json数组，如微信沃卡用户为（chinaunicom）
             * unionid	用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的unionid是唯一的。
             */
            val name = jsonObject.getString("nickname")
            val sex = jsonObject.getString("sex")
            val headimgurl = jsonObject.getString("headimgurl")
            val openid = jsonObject.getString("openid")
            nickname = name
            account = openid
            this.sex = Integer.valueOf(sex)
            this.headimgurl = headimgurl
        } catch (e: JSONException) {
            Log.e(javaClass.name, e.message, e)
        }
        return this
    }
}