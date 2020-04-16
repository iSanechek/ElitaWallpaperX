package com.isanechek.elitawallpaperx.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.isanechek.elitawallpaperx.hasMinimumSdk

object WallpaperUtils {

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
}