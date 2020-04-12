package com.isanechek.elitawallpaperx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity(_layout.activity_main) {

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()
}