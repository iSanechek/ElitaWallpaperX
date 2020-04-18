package com.isanechek.elitawallpaperx.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.*
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.RationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


class MainViewModel(application: Application, private val repository: AppRepository) :
    AndroidViewModel(application) {

    private val _showToast = MutableLiveData<String>()
    val showToast: LiveData<String>
        get() = _showToast

    private val _installWallpaperStatus = MutableLiveData<ExecuteResult<Int>>()
    val installWallpaperStatus: LiveData<ExecuteResult<Int>>
        get() = _installWallpaperStatus

    val data: LiveData<ExecuteResult<List<String>>>
        get() = repository.loadImagesFromAssets()

    private val loadUri = MutableLiveData<String>()
    val uri: LiveData<ExecuteResult<Uri>>
        get() = Transformations.switchMap(loadUri) { path ->
            repository.getBitmapUri(path)
        }

    fun loadUri(path: String) {
        loadUri.value = path
    }

    val rations: List<RationInfo>
        get() = repository.loadRations()

    val getRationInfo: RationInfo
        get() = rations[selectionRatio]

    fun isFirstStart(key: String): Boolean = repository.isFirstStart(key)

    fun markFirstStartDone(key: String) {
        repository.markDoneFirstStart(key)
    }

    var selectionRatio: Int = repository.selectionRation

    fun installWallpaper(bitmap: Bitmap, screens: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            repository.installWallpaper(bitmap, screens)
                .flowOn(Dispatchers.IO)
                .catch { _showToast.value = "Install wallpaper error!" }
                .collect { result -> _installWallpaperStatus.value = result }
        }
    }

    fun showToast(message: String) {
        _showToast.value = message
    }
}