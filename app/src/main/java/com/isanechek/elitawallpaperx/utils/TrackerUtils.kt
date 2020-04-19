package com.isanechek.elitawallpaperx.utils

import android.util.Log
import com.isanechek.elitawallpaperx.BuildConfig
import kotlinx.coroutines.CoroutineScope
import java.lang.Exception

interface TrackerUtils {
    fun sendEvent(tag: String, event: String, scope: CoroutineScope)
    fun sendException(tag: String, event: String, scope: CoroutineScope, exception: Exception?)
}

class TrackerUtilsImpl : TrackerUtils {

    override fun sendEvent(tag: String, event: String, scope: CoroutineScope) {
        sendException(tag, event, scope, null)
    }

    override fun sendException(
        tag: String,
        event: String,
        scope: CoroutineScope,
        exception: Exception?
    ) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, event, exception)
        }
    }
}