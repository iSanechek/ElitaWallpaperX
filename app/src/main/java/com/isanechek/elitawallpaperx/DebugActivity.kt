package com.isanechek.elitawallpaperx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isanechek.elitawallpaperx.settings.SettingFragment
import kotlinx.android.synthetic.main.debug_activity.*

class DebugActivity : AppCompatActivity(_layout.debug_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(_id.debug_container, SettingFragment())
                .commit()
        }
    }
}