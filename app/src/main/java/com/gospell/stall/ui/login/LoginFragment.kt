package com.gospell.stall.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.gospell.stall.Constants
import com.gospell.stall.MainActivity
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.User
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.ui.register.RegisterFragment
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.JsonUtil
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginFragment : BaseFragment() {
    @InjectView(layout = R.layout.fragment_login)
    private var root: View? = null

    @InjectView(id = R.id.headImg)
    private lateinit var headImg: CircleImageView

    @InjectView(id = R.id.login_user)
    private lateinit var userText: EditText

    @InjectView(id = R.id.login_password)
    private lateinit var passwordText: EditText

    @InjectView(id = R.id.login_remember_password)
    private lateinit var rememberCheckBox: CheckBox

    @InjectView(id = R.id.login_retrieve_password)
    private lateinit var retrievePasswordText: TextView

    @InjectView(id = R.id.login_loginBtn)
    private lateinit var loginBtn: Button

    @InjectView(id = R.id.login_go_register)
    private lateinit var registText: TextView

    @InjectView(id = R.id.login_wechat)
    private lateinit var wechatImage: ImageView
    private lateinit var api: IWXAPI
    private lateinit var user: User
    override fun onCreateView() {
        var sharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        var headImgUrl = sharedPreferences.getString("headimgurl", "")
        if (!headImgUrl.isNullOrBlank()){
            if(headImgUrl.indexOf("http")==-1){
                headImgUrl = Constants.baseUrl + headImgUrl
            }
            RequestHelper.getInstance(requireContext()).loadImage(headImgUrl){bitmap ->
                requireActivity().runOnUiThread {
                    headImg.setImageBitmap(bitmap)
                }
            }
        }
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }
            override fun afterTextChanged(s: Editable) {
                loginDataChanged(userText!!.text.toString(),passwordText!!.text.toString() )
            }
        }
        userText.addTextChangedListener(afterTextChangedListener)
        passwordText.addTextChangedListener(afterTextChangedListener)
        loginBtn.setOnClickListener {
            login(userText.text.toString(), passwordText!!.text.toString())
        }
        retrievePasswordText.setOnClickListener { retrievePassword() }
        registText.setOnClickListener { register() }
        wechatImage.setOnClickListener { loginByWeChat() }
    }
    private fun updateUiWithUser() {
        requireActivity().runOnUiThread{
            val welcome = getString(R.string.welcome) + Constants.user?.nickname
            ToastUtil.makeText(requireContext(),welcome)
            startActivity(Intent(requireContext(),MainActivity::class.java))
        }
    }
    private fun showLoginFailed( errorString: String) {
        requireActivity().runOnUiThread{
            LoadDialog(requireContext())
                .setCanCanceled(true)
                .setResultMessage(errorString)
                .setLoadMessage(null).show()
        }
    }
    fun loginDataChanged(username: String, password: String) {
        loginBtn.isEnabled = false
        if (!isUserNameValid(username)) {
            userText.error = getString(R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            passwordText.error = getString(R.string.invalid_password)
        } else {
            loginBtn.isEnabled = true
        }
    }
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    /**
     * @return void
     * @Author peiyongdong
     * @Description (找回密码)
     * @Date 17:00 2019/11/26
     * @Param []
     **/
    private fun retrievePassword() { }
    /**
     * @return void
     * @Author peiyongdong
     * @Description (注册)
     * @Date 17:01 2019/11/26
     * @Param []
     **/
    private fun register() {
        parentFragmentManager.beginTransaction().replace(R.id.fragment_login, RegisterFragment()).commit()
    }

    /**
     * 账号密码登录
     */
    private fun login(username: String, password: String){
        var map = mutableMapOf<String,Any>()
        map["account"] = username
        map["password"] = password
        RequestHelper.getInstance(requireContext()).post(Constants.loginUrl,map,"登录中..."){
                result->
            try {
                var json = JSONObject(result)
                if(json.getBoolean("success")){
                    Constants.user = JsonUtil.toBean(json.getString("user"),User::class.java)
                    Constants.token = json.getString("token")
                    val editor = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit()
                    editor.putString("token", Constants.token)
                    editor.commit()
                    updateUiWithUser()
                }else{
                    showLoginFailed(json.getString("msg"))
                }
            }catch (e:Exception){
                showLoginFailed(e.message!!)
            }
        }
    }
    private fun loginByWeChat() {
        api = WXAPIFactory.createWXAPI(requireContext(), Constants.APP_ID, true)
        api?.registerApp(Constants.APP_ID)
        if (api!!.isWXAppInstalled) {
            Toast.makeText(requireContext(), "您的设备未安装微信客户端", Toast.LENGTH_SHORT).show()
        } else {
            val req = SendAuth.Req()
            //应用授权作用域，如获取用户个人信息则填写 snsapi_userinfo
            req.scope = "snsapi_userinfo"
            //用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验
            req.state = "wechat_sdk_demo_test"
            api?.sendReq(req)
        }
    }
    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val response = sharedPreferences.getString("responseInfo", "")
        if (response.isNotEmpty()) {
            val handler = Handler(Handler.Callback { msg: Message ->
                val bitmap = msg.obj as Bitmap
                headImg.setImageBitmap(bitmap)
                startActivity(Intent(requireActivity(),MainActivity::class.java))
                true
            })
            user = User().parseWXUserInfo(response)
            Constants.user = user
            userText.setText(user.nickname)
            user.headimgurl.let {
                if (it != null) {
                    HttpUtil.get(it,object :Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("LoginFragment",e.message,e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val bitmap = BitmapFactory.decodeStream(response.body!!.byteStream())
                            val message = Message()
                            message.obj = bitmap
                            handler.sendMessage(message)
                        }
                    })
                }
            }
        }
    }
}