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
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gospell.stall.Constants
import com.gospell.stall.MainActivity
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.entity.User
import com.gospell.stall.ui.register.RegisterFragment
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.util.HttpUtil
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class LoginFragment : BaseFragment() {

    private lateinit var loginViewModel: LoginViewModel

    @InjectView(layout = R.layout.fragment_login)
    private var root: View? = null

    @InjectView(id = R.id.headImg)
    private val headImgView: CircleImageView? = null

    @InjectView(id = R.id.login_user)
    private val userText: EditText? = null

    @InjectView(id = R.id.login_password)
    private val passwordText: EditText? = null

    @InjectView(id = R.id.login_remember_password)
    private val rememberCheckBox: CheckBox? = null

    @InjectView(id = R.id.login_retrieve_password)
    private val retrievePasswordText: TextView? = null

    @InjectView(id = R.id.login_loginBtn)
    private val loginBtn: Button? = null

    @InjectView(id = R.id.login_go_register)
    private val registText: TextView? = null

    @InjectView(id = R.id.login_wechat)
    private val wechatImage: ImageView? = null
    private var api: IWXAPI? = null
    private var user: User? = null
    override fun onCreateView() {
        loginViewModel = ViewModelProvider(this,LoginViewModelFactory()).get(LoginViewModel::class.java)
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer {loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginBtn!!.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    userText!!.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordText!!.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                //loadingProgressBar!!.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(userText!!.text.toString(),passwordText!!.text.toString() )
            }
        }
        userText!!.addTextChangedListener(afterTextChangedListener)
        passwordText!!.addTextChangedListener(afterTextChangedListener)
        loginBtn!!.setOnClickListener {
            loginViewModel.login(userText!!.text.toString(), passwordText!!.text.toString())
        }
        retrievePasswordText!!.setOnClickListener { retrievePassword() }
        registText!!.setOnClickListener { register() }
        wechatImage!!.setOnClickListener { loginByWeChat() }
    }
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        Toast.makeText(requireContext(), welcome, Toast.LENGTH_LONG).show()
        goMainActivity()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
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
    private fun goMainActivity(){
        var intent = Intent(requireActivity(),MainActivity::class.java)
        requireActivity().startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val response = sharedPreferences.getString("responseInfo", "")
        if (response!!.isNotEmpty()) {
            val handler = Handler(Handler.Callback { msg: Message ->
                val bitmap = msg.obj as Bitmap
                headImgView!!.setImageBitmap(bitmap)
                goMainActivity()
                true
            })
            user = User().parseWXUserInfo(response)
            Constants.user = user
            userText!!.setText(user!!.nickname)
            user?.headimgurl?.let {
                HttpUtil.get(it,false){ response->
                    val bitmap = BitmapFactory.decodeStream(response.body!!.byteStream())
                    val message = Message()
                    message.obj = bitmap
                    handler.sendMessage(message)
                }
            }
        }
    }
}