package com.isanechek.elitawallpaperx.ui.settings

import android.os.Bundle
import android.webkit.WebView
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isanechek.elitawallpaperx._id
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx._xml

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(_xml.settings, rootKey)

        findPreference<PreferenceScreen>("about_us")?.setOnPreferenceClickListener {
            MaterialDialog(requireContext(), BottomSheet()).show {
                lifecycleOwner(this@SettingFragment)
                customView(viewRes = _layout.about_dialog_layout)
                setPeekHeight(literal = 1000)
                negativeButton(text = "ok") {
                    it.dismiss()
                }
            }
            false
        }

        findPreference<PreferenceScreen>("whatisnew")?.setOnPreferenceClickListener {
            MaterialDialog(requireContext(), BottomSheet()).show {
                lifecycleOwner(this@SettingFragment)
                title(text = "What is new")
                customView(viewRes = _layout.dialog_web_layout)
                positiveButton(text = "ok") {
                    it.dismiss()
                }
            }.onShow {
                val wv = it.getCustomView().findViewById<WebView>(_id.dw_web)
                wv.loadUrl("file:///android_asset/changelog.html")
            }
            false
        }
    }

}