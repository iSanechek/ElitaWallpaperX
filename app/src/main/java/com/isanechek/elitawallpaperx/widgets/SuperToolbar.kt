// original here https://github.com/andrefrsousa/SuperToolbar/blob/master/lib/src/main/java/com/andrefrsousa/supertoolbar/SuperToolbar.kt

package com.isanechek.elitawallpaperx.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.isanechek.elitawallpaperx.*

open class SuperToolbar : Toolbar {

    private lateinit var closeOrBackBtn: AppCompatImageButton
    private lateinit var titleView: AppCompatTextView
    private lateinit var progress: ProgressBar
    private lateinit var settingsBtn: AppCompatImageButton
    private lateinit var container: View

    private var isElevationShown = false
    private var animationDuration = 0L
    private var toolbarElevation = 8f

    constructor(context: Context) : super(context) {
        initView(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (isInEditMode || attrs == null) {
            return
        }

        with(context.obtainStyledAttributes(attrs, R.styleable.SuperToolbar, defStyleAttr, 0)) {
            animationDuration =
                getInt(R.styleable.SuperToolbar_superToolbar_animationDuration, DURATION).toLong()
            isElevationShown =
                getBoolean(R.styleable.SuperToolbar_superToolbar_showElevationAtStart, false)
            recycle()
        }

        // By default we remove the elevation when creating the toolbar
        if (!isElevationShown) {
            ViewCompat.setElevation(this, 0f)
        } else ViewCompat.setElevation(this, toolbarElevation)

        // Add a custom view
        container = this.inflate(_layout.super_toolbar_layout)
        titleView = container.findViewById(_id.toolbar_title) as AppCompatTextView
        progress = container.findViewById(_id.toolbar_progress) as ProgressBar
        closeOrBackBtn =
            container.findViewById(_id.toolbar_back_or_close_btn) as AppCompatImageButton
        settingsBtn = container.findViewById(_id.toolbar_setting_btn) as AppCompatImageButton
        addView(container)
    }

    override fun setTitle(resId: Int) {
        if (::titleView.isInitialized) {
            titleView.setText(resId)
            return
        }

        super.setTitle(resId)
    }

    override fun setTitle(title: CharSequence?) {
        if (::titleView.isInitialized) {
            titleView.text = title
            return
        }

        super.setTitle(title)
    }

    override fun setTitleTextAppearance(context: Context?, resId: Int) {
        if (::titleView.isInitialized) {
            if (hasMinimumSdk(Build.VERSION_CODES.M)) {
                titleView.setTextAppearance(resId)

            } else {
                titleView.setTextAppearance(context, resId)
            }

            return
        }

        super.setTitleTextAppearance(context, resId)
    }

    override fun setTitleTextColor(color: Int) {
        if (::titleView.isInitialized) {
            titleView.setTextColor(color)
            return
        }

        super.setTitleTextColor(color)
    }

    //region PUBLIC METHODS

    /**
     * Toggles the toolbar elevation visibility using an animation
     *
     * @param show true if you want to show the elevation; false otherwise
     */
    fun setElevationVisibility(show: Boolean) {
        if (isElevationShown == show) {
            return
        }

        isElevationShown = show

        ViewCompat.animate(this).run {
            translationZ(if (show) toolbarElevation else 0f)
            interpolator = DecelerateInterpolator()
            duration = animationDuration
            start()
        }
    }

    fun setProgressVisibility(show: Boolean) {
        when {
            show -> if (progress.isInvisible) progress.isInvisible = false
            else -> if (progress.isVisible) progress.isInvisible = true
        }
    }

    fun setBackOrCloseButton(isClose: Boolean = true, callback: () -> Unit) {
        if (closeOrBackBtn.isInvisible) closeOrBackBtn.isInvisible = false
        if (isClose) {
            closeOrBackBtn.setImageDrawable(getDrawable(_drawable.ic_baseline_close_24))
            closeOrBackBtn.onClick { callback.invoke() }
        } else {
            closeOrBackBtn.setImageDrawable(getDrawable(_drawable.ic_baseline_arrow_back_24))
            closeOrBackBtn.onClick { callback.invoke() }
        }
    }

    fun setSettingButton(
        isSettings: Boolean = true,
        @DrawableRes iconId: Int? = null,
        callback: () -> Unit
    ) {
        if (settingsBtn.isInvisible) settingsBtn.isInvisible = false
        if (isSettings) {
            settingsBtn.apply {
                setImageDrawable(getDrawable(_drawable.ic_baseline_settings_24))
                onClick { callback.invoke() }
            }
        } else {
            iconId ?: throw IllegalArgumentException("Icon id not be null!")
            settingsBtn.apply {
                setImageDrawable(getDrawable(iconId))
                onClick { callback.invoke() }
            }
        }
    }

    fun hideCustomLayout() {
        if (container.isVisible) container.isGone = true
    }

    private fun getDrawable(@DrawableRes id: Int): Drawable? =
        ContextCompat.getDrawable(this.context, id)

    //endregion

    companion object {
        private const val DURATION = 250
    }
}