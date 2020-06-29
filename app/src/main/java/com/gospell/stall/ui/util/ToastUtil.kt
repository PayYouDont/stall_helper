package com.gospell.stall.ui.util

import android.content.Context
import android.os.Looper
import android.widget.Toast

class ToastUtil {
    companion object{
        fun makeText(context: Context?, msg: String) {
            if(isInMainThread()){
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }else{
                Looper.prepare()
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }
        fun makeText(context: Context?, stringId: Int) {
            if(isInMainThread()){
                Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
            }else{
                Looper.prepare()
                Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }
        private fun isInMainThread(): Boolean {
            return Looper.myLooper() == Looper.getMainLooper()
        }
    }
}