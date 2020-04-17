@file:JvmName("AppModule")

package com.isanechek.elitawallpaperx

import com.isanechek.elitawallpaperx.data.AppRepository
import com.isanechek.elitawallpaperx.data.AppRepositoryImpl
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import com.isanechek.elitawallpaperx.utils.FilesManager
import com.isanechek.elitawallpaperx.utils.FilesManagerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<FilesManager> {
        FilesManagerImpl()
    }

    single<AppRepository> {
        AppRepositoryImpl(androidApplication(), get())
    }

    viewModel {
        MainViewModel(androidApplication(), get())
    }
}