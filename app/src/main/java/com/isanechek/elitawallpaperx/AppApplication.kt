package com.isanechek.elitawallpaperx

import android.app.Application
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class AppApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        initMetrica()
        MobileAds.initialize(this) {}
        startKoin {
            androidContext(this@AppApplication)
            modules(appModule)
        }
    }

    private fun initMetrica() {
        val config =
            YandexMetricaConfig.newConfigBuilder("3cafef52-a4aa-4388-80a9-bccfdbbab0d1")
                .withSessionTimeout(15)
                .withAppVersion(BuildConfig.VERSION_NAME)
                .build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .build()
}