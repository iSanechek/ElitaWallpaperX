package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.customListAdapter
import com.airbnb.lottie.LottieAnimationView
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.ItemMenu
import com.isanechek.elitawallpaperx.models.NewInfo
import com.isanechek.elitawallpaperx.ui.base.bindAdater
import kotlinx.android.synthetic.main.main_fragment_layout.*
import kotlinx.android.synthetic.main.settings_custom_item_layout.view.*
import kotlinx.android.synthetic.main.what_is_new_item_layout.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class MainFragment : Fragment(_layout.main_fragment_layout) {

    private val vm: AppViewModel by sharedViewModel()
    private val pagerAdapter by lazy { MainPagerAdapter() }
    private val mainAdapter by lazy { MainWallpapersAdapter() }
    private val pagerListener = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            mf_toolbar_counter.text =
                String.format("%d/%d", position.plus(1), pagerAdapter.itemCount)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mf_toolbar.hideCustomLayout()
        mf_toolbar_title.text = getString(_string.app_name)

        // pager
        with(mf_pager) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = pagerAdapter
            registerOnPageChangeCallback(pagerListener)
        }

        pagerAdapter.setOnListenerCallback(object : MainPagerAdapter.ListenerCallback {
            override fun onItemClick(data: String, position: Int) {
                findNavController().navigate(_id.main_go_detail_fragment, bundleOf("path" to data))
            }
        })

        mf_fab.onClick {
            showDialog()
        }

        mf_menu_btn.onClick {
            showSettingsDialog()
        }

        mf_company_tv.onClick {
            showAboutDialog()
        }

        mf_toolbar_counter.onClick {
            // тут надо заимплементить посхалку
        }

        mf_toolbar_ads_btn.onClick {
            findNavController().navigate(_id.main_go_ads_fragment)
        }


        lifecycleScope.launchWhenResumed {

            if (vm.isShowAdsScreen) {
                mf_toolbar_ads_lottie.apply {
                    if (isInvisible) isInvisible = false
                    onClick {
                        findNavController().navigate(_id.main_go_ads_fragment)
                    }
                }

                tickerFlow(period = 10000)
                    .flowOn(Dispatchers.Main)
                    .onEach { mf_toolbar_ads_lottie.update() }
                    .launchIn(this)

            } else {
                if (mf_toolbar_ads_lottie.isVisible) mf_toolbar_ads_lottie.isInvisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupObserver()
    }

    override fun onPause() {
        mf_pager.unregisterOnPageChangeCallback(pagerListener)
        super.onPause()
    }

    override fun onDestroy() {
        mf_pager.unregisterOnPageChangeCallback(pagerListener)
        super.onDestroy()
    }

    private fun setupObserver() {

        vm.data.observe(viewLifecycleOwner, Observer { data ->
            when (data) {
                is ExecuteResult.Error -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    vm.sendEvent(TAG, "Load images from assets error! ${data.errorMessage}")
                    vm.showToast(data.errorMessage)
                }
                is ExecuteResult.Loading -> {
                    if (mf_toolbar_progress.isInvisible) mf_toolbar_progress.isInvisible = false
                }
                is ExecuteResult.LoadingWithStatus -> {
                    if (mf_toolbar_progress.isInvisible) mf_toolbar_progress.isInvisible = false
                }
                is ExecuteResult.Done -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    pagerAdapter.submit(data.data)
                    mainAdapter.submit(data.data)
                }
            }
        })

        vm.resetWallpaperStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is ExecuteResult.Done -> {
                    vm.showToast(getString(_string.done_title))
                }
                is ExecuteResult.Loading -> {
                }
                is ExecuteResult.Error -> {
                    vm.showToast(getString(_string.reset_wallpaper_fail_msg))
                }
            }
        })
    }

    private fun showDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            customListAdapter(
                adapter = mainAdapter,
                layoutManager = GridLayoutManager(requireContext(), 3)
            )
            positiveButton(res = _string.close_title) {
                it.dismiss()
            }
        }.onShow {
            mainAdapter.setClickListener(object : MainWallpapersAdapter.ClickListener {
                override fun onItemClick(position: Int) {
                    it.dismiss()
                    it.onDismiss {
                        mf_pager.currentItem = position
                    }
                }
            })
        }
    }

    private fun showSettingsDialog() {
        val menuItems = listOf(
            ItemMenu(
                id = "remove",
                iconId = _drawable.image_remove,
                titleId = _string.clear_wallpaper_title
            ),
            ItemMenu(
                id = "black",
                iconId = _drawable.black_image,
                titleId = _string.black_wallpaper_title
            ),
            ItemMenu(id = "new", iconId = _drawable.new_box, titleId = _string.what_is_new_title),
            ItemMenu(
                id = "info",
                iconId = _drawable.ic_baseline_info_24,
                titleId = _string.about_info_title
            )
        )

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            val dialog = this
            lifecycleOwner(this@MainFragment)
            title(res = _string.menu_title)
            customListAdapter(
                adapter = bindAdater(
                    menuItems,
                    _layout.settings_custom_item_layout
                ) { item: ItemMenu ->
                    sci_container.onClick {

                        when (item.id) {
                            "remove" -> showRemoveWallpaperDialog()
                            "black" -> showBlackWallpaperDialog()
                            "new" -> showWhatNewDialog()
                            "info" -> showAboutDialog()
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

            negativeButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showRemoveWallpaperDialog() {
        var isLockScreen = false
        MaterialDialog(requireContext()).show {
            lifecycleOwner(this@MainFragment)

            customView(viewRes = _layout.custom_screen_dialog_layout)
            if (hasMinimumSdk(24) && hasIsNotMiUi) {
                checkBoxPrompt(res = _string.lock_screen_title) {
                    isLockScreen = it
                }
            }

            onShow {
                val root = it.getCustomView()
                root.findViewById<TextView>(_id.wsd_msg).text =
                    getString(_string.reset_to_default_wallpaper_msg)
                val lottie = root.findViewById<LottieAnimationView>(_id.wsd_lottie)
                lottie.apply {
                    update(_raw.dialog_alert)
                    onClick { update(_raw.dialog_alert) }
                }

                onCancel {
                    lottie.stop()
                }

                onDismiss {
                    lottie.stop()
                }
            }
            positiveButton(res = _string.reset_title) {
                it.dismiss()
                when {
                    isLockScreen -> vm.resetWallpaper(1)
                    else -> vm.resetWallpaper(0)
                }
            }
            negativeButton(res = _string.cancel_title) {
                it.dismiss()
            }
        }
    }

    private fun showBlackWallpaperDialog() {
        MaterialDialog(requireContext()).show {
//            title(res = _string.black_wallpaper_title)
//            message(res = _string.no_wallpapers_message)

            customView(viewRes = _layout.custom_screen_dialog_layout)
            onShow {
                val root = it.getCustomView()
                root.findViewById<TextView>(_id.wsd_msg).text =
                    getString(_string.no_wallpapers_message)
                val lottie = root.findViewById<LottieAnimationView>(_id.wsd_lottie)
                lottie.apply {
                    update(_raw.dialog_alert)
                    onClick { update(_raw.dialog_alert) }
                }

                onCancel {
                    lottie.stop()
                }

                onDismiss {
                    lottie.stop()
                }
            }

            positiveButton(res = _string.install_title) {
                it.dismiss()
                vm.setBlackWallpaper()
            }
            negativeButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showAboutDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            customView(viewRes = _layout.about_dialog_layout)
            setPeekHeight(literal = 1000)
            negativeButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showWhatNewDialog() {
        val testDescription =
            mutableListOf("Fix base lase", "Improve speed performance", "Add some bags")
        val testData = mutableListOf(
            NewInfo(version = "10.0.0", description = testDescription, date = "16.04.2020"),
            NewInfo(version = "9.18.44", description = testDescription, date = "08.01.2020"),
            NewInfo(version = "9.12.55", description = testDescription, date = "25.11.2019"),
            NewInfo(version = "9.12.55", description = testDescription, date = "18.09.2019"),
            NewInfo(version = "9.12.55", description = testDescription, date = "12.07.2019")
        )

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(res = _string.what_is_new_title)
            lifecycleOwner(this@MainFragment)
            customListAdapter(
                adapter = bindAdater(
                    testData,
                    _layout.what_is_new_item_layout
                ) { item: NewInfo ->
                    wni_title_tv.text = item.version
                    wni_date_tv.text = item.date
                    wni_description_tv.text = descriptionToString(item.description)
                    wni_container.onClick {
                        showWhatNewDetailDialog(item)
                    }
                })
            positiveButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun showWhatNewDetailDialog(item: NewInfo) {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@MainFragment)
            title(text = item.version)
            message(text = descriptionToString(item.description))
            positiveButton(res = _string.close_title) {
                it.dismiss()
            }
        }
    }

    private fun descriptionToString(data: List<String>): String {
        val sb = StringBuilder()
        data.forEachIndexed { index, s ->
            sb.append(s)
            if (data.size.minus(1) != index) {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}