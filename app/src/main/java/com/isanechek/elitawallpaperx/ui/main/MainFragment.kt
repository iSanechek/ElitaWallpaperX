package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
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
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.customListAdapter
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.button.MaterialButton
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.ExecuteResult
import com.isanechek.elitawallpaperx.models.ItemMenu
import com.isanechek.elitawallpaperx.models.NewInfo
import com.isanechek.elitawallpaperx.ui.base.bindAdater
import com.richpathanimator.RichPathAnimator
import kotlinx.android.synthetic.main.main_fragment_layout.*
import kotlinx.android.synthetic.main.settings_custom_item_layout.view.*
import kotlinx.android.synthetic.main.what_is_new_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MainFragment : Fragment(_layout.main_fragment_layout) {

    private val ADS_KEY =
        if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-9548650574871415/9743318724"
    private lateinit var rewardedAd: RewardedAd
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
            mf_toolbar_counter.apply {
                text =
                    String.format("%d/%d", position.plus(1), pagerAdapter.itemCount)

            }
        }
    }

    private val adsLoadListener = object : RewardedAdLoadCallback() {
        override fun onRewardedAdFailedToLoad(p0: Int) {
            super.onRewardedAdFailedToLoad(p0)
            when (p0) {
                AdRequest.ERROR_CODE_INTERNAL_ERROR -> vm.sendEvent(TAG, "ERROR_CODE_INTERNAL_ERROR")
                AdRequest.ERROR_CODE_INVALID_REQUEST -> vm.sendEvent(TAG, "ERROR_CODE_INVALID_REQUEST")
                AdRequest.ERROR_CODE_NETWORK_ERROR -> vm.sendEvent(TAG, "ERROR_CODE_NETWORK_ERROR")
                AdRequest.ERROR_CODE_NO_FILL -> vm.sendEvent(TAG, "ERROR_CODE_NO_FILL")
            }
        }

        override fun onRewardedAdLoaded() {
            super.onRewardedAdLoaded()
            showAdsIcon()
        }
    }

    private val userActionsListener = object : RewardedAdCallback() {
        override fun onUserEarnedReward(p0: RewardItem) {
            debugLog { "user watch ads" }
            vm.sendEvent(TAG, "onUserEarnedReward")
            if (mf_toolbar_ads_icon.isVisible) mf_toolbar_ads_icon.isGone = true
            vm.hideAdsScreen()
            Toast.makeText(requireContext(), getString(_string.ads_thanks_msg), Toast.LENGTH_SHORT).show()
        }

        override fun onRewardedAdFailedToShow(p0: Int) {
            super.onRewardedAdFailedToShow(p0)
            debugLog { "Fail load ads" }
            vm.sendEvent(TAG, "onRewardedAdFailedToShow $p0")
        }

        override fun onRewardedAdClosed() {
            super.onRewardedAdClosed()
            debugLog { "user close ads" }
            vm.sendEvent(TAG, "onRewardedAdClosed")

        }

        override fun onRewardedAdOpened() {
            super.onRewardedAdOpened()
            vm.sendEvent(TAG, "onRewardedAdOpened")
            Toast.makeText(requireContext(), getString(_string.ads_thanks_msg), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mf_toolbar.hideCustomLayout()
        mf_toolbar_title.text = getString(_string.app_name)

        setupViewPager()

        mf_fab.onClick {
            showGridWallpaperDialog()
        }

        mf_menu_btn.onClick {
            showMenuDialog()
        }

        mf_company_tv.onClick {
            showAboutDialog()
        }

        mf_toolbar_counter.onClick {
            // тут надо заимплементить посхалку
        }

        if (vm.isShowAdsScreen) {
            loadRewardedAd()
        }
    }



    override fun onResume() {
        super.onResume()
        setupObserver()
        if (rewardedAd.isLoaded) {
            showAdsIcon()
        }
        mf_pager.registerOnPageChangeCallback(pagerListener)
    }

    override fun onPause() {
        mf_pager.unregisterOnPageChangeCallback(pagerListener)
        super.onPause()
    }

    private fun setupViewPager() {
        // pager
        with(mf_pager) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = pagerAdapter
        }

        pagerAdapter.setOnListenerCallback(object : MainPagerAdapter.ListenerCallback {
            override fun onItemClick(data: String, position: Int) {
                findNavController().navigate(_id.main_go_detail_fragment, bundleOf("path" to data))
            }
        })
    }

    private fun setupObserver() {
        vm.data.observe(viewLifecycleOwner, Observer { data ->
            when (data) {
                is ExecuteResult.Error -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    vm.sendEvent(TAG, "Load images from assets error! ${data.errorMessage}")
                    shortToast(_string.something_msg)
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
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    shortToast(_string.done_title)
                }
                is ExecuteResult.Loading -> if (mf_toolbar_progress.isInvisible) mf_toolbar_progress.isInvisible =
                    false
                is ExecuteResult.Error -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    shortToast(_string.reset_wallpaper_fail_msg)
                }
            }
        })

        vm.installWallpaperStatus.observe(viewLifecycleOwner, Observer { status ->
            debugLog { "INSTALL $status" }
            when (status) {
                is ExecuteResult.Done -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    shortToast(_string.done_title)
                }
                is ExecuteResult.Loading -> if (mf_toolbar_progress.isInvisible) mf_toolbar_progress.isInvisible =
                    false
                is ExecuteResult.Error -> {
                    if (mf_toolbar_progress.isVisible) mf_toolbar_progress.isInvisible = true
                    shortToast(_string.reset_wallpaper_fail_msg)
                }
            }
        })
    }

    private fun showGridWallpaperDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            customListAdapter(
                adapter = mainAdapter,
                layoutManager = GridLayoutManager(requireContext(), 3)
            )
            positiveButton(res = _string.close_title)
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

    private fun showMenuDialog() {
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
                            "info" -> showInfoListDialog()
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

            negativeButton(res = _string.close_title)
        }
    }

    private fun showInfoListDialog() {
        val menuItems = listOf(
            ItemMenu(
                id = "about_app",
                iconId = _drawable.ic_baseline_info_24,
                titleId = _string.about_app_title
            ),
            ItemMenu(
                id = "gp",
                iconId = _drawable.gp_icon_24,
                titleId = _string.gp_title
            ),
            ItemMenu(
                id = "web",
                iconId = _drawable.web_icon_24,
                titleId = _string.web_site_title
            ),
            ItemMenu(
                id = "email",
                iconId = _drawable.ic_baseline_email_24,
                titleId = _string.email_title
            )
        )

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@MainFragment)
            val dialog = this
            title(res = _string.info_title)
            customListAdapter(
                adapter = bindAdater(
                    menuItems,
                    _layout.settings_custom_item_layout
                ) { item: ItemMenu ->
                    sci_container.onClick {
                        when (item.id) {
                            "about_app" -> {
                                showAboutDialog()
                                dialog.dismiss()
                            }
                            "gp" -> {
                                dialog.dismiss()
                                requireContext().actionView { getString(_string.averd_gp_link) }

                            }
                            "web" -> {
                                dialog.dismiss()
                                requireContext().actionView { getString(_string.averd_web) }
                            }
                            "email" -> {
                                dialog.dismiss()
                                requireContext().sendEmail(
                                    getString(_string.app_name),
                                    getString(_string.averd_email),
                                    getString(_string.send_us_email_msg)
                                )
                            }
                        }
                    }
                    sci_icon.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            item.iconId
                        )
                    )
                    sci_title_tv.text = getText(item.titleId)
                })
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
            negativeButton(res = _string.cancel_title)
        }
    }

    private fun showBlackWallpaperDialog() {
        MaterialDialog(requireContext()).show {
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
            negativeButton(res = _string.close_title)
        }
    }

    private fun showAboutDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            title(text = "")
            customView(viewRes = _layout.about_dialog_layout)
            setPeekHeight(res = _dimen.peek_height_about_dialog)
            negativeButton(res = _string.what_is_new_title) {
                it.dismiss()
                showWhatNewDialog()
            }
            positiveButton(res = _string.close_title)

        }
    }

    private fun showWhatNewDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(res = _string.what_is_new_title)
            lifecycleOwner(this@MainFragment)
            val observer = Observer<ExecuteResult<List<NewInfo>>> { result ->
                when (result) {
                    is ExecuteResult.Done -> {
                        this.customListAdapter(
                            adapter = bindAdater(
                                result.data,
                                _layout.what_is_new_item_layout
                            ) { item: NewInfo ->
                                wni_title_tv.text = item.version
                                wni_date_tv.text = item.date
                                wni_description_tv.text = descriptionToString(item.description)
                                wni_container.onClick {
                                    showWhatNewDetailDialog(item)
                                }
                            })
                    }
                    is ExecuteResult.Error -> {
                        this.dismiss()
                        shortToast(_string.something_msg)
                    }
                    is ExecuteResult.Loading -> Unit
                }
            }

            positiveButton(res = _string.close_title)

            onPreShow { vm.whatIsNewData.observeForever(observer) }
            onDismiss { vm.whatIsNewData.removeObserver(observer) }
            onCancel { vm.whatIsNewData.removeObserver(observer) }
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

    private fun loadRewardedAd() {
        if (!(::rewardedAd.isInitialized) || !rewardedAd.isLoaded) {
            rewardedAd = RewardedAd(requireContext(), ADS_KEY)
            rewardedAd.loadAd(AdRequest.Builder().build(), adsLoadListener)
        }
    }

    @ExperimentalCoroutinesApi
    private fun showAdsIcon() {
        lifecycleScope.launch {
            val adsIcon = mf_toolbar_ads_icon
            with(adsIcon) {
                if (isGone) isGone = false
                onClick {
                    showAdsDialog()
                }
            }

            tickerFlow(period = 10000)
                .flowOn(Dispatchers.Default)
                .onEach {
                    val top = adsIcon.findRichPathByIndex(0)
                    val bottom = adsIcon.findRichPathByIndex(1)
                    RichPathAnimator.animate(top)
                        .interpolator(DecelerateInterpolator())
                        .rotation(0f, 20f, -20f, 10f, -10f, 5f, -5f, 2f, -2f, 0f)
                        .duration(4000)
                        .andAnimate(bottom)
                        .interpolator(DecelerateInterpolator())
                        .rotation(0f, 10f, -10f, 5f, -5f, 2f, -2f, 0f)
                        .startDelay(50)
                        .duration(4000)
                        .start()
                }.launchIn(this)
        }
    }

    private fun showAdsDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            title(text = "")
            setPeekHeight(res = _dimen.peek_height_about_dialog)
            customView(viewRes = _layout.advertising_fragment_layout)
            onShow {
                it.getCustomView().findViewById<MaterialButton>(_id.afl_btn).onClick {
                    it.dismiss()
                    rewardedAd.show(requireActivity(), userActionsListener)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}