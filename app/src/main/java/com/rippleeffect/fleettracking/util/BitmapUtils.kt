package com.rippleeffect.fleettracking.util

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import com.rippleeffect.fleettracking.R

object BitmapUtils {

     fun makeBitmap(context: Context, text: String): Bitmap? {
        val resources: Resources = context.resources
        val scale: Float = resources.displayMetrics.density
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_small)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE // Text color
        paint.textSize = 14 * scale // Text size
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE) // Text shadow
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bitmap
    }
}