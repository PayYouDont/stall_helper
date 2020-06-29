package com.gospell.stall.wxapi

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.gospell.stall.Constants
import com.gospell.stall.util.HttpUtil
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class WXEntryActivity:Activity(), IWXAPIEventHandler{
    // IWXAPI 是第三方app和微信通信的openApi接口
    private var api: IWXAPI? = null
    companion object{
        const val TAG = "WXEntryActivity:"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false)
        val result = api!!.handleIntent(intent, this)
        if (!result) {
            Log.d(TAG, "参数异常")
            finish()
        }
    }

    override fun onResp(baseResp: BaseResp?) {
        //登录回调
        when (baseResp!!.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                val code = (baseResp as SendAuth.Resp).code
                //获取accesstoken
                getAccessToken(code)
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> finish()
            BaseResp.ErrCode.ERR_USER_CANCEL -> finish()
        }
    }

    override fun onReq(p0: BaseReq?) {

    }
    private fun getAccessToken(code: String) {
        /**
         * access_token:接口调用凭证
         * appid：应用唯一标识，在微信开放平台提交应用审核通过后获得。
         * secret：应用密钥AppSecret，在微信开放平台提交应用审核通过后获得。
         * code：填写第一步获取的code参数。
         * grant_type：填authorization_code。
         */
        val loginUrl = StringBuffer()
        loginUrl.append("https://api.weixin.qq.com/sns/oauth2/access_token")
            .append("?appid=")
            .append(Constants.APP_ID)
            .append("&secret=")
            .append(Constants.APP_SECRET)
            .append("&code=")
            .append(code)
            .append("&grant_type=authorization_code")
        Log.d("urlurl", loginUrl.toString())
        HttpUtil.get(loginUrl.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG,e.message,e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseInfo = response.body!!.string()
                Log.d(TAG, "onResponse: Success")
                var access: String? = null
                var openId: String? = null
                //用json去解析返回来的access和token值
                try {
                    val jsonObject = JSONObject(responseInfo)
                    access = jsonObject.getString("access_token")
                    openId = jsonObject.getString("openid")
                    Log.d(TAG, "onResponse:$access  $openId")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                getUserInfo(access, openId)
            }
        })
    }

    //如果请求成功，我们通过JSON解析获取access和token值，再通过getUserInfo(access, openId)方法获取用户信息
    private fun getUserInfo(access: String?, openid: String?) {
        val getUserInfoUrl ="https://api.weixin.qq.com/sns/userinfo?access_token=$access&openid=$openid"
        HttpUtil.get(getUserInfoUrl,object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG,e.message,e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseInfo = response.body!!.string()
                //用SharedPreference来缓存字符串
                val editor = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit()
                editor.putString("responseInfo", responseInfo)
                editor.commit()
                finish()
            }
        })
    }
}