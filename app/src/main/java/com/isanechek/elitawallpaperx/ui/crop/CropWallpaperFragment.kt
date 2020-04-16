package com.isanechek.elitawallpaperx.ui.crop

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import kotlinx.android.synthetic.main.croup_wallpaper_fragment_layout.*

class CropWallpaperFragment : Fragment(_layout.croup_wallpaper_fragment_layout) {

    private val path: String
        get() = arguments?.getString("path", "") ?: ""

    private val vm: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cwf_toolbar.setBackOrCloseButton {
            findNavController().navigateUp()
        }

        vm.loadUri(path).observe(viewLifecycleOwner, Observer { uri ->
            if (uri != Uri.EMPTY) {
                setupCropView(uri)
            } else d { "URI IS EMPTY" }
        })
    }


    private fun setupCropView(uri: Uri) {
        with(cwf_crop_view) {
            setImageUri(uri)
            setRatios(2.1f, 0.8f, 2.1f)
        }
        cwf_crop_btn.apply {
            cwf_crop_view.crop(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            ) { resultBitmap ->
                d { "w ${resultBitmap.width}" }
                d { "h ${resultBitmap.height}" }
            }
        }
    }

}