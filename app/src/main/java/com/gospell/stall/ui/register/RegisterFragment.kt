package com.gospell.stall.ui.register

import android.graphics.Color
import android.text.Editable
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.other.TextChangedListener
import com.gospell.stall.ui.login.LoginFragment
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.util.DhUtil
import com.gospell.stall.util.DrawableUtil
import com.gospell.stall.util.HttpUtil
import com.gospell.stall.util.ToastUtil
import org.json.JSONObject

class RegisterFragment : BaseFragment(), View.OnClickListener {

    @InjectView(layout = R.layout.fragment_register)
    private var root: View? = null

    @InjectView(id = R.id.title_bar_back_btn, listeners = [View.OnClickListener::class])
    private var backBtn: Button? = null

    @InjectView(id = R.id.headImg, listeners = [View.OnClickListener::class])
    private var headImg: CircleImageView? = null

    @InjectView(id = R.id.register_btn, listeners = [View.OnClickListener::class])
    private var registerBtn: Button? = null

    @InjectView(id = R.id.account_text)
    private var accountText: EditText? = null

    @InjectView(id = R.id.nick_text)
    private var nickText: EditText? = null

    @InjectView(id = R.id.password_text)
    private var passwordText: EditText? = null

    @InjectView(id = R.id.password_again_text)
    private var passwordAgainText: EditText? = null

    private var invalidUsername:String? = null
    private var invalidPassword:String? = null
    override fun onCreateView() {
        invalidUsername = getString(R.string.invalid_username)
        invalidPassword = getString(R.string.invalid_password)
        registerBtn!!.background = DrawableUtil.getGradientDrawable(
            requireContext(),
            DhUtil.dip2px(requireContext(), 3f),
            Color.parseColor("#1BCC87"),
            DhUtil.dip2px(requireContext(), 0.2f),
            Color.parseColor("#1BCC87")
        )
        accountText!!.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                isUserNameValid(accountText!!.text.toString())
            }
        }
        passwordText!!.addTextChangedListener(object :TextChangedListener{
            override fun afterTextChanged(s: Editable?) {
                if (!isPasswordValid(passwordText!!.text.toString())) {
                    passwordText?.error = invalidPassword
                }
            }
        })
        passwordAgainText!!.addTextChangedListener(object :TextChangedListener{
            override fun afterTextChanged(s: Editable?) {
                if (!isPasswordValid(passwordText!!.text.toString())) {
                    passwordText?.error = invalidPassword
                }
                if(!isPasswordAgainValid(passwordText!!.text.toString(),passwordAgainText!!.text.toString())){
                    passwordAgainText?.error = getString(R.string.invalid_password_again)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.title_bar_back_btn -> toLogin()
            R.id.headImg -> {

            }
            R.id.register_btn -> toRegister()
        }
    }
    private fun toRegister(){
        var param = mutableMapOf<String,Any>()
        param["account"] = accountText!!.text.toString()
        param["nickname"] = nickText!!.text.toString()
        param["password"] = passwordText!!.text.toString()
        HttpUtil.post(Constants.registerUrl,param){
                response ->
            var json = JSONObject(response.body!!.string())
            if(json!!.getBoolean("success")){
                ToastUtil.makeText(requireContext(),R.string.register_success)
            }
        }
    }
    private fun toLogin(){
        parentFragmentManager.beginTransaction().replace(R.id.fragment_login, LoginFragment()).commit()
    }
    private fun isUserNameValid(username: String): Boolean {
        var success:Boolean? = false
        if(username.isNotBlank()){
            var param = mutableMapOf<String,Any>()
            param["account"] =  accountText!!.text.toString()
            HttpUtil.post(Constants.checkAccountUrl,param){
                    response ->
                var result = response.body!!.string()
                var json = JSONObject(result)
                success = json!!.getBoolean("success")
            }
        }
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            success!!
        }
    }
    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
    private fun isPasswordAgainValid(password: String,passwordAgain: String): Boolean {
        return password == passwordAgain
    }
}