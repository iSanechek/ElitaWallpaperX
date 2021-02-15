package com.isanechek.elitawallpaperx.ui.black

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.workers.InstallWallpaperWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

class InstallBlackWallpaperActivity : AppCompatActivity(_layout.black_wallpaper_install_activity_layout) {

    private val vm: AppViewModel by viewModel()
//    private val wm = WorkManager.getInstance(this)

    @KoinApiExtension
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when(intent.getIntExtra(ACTION_KEY, 0)) {
            1 -> {
                vm.setBlackWallpaper()
//                enqueueWork<InstallWallpaperWorker>(
//                    InstallWallpaperWorker.createData(
//                        InstallWallpaperWorker.INSTALL_BLACK_WALLPAPER_ACTION
//                    )
//                )
                finishAndRemoveTask()
            }
            2 -> {
                vm.installRandomWallpaper()
//                enqueueWork<InstallWallpaperWorker>(
//                    InstallWallpaperWorker.createData(
//                        InstallWallpaperWorker.INSTALL_RANDOM_WALLPAPER_ACTION
//                    )
//                )
                finishAndRemoveTask()
            }
            else -> openMain()
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

//    private inline fun <reified T : ListenableWorker> enqueueWork(data: Data): OneTimeWorkRequest {
//
//        val workName = InstallWallpaperWorker::class.simpleName + data.keyValueMap.getValue(
//            InstallWallpaperWorker.WORKER_ACTION_KEY
//        )
//
//        return OneTimeWorkRequestBuilder<T>()
//            .setInputData(data)
//            .build()
//            .also {
//                wm.enqueueUniqueWork(
//                    workName,
//                    ExistingWorkPolicy.APPEND_OR_REPLACE,
//                    it
//                )
//            }
//    }

    companion object {
        const val ACTION_KEY = "action_key"
        const val BLACK_WALLPAPER_INSTALL = 1
        const val RANDOM_WALLPAPER_INSTALL = 2
    }
}