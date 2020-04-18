package com.isanechek.elitawallpaperx.ui.crop

import android.graphics.Bitmap
import android.graphics.Rect
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
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.ui.main.MainViewModel
import com.isanechek.elitawallpaperx.utils.WARNING_INSTALL_LOCK_SCREEN
import com.isanechek.elitawallpaperx.utils.WARNING_RATION_DIALOG_KEY
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.croup_wallpaper_fragment_layout.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CropWallpaperFragment : Fragment(_layout.croup_wallpaper_fragment_layout) {

    private var currentUri: Uri = Uri.EMPTY

    private val path: String
        get() = arguments?.getString("path", "") ?: ""

    private val vm: MainViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.loadUri(path)
        cwf_toolbar.hideCustomLayout()
        cwf_toolbar_close_btn.onClick { findNavController().navigateUp() }
        cwf_toolbar_menu_btn.onClick { showSettingsDialog() }

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

        vm.installWallpaperStatus.observe(viewLifecycleOwner, Observer { status ->
            when(status) {
                is ExecuteResult.Loading -> {}
                is ExecuteResult.Done -> {
                    vm.showToast("Install done")
                }
                is ExecuteResult.Error -> {}
            }
        })

        vm.showToast.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onResume() {
        super.onResume()
        if (vm.isFirstStart(WARNING_RATION_DIALOG_KEY)) {
            showRatioWarningInfoDialog()
        }
    }


    private fun updateCropView() {
        if (currentUri != Uri.EMPTY) {
            val item = vm.getRationInfo
            d { item.toString() }
            cwf_crop_view.apply {
                setImageUriAsync(currentUri)
                val screenSize = vm.screenSize
//                setMinCropResultSize(screenSize.first, screenSize.second)
                setAspectRatio(item.w, item.h)
                setFixedAspectRatio(true)
                setOnSetCropOverlayMovedListener { rect ->
                    if (rect != null) {
                        cwf_toolbar_width_tv.text = String.format("%d", rect.width())
                        cwf_toolbar_height_tv.text = String.format("%d", rect.height())
                    }
                }
                setOnCropImageCompleteListener { _, result ->
                    if (result.isSuccessful) {
                        if (hasMinimumSdk(24) && hasIsNotMiUi) {
                            showScreensChoiceDialog(result.bitmap)
                        } else {
                            vm.installWallpaper(result.bitmap, 0)
                        }
                    }
                }
            }
            cwf_crop_btn.onClick {
                cwf_crop_view.getCroppedImageAsync()
            }
        } else {
            vm.showToast("Uri is empty!")
        }
    }

    private fun showScreensChoiceDialog(bitmap: Bitmap) {
        val screens = mutableListOf("all screens", "system screen", "lock screens")
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(text = "Choice screen")
            listItems(items = screens) { dialog, index, text ->
                dialog.dismiss()
                showWarningLockScreen {
                    vm.installWallpaper(bitmap, index)
                }
            }
        }
    }

    private fun showRatioWarningInfoDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Warning")
            message(text = "Default ration 16:9. For change click CHANGE or later you can cache in settings!")
            lifecycleOwner(this@CropWallpaperFragment)
            negativeButton(text = "close") {
                it.dismiss()
            }
            positiveButton(text = "change") {
                it.dismiss()
                showRatioChangeDialog()
            }
        }.onDismiss {
            vm.markFirstStartDone(WARNING_RATION_DIALOG_KEY)
        }
    }

    private fun showRatioChangeDialog() {
        val rationList = vm.rations
        MaterialDialog(requireContext()).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(text = "Select ration")
            listItemsSingleChoice(
                items = rationList.map { it.title },
                initialSelection = vm.selectionRatio
            ) { _, index, text ->
                if (vm.selectionRatio != index) {
                    vm.showToast(String.format("Выбрано %s \n Значание обновлено!", text))
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

    private fun showSettingsDialog() {
        val items = mutableListOf("Ration", "")
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(text = "Settings")
            listItems(items = items) { dialog, index, text ->

            }

            positiveButton(text = "close") {
                it.dismiss()
            }
        }
    }

    private fun showWarningLockScreen(callback: () -> Unit) {
        if (vm.isFirstStart(WARNING_INSTALL_LOCK_SCREEN)) {
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(text = "Warning")
                message(text = "Some device not supported install wallpaper in lock screen!")
                positiveButton(text = "done") {
                    it.dismiss()
                    callback.invoke()
                }
            }.onDismiss {
                vm.markFirstStartDone(WARNING_INSTALL_LOCK_SCREEN)
            }
        } else callback.invoke()
    }

}