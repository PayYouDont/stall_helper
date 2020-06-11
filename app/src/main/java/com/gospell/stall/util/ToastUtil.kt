package com.gospell.stall.util

import android.content.Context
import android.os.Looper
import android.widget.Toast

class ToastUtil {
    companion object{
        fun makeText(context: Context?, msg: String) {
            Looper.prepare()
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
        fun makeText(context: Context?, stringId: Int) {
            Looper.prepare()
            Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }
}