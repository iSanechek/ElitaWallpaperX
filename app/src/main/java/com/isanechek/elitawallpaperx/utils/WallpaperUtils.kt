package com.isanechek.elitawallpaperx.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.isanechek.elitawallpaperx.hasMinimumSdk
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface WallpaperUtils {
    suspend fun createBlackWallpaper(width: Int, height: Int): Bitmap
}

class WallpaperUtilsImpl : WallpaperUtils {

    fun installWallpaperSystem(ctx: Context, uri: Uri) {
        if (hasMinimumSdk(19)) {
            val intent = WallpaperManager.getInstance(ctx).getCropAndSetWallpaperIntent(uri)
            ctx.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_ATTACH_DATA)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.setDataAndType(uri, "image/jpeg")
            intent.putExtra("mimeType", "image/jpeg")
            ctx.startActivity(intent)
        }
    }

    override suspend fun createBlackWallpaper(width: Int, height: Int): Bitmap =
        suspendCancellableCoroutine { c ->
            val w = if (width == 0) 1000 else width
            val h = if (height == 0) 1000 else height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawPaint(paint)
            c.resume(bitmap)
        }
}