package com.isanechek.elitawallpaperx.ui.ads

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.isanechek.elitawallpaperx._color
import com.isanechek.elitawallpaperx._layout
import kotlinx.android.synthetic.main.ads_fragment_layout.*

class AdsFragment : Fragment(_layout.ads_fragment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        af_toolbar.setBackOrCloseButton(tintColor = _color.my_white_color) {
            findNavController().navigateUp()
        }
    }
}