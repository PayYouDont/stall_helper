package com.gospell.stall.util

import android.R
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable

class DrawableUtil{
    companion object{
        /**
         * @param solidColor  填充色
         * @param strokeW     边框大小
         * @param strokeColor 边框颜色
         */
        fun getGradientDrawable(
            context: Context,
            solidColor: Int,
            strokeW: Float,
            strokeColor: Int
        ): GradientDrawable? {
            val density = context.resources.displayMetrics.density
            val strokeWidth = strokeW * density
            // 创建drawable
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.OVAL
            gradientDrawable.setColor(solidColor)
            gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor)
            return gradientDrawable
        }

        /**
         * @param radius      圆角大小
         * @param solidColor  填充色
         * @param strokeW     边框大小
         * @param strokeColor 边框颜色
         */
        fun getGradientDrawable(
            context: Context,
            radius: Int,
            solidColor: Int,
            strokeW: Int,
            strokeColor: Int
        ): GradientDrawable? {
            val density = context.resources.displayMetrics.density
            val roundRadius = radius * density
            val strokeWidth = strokeW * density
            // 创建drawable
            val gradientDrawable = GradientDrawable()
            gradientDrawable.setColor(solidColor)
            gradientDrawable.cornerRadius = roundRadius
            gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor)
            return gradientDrawable
        }


        /**
         * @param orientation 渐变色方向 eg:GradientDrawable.Orientation.LEFT_RIGHT
         * @param radius      圆角大小
         * @param colors      渐变色默认从左到右
         * @param strokeW     边框大小
         * @param strokeColor 边框颜色
         */
        fun getGradientDrawable(
            context: Context,
            radius: Float,
            orientation: GradientDrawable.Orientation?,
            colors: IntArray?,
            strokeW: Float,
            strokeColor: Int
        ): GradientDrawable? {
            val density = context.resources.displayMetrics.density
            val roundRadius = radius * density
            val strokeWidth = strokeW * density
            val gradientDrawable =
                GradientDrawable(orientation, colors) // 创建drawable
            gradientDrawable.cornerRadius = roundRadius
            gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor)
            return gradientDrawable
        }


        /**
         * @param radii       每一个角的圆角大小
         * @param solidColor  填充色
         * @param strokeW     边框大小
         * @param strokeColor 边框颜色
         */
        fun getGradientDrawable(
            context: Context,
            radii: FloatArray?,
            solidColor: Int,
            strokeW: Float,
            strokeColor: Int
        ): GradientDrawable? {
            val density = context.resources.displayMetrics.density
            val strokeWidth = strokeW * density
            val gradientDrawable = GradientDrawable() // 创建drawable
            gradientDrawable.setColor(solidColor)
            gradientDrawable.cornerRadii = radii
            gradientDrawable.setStroke(strokeWidth.toInt(), strokeColor)
            return gradientDrawable
        }

        /**
         * @param colorNormal 正常颜色
         * @param colorPress  按下的颜色
         * @param radius      圆角
         * @param strokeWidth 边框
         * @param strokeColor 边框色
         */
        fun getStateListDrawable(
            context: Context,
            colorNormal: Int, colorPress: Int, radius: Float, strokeWidth: Int,
            strokeColor: Int
        ): StateListDrawable? {
            var strokeWidth = strokeWidth
            val density = context.resources.displayMetrics.density
            val roundRadius = radius * density
            strokeWidth = (strokeWidth * density).toInt()
            val gdNormal = GradientDrawable()
            gdNormal.setColor(colorNormal)
            gdNormal.cornerRadius = roundRadius
            gdNormal.setStroke(strokeWidth, strokeColor)
            val gdPress = GradientDrawable()
            gdPress.setColor(colorPress)
            gdPress.cornerRadius = roundRadius
            gdPress.setStroke(strokeWidth, strokeColor)
            val drawable =
                StateListDrawable()
            drawable.addState(
                intArrayOf(-R.attr.state_pressed),
                gdNormal
            )
            drawable.addState(intArrayOf(R.attr.state_pressed), gdPress)
            return drawable
        }

        /**
         * @param radii       每个方向的圆角大小
         * @param colorNormal 正常颜色
         * @param colorPress  按下的颜色
         * @param radius      圆角
         * @param strokeWidth 边框
         * @param strokeColor 边框色
         */
        fun getStateListDrawable(
            context: Context, radii: FloatArray?,
            colorNormal: Int, colorPress: Int, radius: Float, strokeWidth: Int,
            strokeColor: Int
        ): StateListDrawable? {
            var strokeWidth = strokeWidth
            val density = context.resources.displayMetrics.density
            val roundRadius = radius * density
            strokeWidth = (strokeWidth * density).toInt()
            val gdNormal = GradientDrawable()
            gdNormal.setColor(colorNormal)
            gdNormal.cornerRadius = roundRadius
            gdNormal.cornerRadii = radii
            gdNormal.setStroke(strokeWidth, strokeColor)
            val gdPress = GradientDrawable()
            gdPress.setColor(colorPress)
            gdPress.cornerRadius = roundRadius
            gdPress.cornerRadii = radii
            val drawable =
                StateListDrawable()
            drawable.addState(
                intArrayOf(-R.attr.state_pressed),
                gdNormal
            )
            drawable.addState(intArrayOf(R.attr.state_pressed), gdPress)
            return drawable
        }
    }
}
