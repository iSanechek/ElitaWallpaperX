package com.isanechek.elitawallpaperx

import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(_layout.activity_main) {

    private val vm: AppViewModel by viewModel()

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeNavBarColor()
        Handler().postDelayed({
            controller.navigate(
                _id.go_splash_to_main,
                null,
                NavOptions.Builder()
                    .setPopUpTo(_id.splash_fragment, true)
                    .setEnterAnim(_anim.slide_up_anim)
                    .setExitAnim(_anim.alpha_out_anim)
                    .setPopExitAnim(_anim.alpha_out_anim)
                    .setPopEnterAnim(_anim.slide_up_anim)
                    .build()
            )
        }, 1000)
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

    private fun changeNavBarColor() {
        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != _id.splash_fragment) {
                if (hasMinimumSdk(21)) {
                    window.navigationBarColor =
                        ContextCompat.getColor(this, _color.my_primary_color)
                }
            }
        }
    }
}