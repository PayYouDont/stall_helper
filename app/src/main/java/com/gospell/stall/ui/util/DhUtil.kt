package com.gospell.stall.ui.util

import android.content.Context

class DhUtil {
    companion object{
        fun dip2px(context: Context, dipValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (scale * dipValue + 0.5f).toInt()
        }

        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }


        fun delHtml(str: String): String? {
            var info =
                str.replace("\\&[a-zA-Z]{1,10};".toRegex(), "").replace("<[^>]*>".toRegex(), "")
            info = info.replace("[(/>)<]".toRegex(), "")
            return info
        }
    }
}