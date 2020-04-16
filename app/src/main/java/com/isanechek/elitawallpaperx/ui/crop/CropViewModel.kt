package com.isanechek.elitawallpaperx.ui.crop

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel

class CropViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = getApplication()
    private var wm: WallpaperManager? = null


    fun setWallpaper(bitmap: Bitmap) {
        wm = WallpaperManager.getInstance(context)
        wm?.setBitmap(bitmap)
    }

    override fun onCleared() {

        super.onCleared()
    }
}