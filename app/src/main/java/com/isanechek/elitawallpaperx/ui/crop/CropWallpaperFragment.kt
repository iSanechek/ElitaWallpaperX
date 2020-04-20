package com.isanechek.elitawallpaperx.ui.crop

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.AppViewModel
import com.isanechek.elitawallpaperx.models.BitmapInfo
import com.isanechek.elitawallpaperx.models.ItemMenu
import com.isanechek.elitawallpaperx.ui.base.bindAdater
import com.isanechek.elitawallpaperx.utils.WARNING_INSTALL_LOCK_SCREEN
import com.isanechek.elitawallpaperx.utils.WARNING_RATION_DIALOG_KEY
import com.isanechek.elitawallpaperx.utils.WARNING_SCREEN_SIZE
import kotlinx.android.synthetic.main.croup_wallpaper_fragment_layout.*
import kotlinx.android.synthetic.main.settings_custom_item_layout.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.round

class CropWallpaperFragment : Fragment(_layout.croup_wallpaper_fragment_layout) {

    private var currentInfo: BitmapInfo = BitmapInfo.empty()

    private val path: String
        get() = arguments?.getString("path", "") ?: ""

    private val vm: AppViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.loadUri(path)
        cwf_toolbar.hideCustomLayout()
        cwf_toolbar_close_btn.onClick { findNavController().navigateUp() }
        cwf_toolbar_menu_btn.onClick { showMenuDialog() }

        vm.uri.observe(viewLifecycleOwner, Observer { data ->
            when (data) {
                is ExecuteResult.Done -> {
                    currentInfo = data.data
                    updateCropView()
                    statusProgress()
                }
                is ExecuteResult.Loading -> {
                    statusProgress(true)
                }
                is ExecuteResult.Error -> {
                    statusProgress()
                    vm.sendEvent(TAG, "Load wallpaper error! ${data.errorMessage}")
                }
            }
        })

        vm.installWallpaperStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is ExecuteResult.Loading -> {
                    statusProgress(true)
                }
                is ExecuteResult.Done -> {
                    statusProgress()
                    vm.showToast(getString(_string.install_wallpaper_done))
                }
                is ExecuteResult.Error -> {
                    statusProgress()
                    vm.sendEvent(TAG, "Install wallpaper error! ${status.errorMessage}")
                }
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
        if (currentInfo.uri != Uri.EMPTY) {
            val screenSize = vm.screenSize
            val item = vm.getRationInfo
            d { item.toString() }
            cwf_crop_view.apply {
                setImageUriAsync(currentInfo.uri)

//                setMinCropResultSize(screenSize.first, screenSize.second)
                setAspectRatio(item.w, item.h)
                setFixedAspectRatio(true)
                setOnSetCropOverlayMovedListener { rect ->
                    if (rect != null) {
                        when {
                            rect.width() < round(currentInfo.width / 1.3) && rect.height() < round(
                                currentInfo.height / 1.3
                            ) -> {
                                if (cwf_toolbar_warning_tv.isInvisible) {
                                    cwf_toolbar_warning_tv.apply {
                                        isInvisible = false
                                        onClick {
                                            d { "BOOOOOM" }
                                            showWarningScreenSizeDialog(
                                                rect.width(),
                                                rect.height(),
                                                currentInfo.width,
                                                currentInfo.height
                                            )
                                        }
                                    }
                                }
                            }
                            cwf_toolbar_warning_tv.isVisible -> cwf_toolbar_warning_tv.isInvisible =
                                true
                        }
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

    private fun showWarningScreenSizeDialog(
        currentW: Int,
        currentH: Int,
        bitmapW: Int,
        bitmapH: Int
    ) {
        if (vm.isFirstStart(WARNING_SCREEN_SIZE)) {
            d { "Boom" }
            MaterialDialog(requireContext()).show {
                title(res = _string.warning_title)
                val msg = String.format(
                    "%s\n%s - %dx%d\n%s - %dx%d",
                    getString(_string.resolution_low_warning_msg),
                    getString(_string.original_wallpaper_size_title),
                    bitmapW,
                    bitmapH,
                    getString(_string.crop_wallpaper_size_title),
                    currentW,
                    currentH
                )
                message(text = msg)
                positiveButton(res = _string.close_title) {
                    it.dismiss()
                }
//                checkBoxPrompt(res = _string.save_title) {
//                    if (it) {
//                        vm.markFirstStartDone(WARNING_SCREEN_SIZE)
//                    }
//                }
            }
        }
    }

    private fun showScreensChoiceDialog(bitmap: Bitmap) {
        val screens = mutableListOf(
            getText(_string.all_screens_title),
            getText(_string.system_screen_title),
            getText(_string.lock_screen_title)
        )
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@CropWallpaperFragment)
            title(res = _string.choice_screen_title)
            listItems(items = screens) { dialog, index, _ ->
                dialog.dismiss()
                showWarningLockScreen {
                    vm.installWallpaper(bitmap, index)
                }
            }
        }
    }

    private fun showRatioWarningInfoDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(res = _string.warning_title)
            message(res = _string.default_ration_message)
            lifecycleOwner(this@CropWallpaperFragment)
            negativeButton(res = _string.close_title) {
                it.dismiss()
            }
            positiveButton(res = _string.change_title) {
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
            title(res = _string.select_ratio_title)
            listItemsSingleChoice(
                items = rationList.map { it.title },
                initialSelection = vm.selectionRatio
            ) { _, index, text ->
                if (vm.selectionRatio != index) {
                    vm.showToast(
                        String.format(
                            "%s %s\n%s",
                            getText(_string.select_text),
                            text,
                            getText(_string.value_update_message)
                        )
                    )
                    vm.selectionRatio = index
                    updateCropView()
                }
            }
            positiveButton(res = _string.save_title) {
                it.dismiss()
            }
            negativeButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showMenuDialog() {
        val menuItems = listOf(
            ItemMenu(
                id = "ratio",
                titleId = _string.ratio_title,
                iconId = _drawable.ic_baseline_aspect_ratio_24
            ),
            ItemMenu(
                id = "crop",
                titleId = _string.minimum_crop_size_title,
                iconId = _drawable.ic_baseline_crop_free_24
            )
        )
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            val dialog = this
            lifecycleOwner(this@CropWallpaperFragment)
            title(res = _string.menu_title)
            customListAdapter(
                adapter = bindAdater(
                    menuItems,
                    _layout.settings_custom_item_layout
                ) { item: ItemMenu ->
                    sci_container.onClick {
                        when (item.id) {
                            "ratio" -> showRatioChangeDialog()
                            "crop" -> Unit
                        }
                        dialog.dismiss()
                    }
                    sci_icon.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            item.iconId
                        )
                    )
                    sci_title_tv.text = getText(item.titleId)
                })

            positiveButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showWarningLockScreen(callback: () -> Unit) {
        if (vm.isFirstStart(WARNING_INSTALL_LOCK_SCREEN)) {
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(res = _string.warning_title)
                message(res = _string.warning_message_not_supported_lock_screen)
                positiveButton(res = _string.close_title) {
                    it.dismiss()
                    callback.invoke()
                }
            }.onDismiss {
                vm.markFirstStartDone(WARNING_INSTALL_LOCK_SCREEN)
            }
        } else callback.invoke()
    }

    private fun statusProgress(show: Boolean = false) {
        when {
            show -> if (cwf_toolbar_progress.isInvisible) cwf_toolbar_progress.isInvisible = false
            cwf_toolbar_progress.isVisible -> cwf_toolbar_progress.isInvisible = true
        }
    }

    companion object {
        private const val TAG = "CropFragment"
    }

}