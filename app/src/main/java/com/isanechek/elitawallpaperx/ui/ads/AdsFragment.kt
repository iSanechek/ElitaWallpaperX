package com.isanechek.elitawallpaperx.ui.ads

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.isanechek.elitawallpaperx._color
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.onClick
import kotlinx.android.synthetic.main.ads_fragment_layout.*

class AdsFragment : Fragment(_layout.ads_fragment_layout) {

    private lateinit var mRewardedAd: RewardedAd

    private val adsLoadListener = object : RewardedAdLoadCallback() {
        override fun onRewardedAdFailedToLoad(p0: Int) {
            super.onRewardedAdFailedToLoad(p0)
            d { "onRewardedAdFailedToLoad" }
        }

        override fun onRewardedAdLoaded() {
            super.onRewardedAdLoaded()
            d { "onRewardedAdLoaded" }
            showAds()
        }
    }

    private val userActionsListener = object : RewardedAdCallback() {
        override fun onUserEarnedReward(p0: RewardItem) {
            d { "user watch ads" }
        }

        override fun onRewardedAdFailedToShow(p0: Int) {
            super.onRewardedAdFailedToShow(p0)
            d { "Fail load ads" }
        }

        override fun onRewardedAdClosed() {
            super.onRewardedAdClosed()
            d { "user close ads" }
        }

        override fun onRewardedAdOpened() {
            super.onRewardedAdOpened()
            d { "user open ads" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        af_toolbar.setBackOrCloseButton(tintColor = _color.my_white_color) {
            findNavController().navigateUp()
        }

        ad_watch_btn.onClick {
            loadRewardedAd()
        }
    }

    private fun loadRewardedAd() {
        if (!(::mRewardedAd.isInitialized) || !mRewardedAd.isLoaded) {
            mRewardedAd = RewardedAd(requireContext(), TEST_AD_UNIT_ID)
            mRewardedAd.loadAd(AdRequest.Builder().build(), adsLoadListener)
        } else {
            showAds()
        }
    }

    private fun showAds() {
        mRewardedAd.show(requireActivity(), userActionsListener)
    }

    companion object {
//        const val AD_UNIT_ID = "ca-app-pub-9548650574871415/9743318724"
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}