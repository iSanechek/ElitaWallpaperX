package com.isanechek.elitawallpaperx.data

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.hasIsNotMiUi
import com.isanechek.elitawallpaperx.hasMinimumSdk
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.RationInfo
import com.isanechek.elitawallpaperx.utils.FilesManager
import com.isanechek.elitawallpaperx.utils.WallpaperUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface AppRepository {
    fun loadImagesFromAssets(): LiveData<ExecuteResult<List<String>>>
    fun getBitmapUri(imagePath: String): LiveData<ExecuteResult<Uri>>
    fun isFirstStart(key: String): Boolean
    fun markDoneFirstStart(key: String)
    var selectionRation: Int
    fun loadRations(): List<RationInfo>
    suspend fun installWallpaper(bitmap: Bitmap, screens: Int): Flow<ExecuteResult<Int>>
    suspend fun resetWallpaper(which: Int): Flow<ExecuteResult<Int>>
    suspend fun updateWallpaperSize(width: Int, height: Int)
    suspend fun installBlackWallpaper(w: Int, h: Int): Flow<ExecuteResult<Int>>
    fun loadWallpaperSize(): Pair<Int, Int>
}

class AppRepositoryImpl(
    private val context: Context,
    private val preferences: SharedPreferences,
    private val filesManager: FilesManager,
    private val wallpaperUtils: WallpaperUtils
) : AppRepository {

    private val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(context)
    }

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

    override fun isFirstStart(key: String): Boolean = preferences.getBoolean(key, true)

    override fun markDoneFirstStart(key: String) {
        preferences.edit {
            putBoolean(key, false)
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

    /**
     * 0 - all screens
     * 1 - only system screen
     * 2 - only lock screen
     */
    override suspend fun installWallpaper(bitmap: Bitmap, screens: Int): Flow<ExecuteResult<Int>> = flow {
        emit(ExecuteResult.Loading)
        if (hasMinimumSdk(24)) {
            val result = when (screens) {
                0 -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                1 -> {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                2 -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                else -> 0
            }
            if (result == 0) {
                emit(ExecuteResult.Error("Wallpaper not installing! Unknown error!"))
            } else emit(ExecuteResult.Done(0))
        } else {
            wallpaperManager.setBitmap(bitmap)
            emit(ExecuteResult.Done(0))
        }
    }

    override suspend fun resetWallpaper(which: Int): Flow<ExecuteResult<Int>> = flow {
        emit(ExecuteResult.Loading)
        if (hasMinimumSdk(24)) {
            when(which) {
                0 -> wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)
                1 -> {
                    wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)
                    wallpaperManager.clear(WallpaperManager.FLAG_LOCK)
                }
                2 -> wallpaperManager.clear(WallpaperManager.FLAG_LOCK)
            }
            emit(ExecuteResult.Done(0))
        } else {
            wallpaperManager.clear()
            emit(ExecuteResult.Done(0))
        }
    }

    override suspend fun updateWallpaperSize(width: Int, height: Int) = withContext(Dispatchers.IO) {
        preferences.edit { putInt(SYSTEM_WALLPAPER_WIDTH_KEY, width) }
        preferences.edit { putInt(SYSTEM_WALLPAPER_HEIGHT_KEY, height) }

    }

    override suspend fun installBlackWallpaper(w: Int, h: Int): Flow<ExecuteResult<Int>> = flow {
        emit(ExecuteResult.Loading)
        val blackWallpaper = wallpaperUtils.createBlackWallpaper(w, h)
        if (hasMinimumSdk(24) && hasIsNotMiUi) {
            val system =wallpaperManager.setBitmap(blackWallpaper, null, true, WallpaperManager.FLAG_SYSTEM)
            val lock = wallpaperManager.setBitmap(blackWallpaper, null, true, WallpaperManager.FLAG_LOCK)
            if (system != 0 && lock != 0) {
                emit(ExecuteResult.Done(0))
            } else emit(ExecuteResult.Error("Set black wallpaper error!"))
        } else {
            wallpaperManager.setBitmap(blackWallpaper)
            emit(ExecuteResult.Done(0))
        }
    }

    override fun loadWallpaperSize(): Pair<Int, Int> {
        val width = preferences.getInt(SYSTEM_WALLPAPER_WIDTH_KEY, wallpaperManager.desiredMinimumWidth)
        val height = preferences.getInt(SYSTEM_WALLPAPER_HEIGHT_KEY, wallpaperManager.desiredMinimumHeight)
        return Pair(width, height)
    }


    private fun String.fixPath(): String = "file:///android_asset/images/$this"

    companion object {
        private const val SYSTEM_WALLPAPER_WIDTH_KEY = "s.w.w.k"
        private const val SYSTEM_WALLPAPER_HEIGHT_KEY = "s.w.h.k"
    }

}