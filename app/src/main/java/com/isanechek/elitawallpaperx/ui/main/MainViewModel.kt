package com.isanechek.elitawallpaperx.ui.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.RationInfo


class MainViewModel(application: Application, private val repository: AppRepository) :
    AndroidViewModel(application) {

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
}