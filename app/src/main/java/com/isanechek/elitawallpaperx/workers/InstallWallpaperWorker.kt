package com.isanechek.elitawallpaperx.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.debugLog
import com.isanechek.elitawallpaperx.models.ExecuteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class InstallWallpaperWorker(
    private val repository: AppRepository,
    appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        var resultWork = Result.success()

        when (params.inputData.getInt(WORKER_ACTION_KEY, DEFAULT_ACTION)) {
            DEFAULT_ACTION -> {
                debugLog { "Что-то пошло не так. Непонятно что акшен" }
            }
            INSTALL_BLACK_WALLPAPER_ACTION -> {
                debugLog { "Install black wallpaper" }
                repository.installBlackWallpaper(1200, 720)
                    .flowOn(Dispatchers.IO)
                    .collect { result ->
                        when (result) {
                            is ExecuteResult.Error -> {
                                resultWork = Result.failure()
                            }
                            is ExecuteResult.Done -> {
                                resultWork = Result.success()
                            }
                            else -> Unit
                        }

                    }
            }
            INSTALL_RANDOM_WALLPAPER_ACTION -> {
                launch(Dispatchers.IO) {
                    repository.installRandomWallpaper()
                }
            }
        }
        resultWork
    }


    companion object {
        private const val DEFAULT_ACTION = 0

        const val WORKER_ACTION_KEY = "worker_action_key"
        const val INSTALL_BLACK_WALLPAPER_ACTION = 1
        const val INSTALL_RANDOM_WALLPAPER_ACTION = 2

        fun createData(action: Int): Data = Data.Builder()
            .putInt(WORKER_ACTION_KEY, action)
            .build()
    }
}