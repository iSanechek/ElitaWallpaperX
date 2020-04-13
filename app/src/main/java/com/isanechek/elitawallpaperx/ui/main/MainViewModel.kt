package com.isanechek.elitawallpaperx.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isanechek.elitawallpaperx.d
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = getApplication()

    private val _data = MutableLiveData<List<String>>()
    val data: LiveData<List<String>>
        get() = _data

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