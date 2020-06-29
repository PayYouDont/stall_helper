package com.gospell.stall.ui.login

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gospell.stall.R
import com.gospell.stall.helper.ActivityTack

class LoginActivity : AppCompatActivity() {
    private var lastExitTime : Long = 0//上一次点击退出app的时间戳

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去掉标题栏
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
        setContentView(R.layout.activity_login)
        ActivityTack.getInstanse()!!.addActivity(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event!!.action == KeyEvent.ACTION_DOWN) {
            //3秒内重复点击2次退出
            if (System.currentTimeMillis() - lastExitTime < 3000) {
                ActivityTack.getInstanse()?.exit()
            } else {
                lastExitTime = System.currentTimeMillis()
                Toast.makeText(this@LoginActivity, R.string.exit_text, Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}