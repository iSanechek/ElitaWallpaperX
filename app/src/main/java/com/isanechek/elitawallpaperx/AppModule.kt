@file:JvmName("AppModule")

package com.isanechek.elitawallpaperx

import android.content.Context
import android.content.SharedPreferences
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.data.AppRepositoryImpl
import com.isanechek.elitawallpaperx.utils.*
import com.isanechek.elitawallpaperx.workers.InstallWallpaperWorker
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    single<TrackerUtils> { TrackerUtilsImpl() }

    single<FilesManager> {
        FilesManagerImpl(get())
    }

    factory<WallpaperUtils> {
        WallpaperUtilsImpl()
    }

    single<AppRepository> {
        AppRepositoryImpl(androidApplication(), get(), get(), get())
    }

    viewModel {
        AppViewModel(androidApplication(), get(), get())
    }

    single {
        androidContext()
            .applicationContext
            .getSharedPreferences("wallpaperx", Context.MODE_PRIVATE)
    } bind (SharedPreferences::class)

//    worker {
//        InstallWallpaperWorker(get(), get(), get())
//    }
}