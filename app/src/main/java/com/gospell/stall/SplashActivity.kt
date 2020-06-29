package com.gospell.stall

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gospell.stall.common.util.NetworkUtil
import com.gospell.stall.entity.User
import com.gospell.stall.helper.ActivityTack
import com.gospell.stall.helper.RequestHelper
import com.gospell.stall.ui.login.LoginActivity
import com.gospell.stall.util.JsonUtil
import org.json.JSONObject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }

    private val hideRunnable = Runnable { hide() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)
        ActivityTack.getInstanse()?.addActivity(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, 300.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }
    override fun onResume() {
        super.onResume()
        netCheck()
    }

    private fun netCheck() {
        if (NetworkUtil.checkEnable(this)) {
            login()
        } else {
            Toast.makeText(this,R.string.network_error,Toast.LENGTH_SHORT).show() //网络异常，请检查网络
        }
    }
    private fun login(){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "")
        if (token!!.isNotEmpty()) {
            var map = mutableMapOf<String,Any>()
            map["token"] = token
            RequestHelper.getInstance(this).post(Constants.loginUrl,map,"加载中..."){
                    result->
                var json = JSONObject(result)
                var intent:Intent
                if(json.getBoolean("success")){
                    Constants.user = JsonUtil.toBean(json.getString("user"), User::class.java)
                    Constants.token = json.getString("token")
                    val editor = sharedPreferences.edit()
                    editor.putString("token", token)
                    editor.apply()
                    intent = Intent(this,MainActivity::class.java)
                }else{
                    intent = Intent(this,LoginActivity::class.java)
                }
                startActivity(intent)
            }
        }else{
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}