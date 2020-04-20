package com.isanechek.elitawallpaperx.models

import android.net.Uri

data class BitmapInfo(val uri: Uri, val width: Int, val height: Int) {
    companion object {
        fun empty() = BitmapInfo(Uri.EMPTY,0, 0)
    }
}