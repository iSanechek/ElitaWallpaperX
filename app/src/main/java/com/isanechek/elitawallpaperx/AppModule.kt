@file:JvmName("AppModule")

package com.isanechek.elitawallpaperx

import android.content.Context
import android.content.SharedPreferences
import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.data.AppRepositoryImpl
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import com.isanechek.elitawallpaperx.utils.FilesManager
import com.isanechek.elitawallpaperx.utils.FilesManagerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    single<FilesManager> {
        FilesManagerImpl()
    }

    single<AppRepository> {
        AppRepositoryImpl(androidApplication(), get(), get())
    }

    viewModel {
        MainViewModel(androidApplication(), get())
    }

    single {
        androidContext()
            .applicationContext
            .getSharedPreferences("wallpaperx", Context.MODE_PRIVATE)
    } bind (SharedPreferences::class)
}