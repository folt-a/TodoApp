package com.folta.todoapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class TileDrawable(drawable: Drawable, tileMode: Shader.TileMode) : Drawable() {

    private val paint: Paint

    companion object {
        fun create(
            context: Context,
            colorResId: Int,
            patternResId: Int,
            tileMode: Shader.TileMode
        ): TileDrawable? {
            val drawable = ContextCompat.getDrawable(context, patternResId)
            drawable?.let {
                it.setTint(ContextCompat.getColor(context, colorResId))
                return TileDrawable(it, tileMode)
            }
            return null
        }
    }

    init {
        paint = Paint().apply {
            shader = BitmapShader(getBitmap(drawable), tileMode, tileMode)
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(bmp)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(c)
        return bmp
    }

}