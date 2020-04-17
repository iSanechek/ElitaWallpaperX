package com.isanechek.elitawallpaperx.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.RationInfo
import com.isanechek.elitawallpaperx.utils.FilesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    fun loadImagesFromAssets(): LiveData<ExecuteResult<List<String>>>
    fun getBitmapUri(imagePath: String): LiveData<ExecuteResult<Uri>>
    fun isFirstStart(key: String): Boolean
    fun markDoneFirstStart(key: String)
    var selectionRation: Int
    fun loadRations(): List<RationInfo>
}

class AppRepositoryImpl(
    private val context: Context,
    private val preferences: SharedPreferences,
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

    override fun isFirstStart(key: String): Boolean = preferences.getBoolean(key, false)

    override fun markDoneFirstStart(key: String) {
        preferences.edit {
            putBoolean(key, true)
        }
    }

    override var selectionRation: Int
        get() = preferences.getInt("selection.ration", 2) // index 2 in list 16:9
        set(value) {
            preferences.edit {
                putInt("selection.ration", value)
            }
        }

    override fun loadRations(): List<RationInfo> = mutableListOf(
        RationInfo(title = "18:9", w = 9, h = 18),
        RationInfo(title = "9:18", w = 18, h = 9),
        RationInfo(title = "16:9", w = 9, h = 16),
        RationInfo(title = "9:16", w = 16, h = 9)
    )

    private fun String.fixPath(): String = "file:///android_asset/images/$this"

}