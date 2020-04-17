package com.isanechek.elitawallpaperx.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.utils.FilesManager
import kotlinx.coroutines.Dispatchers

interface AppRepository {
    fun loadImagesFromAssets(): LiveData<ExecuteResult<List<String>>>
    fun getBitmapUri(imagePath: String): LiveData<ExecuteResult<Uri>>
}

class AppRepositoryImpl(
    private val context: Context,
    private val filesManager: FilesManager
) : AppRepository {

    override fun loadImagesFromAssets(): LiveData<ExecuteResult<List<String>>> =
        liveData(Dispatchers.IO) {
            emit(ExecuteResult.Loading)
            val result = filesManager.loadImagesFromAssets(context)
            when {
                result.isNotEmpty() -> emit(ExecuteResult.Done(result.filter {
                    it.contains(
                        "moto_",
                        ignoreCase = true
                    )
                }.map { it.fixPath() }))
                else -> emit(ExecuteResult.Error(""))
            }
        }

    override fun getBitmapUri(imagePath: String): LiveData<ExecuteResult<Uri>> =
        liveData(Dispatchers.IO) {
            emit(ExecuteResult.Loading)
            val name = imagePath.replaceBefore("images", "").trim()
            when (val uri = filesManager.getBitmapUri(context, name)) {
                Uri.EMPTY -> emit(ExecuteResult.Error(""))
                else -> emit(ExecuteResult.Done(uri))
            }
        }

    private fun String.fixPath(): String = "file:///android_asset/images/$this"
}