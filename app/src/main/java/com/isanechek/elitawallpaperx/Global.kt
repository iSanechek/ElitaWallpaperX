@file:JvmName("Global")

package com.isanechek.elitawallpaperx

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

typealias _color = R.color
typealias _xml = R.xml
typealias _layout = R.layout
typealias _id = R.id
typealias _drawable = R.drawable
typealias _string = R.string

infix fun ViewGroup.inflate(layoutResId: Int): View =
    LayoutInflater.from(this.context).inflate(layoutResId, this, false)

fun View.onClick(function: () -> Unit) {
    setOnClickListener {
        function()
    }
}

inline fun d(message: () -> String) {
    Log.e("DEBUG", message())
}

fun hasMinimumSdk(minimumSdk: Int): Boolean = Build.VERSION.SDK_INT >= minimumSdk

val hasIsNotMiUi: Boolean
    get() = getSystemProperty("ro.miui.ui.version.name").isEmpty()

private fun getSystemProperty(propName: String): String = try {
    val p = Runtime.getRuntime().exec("getprop $propName")
    BufferedReader(InputStreamReader(p.inputStream), 1024).use {
        it.readLine()
    }
} catch (ex: IOException) { "" }