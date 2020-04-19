package com.isanechek.elitawallpaperx.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.isanechek.elitawallpaperx._layout

class SplashFragment : Fragment(_layout.splash_sreen_fragment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
//        if (ssf_lottie.isAnimating) {
//            ssf_lottie.cancelAnimation()
//        }
        super.onPause()
    }
}