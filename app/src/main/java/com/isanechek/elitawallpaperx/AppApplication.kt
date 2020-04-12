package com.isanechek.elitawallpaperx

import android.app.Application

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

//        startKoin {
//            androidContext(this@AppApplication)
////            modules(settingsModule)
//        }
    }
}