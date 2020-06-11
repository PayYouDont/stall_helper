package com.gospell.stall.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircleImageView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {
    private val mPaintBitmap = Paint(Paint.ANTI_ALIAS_FLAG) //抗锯齿

    private var mRawBitmap: Bitmap? = null
    private var mShader: BitmapShader? = null
    private val mMatrix = Matrix()
    private var strokeColor = -0x1 //默认边框是白色
    private var strokeWidth = 0f //单位是像素的边框宽度
    override fun onDraw(canvas: Canvas) {
        //获取资源图片并转为Bitmap
        val rawBitmap = getBitmap(drawable)
        if (rawBitmap != null) {
            //取较短的那一个作为圆的半径，保证整张图能填满整个圆
            val viewWidth = width
            val viewHeight = height
            val viewMinSize = Math.min(viewWidth, viewHeight)
            val dstWidth = viewMinSize.toFloat()
            val dstHeight = viewMinSize.toFloat()
            if (mShader == null || rawBitmap != mRawBitmap) {
                mRawBitmap = rawBitmap
                mShader = BitmapShader(mRawBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            if (mShader != null) {
                mMatrix.setScale(dstWidth / rawBitmap.width, dstHeight / rawBitmap.height)
                mShader!!.setLocalMatrix(mMatrix)
            }
            mPaintBitmap.shader = mShader
            val radius = viewMinSize / 2.0f

            // 如果边框宽度不为0,则画出边框
            if (strokeWidth != 0f) {
                val whitePaint =
                    Paint(Paint.ANTI_ALIAS_FLAG)
                whitePaint.color = strokeColor
                // 首先画一个圆，填充的是边框的颜色,大小就是此控件设置的大小
                canvas.drawCircle(radius, radius, radius, whitePaint)
                // 在边框的圆的基础上再画一个圆，画的是图片，半径 = 此控件设置的大小 - 边框宽度，就露出了边框
                canvas.drawCircle(radius, radius, radius - strokeWidth, mPaintBitmap)
            } else {
                // 如果边框为0，直接画一个圆形图片即可
                canvas.drawCircle(radius, radius, radius, mPaintBitmap)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    private fun getBitmap(drawable: Drawable): Bitmap? {
       return when(drawable){
            is BitmapDrawable -> drawable.bitmap
            is ColorDrawable -> {
                val rect = drawable.getBounds()
                val width = rect.right - rect.left
                val height = rect.bottom - rect.top
                val color = drawable.color
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawARGB(
                    Color.alpha(color),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                bitmap
            }
            else -> null
        }
    }

    /**
     * @param strokeWidth 要设置的边框宽度，单位是px
     */
    fun setStrokeWidth(strokeWidth: Int) {
        this.strokeWidth = strokeWidth.toFloat()
    }

    /**
     * @param strokeColor 要设置的边框颜色，必须是带透明度的16进制，例如：0xFF0000FF
     */
    fun setStrokeColot(strokeColor: Int) {
        this.strokeColor = strokeColor
    }
}