package com.isanechek.elitawallpaperx

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.*
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.models.BitmapInfo
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.NewInfo
import com.isanechek.elitawallpaperx.models.RationInfo
import com.isanechek.elitawallpaperx.utils.LiveEvent
import com.isanechek.elitawallpaperx.utils.TrackerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class AppViewModel(
    application: Application,
    private val repository: AppRepository,
    private val tracker: TrackerUtils
) : AndroidViewModel(application) {

    init {
        checkTimeForShowAdsIcon()
    }

    private val _showToast = LiveEvent<String>()
    val showToast: LiveEvent<String>
        get() = _showToast

    private val _resetWallpaperStatus = LiveEvent<ExecuteResult<Int>>()
    val resetWallpaperStatus: LiveEvent<ExecuteResult<Int>>
        get() = _resetWallpaperStatus

    private val _installWallpaperStatus = LiveEvent<ExecuteResult<Int>>()
    val installWallpaperStatus: LiveEvent<ExecuteResult<Int>>
        get() = _installWallpaperStatus

    val data: LiveData<ExecuteResult<List<String>>>
        get() = repository.loadImagesFromAssets()

    private val loadUri = MutableLiveData<String>()
    val uri: LiveData<ExecuteResult<BitmapInfo>>
        get() = Transformations.switchMap(loadUri) { path ->
            repository.getBitmapUri(path)
        }

    val screenSize: Pair<Int, Int>
        get() = repository.loadWallpaperSize()

    fun loadUri(path: String) {
        loadUri.value = path
    }

    val whatIsNewData: LiveData<ExecuteResult<List<NewInfo>>>
        get() = liveData {
            emitSource(repository.loadWhatIsNewInfo())
        }

    val rations: List<RationInfo>
        get() = repository.loadRations()

    val getRationInfo: RationInfo
        get() = rations[selectionRatio]

    fun isFirstStart(key: String): Boolean = repository.isFirstStart(key)

    fun markFirstStartDone(key: String) {
        repository.markDoneFirstStart(key)
    }

    var selectionRatio: Int
        get() = repository.selectionRation
        set(value) {
            repository.selectionRation = value
        }

    val isShowAdsScreen: Boolean
        get() = repository.isShowAdsAnim()

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

    fun updateScreenSize(width: Int, height: Int) {
        viewModelScope.launch {
            repository.updateWallpaperSize(width, height)
        }
    }

    fun sendEvent(tag: String, event: String) {
        tracker.sendEvent(tag, event, viewModelScope)
    }

    fun sendException(tag: String, event: String, exception: Exception?) {
        tracker.sendException(tag, event, viewModelScope, exception)
    }

    fun resetWallpaper(which: Int) {
        viewModelScope.launch {
            repository.resetWallpaper(which)
                .flowOn(Dispatchers.IO)
                .catch { _showToast.value = "Reset wallpaper error!" }
                .collect { _resetWallpaperStatus.postValue(it) }
        }
    }

    fun setBlackWallpaper() {
        viewModelScope.launch {
            repository.installBlackWallpaper(screenSize.first, screenSize.second)
                .flowOn(Dispatchers.IO)
                .catch { _showToast.value = it.message }
                .collect {
                    debugLog { "INSTALL BLACK $it" }
                    _installWallpaperStatus.postValue(it)
                }
        }
    }

    fun cacheData() {

    }

    fun hideAdsScreen() {
        repository.showAnimation(false)
        repository.setTimeForUpdate(System.currentTimeMillis())
    }

    private fun checkTimeForShowAdsIcon() {
        val delayTime = if (BuildConfig.DEBUG) TimeUnit.SECONDS.toMillis(30) else TimeUnit.DAYS.toMillis(1)
        if (repository.isTimeUpdate(delayTime)) {
            if (!repository.isShowAdsAnim()) {
                repository.showAnimation(true)
            }
        }
    }
}