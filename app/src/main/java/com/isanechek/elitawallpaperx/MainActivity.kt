package com.isanechek.elitawallpaperx

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Point
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.isanechek.elitawallpaperx.ui.black.InstallBlackWallpaperActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class MainActivity : AppCompatActivity(_layout.activity_main) {

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private var shortcutManager: ShortcutManager? = null

    private val vm: AppViewModel by viewModel()

    private val controller: NavController by lazy {
        findNavController(_id.main_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeNavBarColor()
        Handler(Looper.getMainLooper()).postDelayed({
            controller.navigate(
                _id.go_splash_to_main,
                null,
                NavOptions.Builder()
                    .setPopUpTo(_id.splash_fragment, true)
                    .setEnterAnim(_anim.slide_up_anim)
                    .setExitAnim(_anim.alpha_out_anim)
                    .setPopExitAnim(_anim.alpha_out_anim)
                    .setPopEnterAnim(_anim.slide_up_anim)
                    .build()
            )
        }, 1000)

        if (hasMinimumSdk(Build.VERSION_CODES.N_MR1)) {
            shortcutManager = getSystemService(ShortcutManager::class.java) as ShortcutManager
            createShortcuts()
        }
    }

    override fun onSupportNavigateUp(): Boolean = controller.navigateUp()

    override fun onResume() {
        super.onResume()
        updateScreenSize()
    }

    private fun updateScreenSize() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        vm.updateScreenSize(size.x, size.y)
    }

    private fun changeNavBarColor() {
        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != _id.splash_fragment) {
                if (hasMinimumSdk(21)) {
                    window.navigationBarColor =
                        ContextCompat.getColor(this, _color.my_primary_color)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcuts() {

        val webShortcut = ShortcutInfo.Builder(this, WEB_SHORTCUT_ID)
            .setShortLabel(getString(_string.web_site_title))
            .setLongLabel(getString(_string.open_us_website))
            .setIcon(Icon.createWithResource(this, _drawable.ic_shortcut_web))
            .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(getString(_string.averd_web))))
            .build()

        val blackWallShortcut = ShortcutInfo.Builder(this, BLACK_WALLPAPER_ID)
            .setShortLabel(getString(_string.black_wallpaper_title))
            .setLongLabel(getString(_string.install_black_wallpaper_description))
            .setIcon(Icon.createWithResource(this, _drawable.ic_shortcut_b))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    this,
                    InstallBlackWallpaperActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(
                        InstallBlackWallpaperActivity.ACTION_KEY,
                        InstallBlackWallpaperActivity.BLACK_WALLPAPER_INSTALL
                    )
            )
            .build()

        val randomWallShortcut = ShortcutInfo.Builder(this, RANDOM_WALLPAPER_ID)
            .setShortLabel(getString(_string.random_shortcut_title))
            .setLongLabel(getString(_string.random_shortcut_description))
            .setIcon(Icon.createWithResource(this, _drawable.ic_shortcut_r))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    this,
                    InstallBlackWallpaperActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(
                        InstallBlackWallpaperActivity.ACTION_KEY,
                        InstallBlackWallpaperActivity.RANDOM_WALLPAPER_INSTALL
                    )
            )
            .build()

        shortcutManager?.dynamicShortcuts =
            listOf(webShortcut, blackWallShortcut, randomWallShortcut)

    }

    companion object {
        private const val WEB_SHORTCUT_ID = "web_shortcut_id"
        private const val BLACK_WALLPAPER_ID = "black_wallpaper_id"
        private const val RANDOM_WALLPAPER_ID = "random_wallpaper_id"
    }
}