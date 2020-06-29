package com.gospell.stall.ui.register

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.gospell.stall.Constants
import com.gospell.stall.R
import com.gospell.stall.common.annotation.InjectView
import com.gospell.stall.common.base.BaseFragment
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.helper.TextChangedListener
import com.gospell.stall.ui.login.LoginActivity
import com.gospell.stall.ui.login.LoginFragment
import com.gospell.stall.ui.util.DhUtil
import com.gospell.stall.ui.util.DrawableUtil
import com.gospell.stall.ui.util.ToastUtil
import com.gospell.stall.ui.view.ChoosePicDialog
import com.gospell.stall.ui.view.CircleImageView
import com.gospell.stall.ui.view.LoadDialog
import com.gospell.stall.util.HttpUtil
import com.yalantis.ucrop.UCrop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import ru.alexbykov.nopermission.PermissionHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.collections.set


class RegisterFragment : BaseFragment(), View.OnClickListener {

    @InjectView(layout = R.layout.fragment_register)
    private var root: View? = null

    @InjectView(id = R.id.register_layout)
    private var registerLayout: LinearLayout? = null

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

    private var invalidUsername: String? = null
    private var invalidPassword: String? = null
    private var permissionHelper: PermissionHelper? = null
    private var picDialog:ChoosePicDialog?=null
    private var avatarFile:File? = null

    override fun onCreateView() {
        permissionHelper = PermissionHelper(this)
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
            if (!hasFocus) {
                isUserNameValid(accountText!!.text.toString())
            }
        }
        passwordText!!.addTextChangedListener(object : TextChangedListener {
            override fun afterTextChanged(s: Editable?) {
                if (!isPasswordValid(passwordText!!.text.toString())) {
                    passwordText?.error = invalidPassword
                }
            }
        })
        passwordAgainText!!.addTextChangedListener(object : TextChangedListener {
            override fun afterTextChanged(s: Editable?) {
                if (!isPasswordValid(passwordText!!.text.toString())) {
                    passwordText?.error = invalidPassword
                }
                if (!isPasswordAgainValid(
                        passwordText!!.text.toString(),
                        passwordAgainText!!.text.toString()
                    )
                ) {
                    passwordAgainText?.error = getString(R.string.invalid_password_again)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.title_bar_back_btn -> parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_login, LoginFragment()).commit()
            R.id.headImg -> {
                permissionHelper!!.check(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).onSuccess {
                    picDialog = ChoosePicDialog(this)
                    picDialog!!.onSuccess {file->
                        avatarFile= file
                        try {
                            val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                            headImg?.setImageBitmap(bitmap)
                        } catch (e: FileNotFoundException) {
                            Log.e("uri", e.message, e)
                        }
                    }.onFail {
                        val cropError = UCrop.getError(it)
                        Toast.makeText(requireContext(), cropError.toString(), Toast.LENGTH_SHORT).show()
                    }.show()
                }.onDenied {
                    Toast.makeText(requireContext(), "权限被拒绝！将读取照片信息!", Toast.LENGTH_SHORT).show()
                }.onNeverAskAgain {
                    Toast.makeText(requireContext(), "需要授权后才能使用！", Toast.LENGTH_SHORT).show();
                    permissionHelper!!.startApplicationSettingsActivity();
                }.run()
            }
            R.id.register_btn -> toRegister()
        }
    }

    private fun toRegister() {
        var param = mutableMapOf<String, Any>()
        param["account"] = accountText?.checkBlank("账号不能为空") ?: return.toString()
        param["nickname"] = nickText?.checkBlank("昵称不能为空") ?: return.toString()
        param["phoneNumber"] = passwordText?.checkBlank("电话不能为空") ?: return.toString()
        param["password"] = passwordText?.checkBlank("密码不能为空") ?: return.toString()
        if (avatarFile == null) {
            Toast.makeText(requireContext(), "请选择头像", Toast.LENGTH_SHORT).show()
            return
        }
        /*"image/jpg".toMediaTypeOrNull()?.let {
            RequestHelper.getInstance(requireContext()).updateFile(Constants.avatarUploadUrl, it,avatarFile!!, "头像上传中...") { result ->
                try {
                    requireActivity().runOnUiThread {
                        var json = JSONObject(result)
                        if (json!!.getBoolean("success")) {
                            var url = json.getString("data")
                            param["headimgurl"] = url
                            RequestHelper.getInstance(requireContext()).post(Constants.registerUrl, param, "注册中...") { result ->
                                var json = JSONObject(result)
                                requireActivity().runOnUiThread {
                                    if (json!!.getBoolean("success")) {
                                        LoadDialog(requireContext())
                                            .setResultMessage("注册成功！")
                                            .setConfirm("去登录")
                                            .setConfirmListener { _, dialog ->
                                                dialog.dismiss()
                                                requireActivity().startActivity(Intent(requireActivity(),LoginActivity::class.java))
                                            }.show()
                                    } else {
                                        LoadDialog(requireContext())
                                            .setCanCanceled(true)
                                            .setLoadMessage(null)
                                            .setResultMessage(json.getString("msg"))
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RegisterFragment", e.message, e)
                    ToastUtil.makeText(requireContext(), e.message!!)
                }
            }
        }*/
        RequestHelper.getInstance(requireContext()).uploadImage(avatarFile!!, "头像上传中..."){fileUploadedUrl->
            requireActivity().runOnUiThread {
                param["headimgurl"] = fileUploadedUrl
                RequestHelper.getInstance(requireContext()).post(Constants.registerUrl, param, "注册中...") { result ->
                    var json = JSONObject(result)
                    requireActivity().runOnUiThread {
                        if (json!!.getBoolean("success")) {
                            LoadDialog(requireContext())
                                .setResultMessage("注册成功！")
                                .setConfirm("去登录")
                                .setConfirmListener { _, dialog ->
                                    dialog.dismiss()
                                    requireActivity().startActivity(Intent(requireActivity(),LoginActivity::class.java))
                                }.show()
                        } else {
                            LoadDialog(requireContext())
                                .setCanCanceled(true)
                                .setLoadMessage(null)
                                .setResultMessage(json.getString("msg"))
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun isUserNameValid(username: String) {
        if (username.isNotBlank()) {
            var param = mutableMapOf<String, Any>()
            param["account"] = accountText!!.text.toString()
            HttpUtil.post(Constants.checkAccountUrl, param, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("RegisterFragment", e.message, e)
                }

                override fun onResponse(call: Call, response: Response) {
                    var result = response.body!!.string()
                    requireActivity().runOnUiThread {
                        try {
                            var json = JSONObject(result)
                            var success = json!!.getBoolean("success")
                            if (!success) {
                                accountText?.error = json?.getString("msg")
                                registerBtn?.isEnabled = false
                            } else {
                                registerBtn?.isEnabled = true
                                if (username.contains("@")) {
                                    if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                                        accountText?.error = getString(R.string.invalid_username)
                                        registerBtn?.isEnabled = false
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            ToastUtil.makeText(requireContext(), e.message!!)
                        }
                    }
                }
            })
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordAgainValid(password: String, passwordAgain: String): Boolean {
        return password == passwordAgain
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        picDialog!!.onActivityResult(requestCode,resultCode,data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}