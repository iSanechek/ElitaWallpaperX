package com.isanechek.elitawallpaperx.ui.ads

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.isanechek.elitawallpaperx.*
import kotlinx.android.synthetic.main.ads_fragment_layout.*
import kotlin.math.roundToInt

class AdsFragment : Fragment(_layout.ads_fragment_layout) {

    private lateinit var rewardedAd: RewardedAd
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRun = false

    private val adsLoadListener = object : RewardedAdLoadCallback() {
        override fun onRewardedAdFailedToLoad(p0: Int) {
            super.onRewardedAdFailedToLoad(p0)
            d { "onRewardedAdFailedToLoad" }
            when(p0) {
                2 -> showAnimState(_raw.emoji_crying, _string.bad_internet_msg)
                3 -> showAnimState(_raw.emoji_no_ads, _string.no_ads_to_show_msg)
                else -> showAnimState(_raw.emoji_no_ads, _string.no_ads_to_show_msg)
            }

            showCloseTimer(COUNTER_TIME)
        }

        override fun onRewardedAdLoaded() {
            super.onRewardedAdLoaded()
            hideProgress()
        }
    }

    private val userActionsListener = object : RewardedAdCallback() {
        override fun onUserEarnedReward(p0: RewardItem) {
            d { "user watch ads" }
            showAnimState(_raw.emoji_thanks, _string.ads_thanks_msg)
            showCloseTimer(COUNTER_TIME)
        }

        override fun onRewardedAdFailedToShow(p0: Int) {
            super.onRewardedAdFailedToShow(p0)
            d { "Fail load ads" }
            showAnimState(_raw.emoji_crying, _string.something_msg)
            showCloseTimer(COUNTER_TIME)
        }

        override fun onRewardedAdClosed() {
            super.onRewardedAdClosed()
            d { "user close ads" }
            showAnimState(_raw.emoji_close_ads, _string.close_ads_msg)
            showCloseTimer(COUNTER_TIME)
        }

        override fun onRewardedAdOpened() {
            super.onRewardedAdOpened()
            showAnimState(_raw.emoji_smiley, _string.ads_again_msg)
            d { "user open ads" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        af_toolbar.setBackOrCloseButton(
            isClose = true,
            tintColor = _color.my_white_color
        ) {
            countDownTimer?.cancel()
            findNavController().navigateUp()
        }


        ad_watch_btn.onClick {
            if (isTimerRun) {
                countDownTimer?.cancel()
                ad_watch_btn.text = getString(_string.watch_ads_title)
                isTimerRun = false
            } else {
                showProgress()
            loadRewardedAd()
//                Handler().postDelayed({
//                    af_container.transitionToEnd()
//                }, 1000)
            }

        }
    }

    private fun loadRewardedAd() {
        if (!(::rewardedAd.isInitialized) || !rewardedAd.isLoaded) {
            rewardedAd = RewardedAd(requireContext(), TEST_AD_UNIT_ID)
            rewardedAd.loadAd(AdRequest.Builder().build(), adsLoadListener)
        }
    }

    override fun onPause() {
        countDownTimer?.cancel()
        super.onPause()
    }

    private fun showAnimState(animId: Int, msgId: Int) {
        af_lottie.apply {
            setAnimation(animId)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
        af_load_status.text = getString(msgId)
    }

    private fun showNoAds() {
        af_lottie.apply {
            setAnimation(_raw.emoji_no_ads)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
        af_load_status.text = getString(_string.no_ads_to_show_msg)
    }

    private fun showProgress() {
        af_container.transitionToEnd()
        af_lottie.apply {
            setAnimation(_raw.emoji_loading)
            repeatMode = LottieDrawable.REVERSE
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }

        af_lottie_progress.playAnimation()
    }

    private fun hideProgress() {
        showAds()
        af_lottie_progress.apply {
            cancelAnimation()
            isGone = true
        }
        if (af_lottie.isAnimating) {
            af_lottie.cancelAnimation()
        }
    }

    private fun showAds() {
        rewardedAd.show(requireActivity(), userActionsListener)
    }

    private fun showCloseTimer(time: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(time * 1000, 50) {

            override fun onFinish() {
                findNavController().navigateUp()
            }

            override fun onTick(millisUntilFinished: Long) {
                d { "Count ${millisUntilFinished / 1000 + 1}" }
                ad_watch_btn.text = String.format(
                    "%s (%d)",
                    getString(_string.close_title),
                    millisUntilFinished / 1000 + 1
                )

            }
        }
        countDownTimer?.start()
        Toast.makeText(
            requireContext(),
            getString(_string.screen_close_msg),
            Toast.LENGTH_SHORT
        ).show()
        isTimerRun = true
    }

    companion object {
        private const val COUNTER_TIME = 10L

        //        const val AD_UNIT_ID = "ca-app-pub-9548650574871415/9743318724"
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}