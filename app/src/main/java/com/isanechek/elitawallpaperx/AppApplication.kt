package com.isanechek.elitawallpaperx

import android.app.Application
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        startKoin {
            androidContext(this@AppApplication)
            modules(appModule)
        }
    }
}