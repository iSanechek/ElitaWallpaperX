package com.isanechek.elitawallpaperx.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isanechek.elitawallpaperx.R
import com.isanechek.elitawallpaperx.ui.settings.SettingFragment

class DebugActivity : AppCompatActivity(R.layout.debug_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.debug_container, SettingFragment())
                .commit()
        }
    }
}