package com.isanechek.elitawallpaperx.debug

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.isanechek.elitawallpaperx.R
import com.isanechek.elitawallpaperx.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity(R.layout.debug_activity) {

    private val repo: AppRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            repo.loadWhatIsNewInfo().observe(this@DebugActivity, Observer { data -> })
        }

    }
}