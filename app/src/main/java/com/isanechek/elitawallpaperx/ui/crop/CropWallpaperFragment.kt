package com.isanechek.elitawallpaperx.ui.crop

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.isanechek.elitawallpaperx._id
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.models.RationInfo
import com.isanechek.elitawallpaperx.onClick
import com.isanechek.elitawallpaperx.ui.base.bind
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import kotlinx.android.synthetic.main.croup_wallpaper_fragment_layout.*
import kotlinx.android.synthetic.main.ration_dialog_item_layout.view.*

class CropWallpaperFragment : Fragment(_layout.croup_wallpaper_fragment_layout) {

    private val path: String
        get() = arguments?.getString("path", "") ?: ""

    private val vm: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cwf_toolbar.hideCustomLayout()
        cwf_toolbar_close_btn.onClick { findNavController().navigateUp() }
        cwf_toolbar_menu_btn.onClick { showRationInfoDialog() }

        vm.loadUri(path).observe(viewLifecycleOwner, Observer { uri ->
            if (uri != Uri.EMPTY) {
                setupCropView(uri)
            } else d { "URI IS EMPTY" }
        })
    }


    private fun setupCropView(uri: Uri) {
        cwf_crop_view.apply {
            setImageUriAsync(uri)
            setAspectRatio(9, 18)
            setFixedAspectRatio(true)
            setOnCropImageCompleteListener { _, result ->
                if (result.isSuccessful) {

                }
            }
        }
        cwf_crop_btn.onClick {
            cwf_crop_view.getCroppedImageAsync()
        }
    }

    private fun showRationInfoDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Warning")
            message(text = "Default ration 16:9. For change click CHANGE or later you can cache in settings!")
            lifecycleOwner(this@CropWallpaperFragment)
            negativeButton(text = "close") {
                it.dismiss()
            }
            positiveButton(text = "change") {
                it.dismiss()
                showRationChangeDialog()
            }
        }
    }

    private fun showRationChangeDialog() {
        val rationList = mutableListOf(
            RationInfo(title = "18:9", w = 9, h = 18),
            RationInfo(title = "9:18", w = 18, h = 9),
            RationInfo(title = "16:9", w = 9, h = 16),
            RationInfo(title = "9:16", w = 16, h = 9)
        )
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(text = "Ration")
            listItemsSingleChoice(items = rationList.map { it.title }) { dialog, index, text ->

            }
            negativeButton(text = "close") {
                it.dismiss()
            }
        }
    }

}