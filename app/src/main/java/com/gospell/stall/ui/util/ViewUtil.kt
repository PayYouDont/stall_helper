package com.gospell.stall.ui.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import java.util.*

class ViewUtil {
    companion object {
        fun getAllChildViews(view: View?): List<View>? {
            val allChildren: MutableList<View> =
                ArrayList()
            if (view is ViewGroup) {
                val vp = view
                for (i in 0 until vp.childCount) {
                    val viewChild = vp.getChildAt(i)
                    allChildren.add(viewChild)
                    //再次 调用本身（递归）
                    allChildren.addAll(getAllChildViews(viewChild)!!)
                }
            }
            return allChildren
        }

        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
         */
        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        fun getdip(context: Context, value: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.resources.displayMetrics
            ).toInt()
        }

        fun createLinearLayout(
            context: Context?,
            resId: Int,
            width: Int,
            height: Int
        ): LinearLayout? {
            val borderBottomLayout = LinearLayout(context)
            borderBottomLayout.tag = "borderBottomLayout"
            borderBottomLayout.setBackgroundResource(resId)
            val borderParam = LinearLayout.LayoutParams(width, height)
            borderBottomLayout.layoutParams = borderParam
            return borderBottomLayout
        }

        fun getScreenRelatedInformation(context: Context) {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (windowManager != null) {
                val outMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(outMetrics)
                val widthPixels = outMetrics.widthPixels
                val heightPixels = outMetrics.heightPixels
                val densityDpi = outMetrics.densityDpi
                val density = outMetrics.density
                val scaledDensity = outMetrics.scaledDensity
                //可用显示大小的绝对宽度（以像素为单位）。
                //屏幕密度表示为每英寸点数。
                //显示器的逻辑密度。
                //显示屏上显示的字体缩放系数。
                println("widthPixels = $widthPixels,heightPixels = $heightPixels\n,densityDpi = $densityDpi\n,density = $density,scaledDensity = $scaledDensity")
            }
        }

        fun getRealScreenRelatedInformation(context: Context) {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (windowManager != null) {
                val outMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getRealMetrics(outMetrics)
                val widthPixels = outMetrics.widthPixels
                val heightPixels = outMetrics.heightPixels
                val densityDpi = outMetrics.densityDpi
                val density = outMetrics.density
                val scaledDensity = outMetrics.scaledDensity
                //可用显示大小的绝对宽度（以像素为单位）。
                //可用显示大小的绝对高度（以像素为单位）。
                //屏幕密度表示为每英寸点数。
                //显示器的逻辑密度。
                //显示屏上显示的字体缩放系数。
                println(
                    """
                    widthPixels = $widthPixels,heightPixels = $heightPixels
                    ,densityDpi = $densityDpi
                    ,density = $density,scaledDensity = $scaledDensity
                    """.trimIndent()
                )
            }
        }

        fun setBackgroundRadius(view: View, radius: Int, color: Int) {
            val drawable = GradientDrawable()
            drawable.cornerRadius = ViewUtil.getdip(view.context, radius.toFloat()).toFloat()
            //int color = ViewUtil.getColorByStatus (view.getContext (),userLog.getStatus ());
            drawable.setColor(color)
            drawable.setStroke(1, Color.parseColor("#D5DDDB"))
            view.background = drawable
        }
    }
}