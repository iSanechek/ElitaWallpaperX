package com.isanechek.elitawallpaperx.ui.main

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.isanechek.elitawallpaperx.BuildConfig
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.utils.FilesManager
import com.isanechek.elitawallpaperx.utils.FilesManagerImpl
import com.isanechek.elitawallpaperx.utils.WallpaperUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream


class MainViewModel(application: Application) :
    AndroidViewModel(application) {

    private val context: Context = getApplication()
    private var filesManager: FilesManager? = null

    private val _data = MutableLiveData<List<String>>()
    val data: LiveData<List<String>>
        get() = _data

    init {
        filesManager = FilesManagerImpl()
    }

    fun installWallpaper(path: String) {
        d { "Path $path" }
        viewModelScope.launch(Dispatchers.Main) {
            val uri = filesManager?.getBitmapUri(context, path)
            if (uri != null && uri != Uri.EMPTY) {
                WallpaperUtils.installWallpaperSystem(context, uri)
            } else d { "URI IS NULL OR EMPTY!" }
        }
    }

    private val _uri = MutableLiveData<Uri>()
    val uri: LiveData<Uri>
        get() = _uri

    fun loadUri(path: String): LiveData<Uri> = liveData(Dispatchers.IO) {
        val uri = filesManager!!.getBitmapUri(context, path)
        emit(uri)
    }

    fun loadWallpapers() {
        viewModelScope.launch(Dispatchers.Main) {
            val data = data()
            val result =
                data.filter { it.contains("moto_", ignoreCase = true) }
                    .map { it.fixPath() }
                    .toList()
            _data.value = result
        }
    }

    private fun String.fixPath(): String = "file:///android_asset/images/$this"

    private suspend fun data(): List<String> = withContext(Dispatchers.IO) {
        val am = context.assets
        val paths = am.list("images")
        paths?.toList() ?: emptyList()
    }

}