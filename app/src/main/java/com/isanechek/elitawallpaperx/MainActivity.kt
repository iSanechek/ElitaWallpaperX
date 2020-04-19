package com.isanechek.elitawallpaperx

import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(_layout.activity_main) {

    private val vm: AppViewModel by viewModel()

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    override fun onResume() {
        super.onResume()
        updateScreenSize()
    }

    private fun updateScreenSize() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        vm.updateScreenSize(size.x, size.y)
    }
}