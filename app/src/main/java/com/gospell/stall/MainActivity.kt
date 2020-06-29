package com.gospell.stall

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gospell.stall.helper.ActivityTack
import com.gospell.stall.ui.info.StallInfoFragment

class MainActivity : AppCompatActivity() {
    private var lastExitTime : Long = 0//上一次点击退出app的时间戳

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        ActivityTack.getInstanse()!!.addActivity(this)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_user_info, R.id.navigation_setting))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        ActivityTack.getInstanse()?.addActivity(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event!!.action == KeyEvent.ACTION_DOWN) {
            var stallInfoFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.findFragmentByTag("stallInfoFragment")
            if(stallInfoFragment!=null){
                (stallInfoFragment as StallInfoFragment).onBack()
            }else{
                //3秒内重复点击2次退出
                if (System.currentTimeMillis() - lastExitTime < 3000) {
                    ActivityTack.getInstanse()?.exit()
                } else {
                    lastExitTime = System.currentTimeMillis()
                    Toast.makeText(this@MainActivity, R.string.exit_text, Toast.LENGTH_SHORT).show()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}