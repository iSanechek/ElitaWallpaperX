package com.isanechek.elitawallpaperx.ui.crop

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.onClick
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import kotlinx.android.synthetic.main.croup_wallpaper_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CropWallpaperFragment : Fragment(_layout.croup_wallpaper_fragment_layout) {

    private var currentUri: Uri = Uri.EMPTY

    private val path: String
        get() = arguments?.getString("path", "") ?: ""

    private val vm: MainViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cwf_toolbar.hideCustomLayout()
        cwf_toolbar_close_btn.onClick { findNavController().navigateUp() }
        cwf_toolbar_menu_btn.onClick { showRationInfoDialog() }

        vm.uri.observe(viewLifecycleOwner, Observer { data ->
            when (data) {
                is ExecuteResult.Done -> {
                    currentUri = data.data
                    updateCropView()
                }
                is ExecuteResult.Loading -> {
                }
                is ExecuteResult.Error -> {
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        vm.loadUri(path)
    }


    private fun updateCropView() {
        if (currentUri != Uri.EMPTY) {
            val item = vm.getRationInfo
            d { item.toString() }
            cwf_crop_view.apply {
                setImageUriAsync(currentUri)
                setAspectRatio(item.w, item.h)
                setFixedAspectRatio(true)
                setOnCropImageCompleteListener { _, result ->
                    if (result.isSuccessful) {

                    }
                }
            }
            cwf_crop_btn.onClick {
                cwf_crop_view.getCroppedImageAsync()
            }
        } else {
            showToast("Uri is empty!")
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
        val rationList = vm.rations
        MaterialDialog(requireContext()).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(text = "Select ration")
            listItemsSingleChoice(
                items = rationList.map { it.title },
                initialSelection = vm.selectionRatio
            ) { _, index, text ->
                if (vm.selectionRatio != index) {
                    showToast(String.format("Выбрано %s \n Значание обновлено!", text))
                    vm.selectionRatio = index
                    updateCropView()
                }
            }
            positiveButton(text = "save") {
                it.dismiss()
            }
            negativeButton(text = "close") {
                it.dismiss()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}