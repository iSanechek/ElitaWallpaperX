package com.isanechek.elitawallpaperx.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.isanechek.elitawallpaperx._id
import com.isanechek.elitawallpaperx._layout
import kotlinx.android.synthetic.main.parent_settings_fragment_layout.*

class ParentSettingFragment : Fragment(_layout.parent_settings_fragment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        psf_toolbar.apply {
            setBackOrCloseButton {
                findNavController().navigateUp()
            }
            title = "Settings"
        }

        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(_id.psf_container, SettingFragment())
                .commit()
        }
    }
}