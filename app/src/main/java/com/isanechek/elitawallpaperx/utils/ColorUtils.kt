package com.isanechek.elitawallpaperx.utils

import android.graphics.Color
import androidx.annotation.ColorRes
import com.isanechek.elitawallpaperx.d

object ColorUtils {
    const val PERCENT_0 = "00"
    const val PERCENT_5 = "0D"
    const val PERCENT_10 = "1A"
    const val PERCENT_15 = "26"
    const val PERCENT_20 = "33"
    const val PERCENT_25 = "40"
    const val PERCENT_30 = "4D"
    const val PERCENT_35 = "59"
    const val PERCENT_40 = "66"
    const val PERCENT_45 = "73"
    const val PERCENT_50 = "80"
    const val PERCENT_55 = "8C"
    const val PERCENT_60 = "99"
    const val PERCENT_65 = "A6"
    const val PERCENT_70 = "B3"
    const val PERCENT_75 = "BF"
    const val PERCENT_80 = "CC"
    const val PERCENT_85 = "D9"
    const val PERCENT_90 = "E6"
    const val PERCENT_95 = "F2"
    const val PERCENT_100 = "FF"

    fun colorToTransparent(color: String, percent: String): Int =
        getColor(Color.parseColor(color.replace("#", "")), percent)

    fun colorToTransparent(color: Int, percent: String): Int =
        getColor(color, percent)

    private fun getColor(color: Int, percent: String): Int {
        // Color.parseColor(String.format("%s%d", percent, color))

        val s = String.format("%s%d", percent, color)
        d { "Color $color" }
        d { "Percent $percent" }
        d { "Result $s" }
        return 0
    }

}