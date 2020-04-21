@file:JvmName("Global")

package com.isanechek.elitawallpaperx

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.isanechek.elitawallpaperx.utils.LiveEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

typealias _color = R.color
typealias _xml = R.xml
typealias _layout = R.layout
typealias _id = R.id
typealias _drawable = R.drawable
typealias _string = R.string
typealias _anim = R.anim
typealias _raw = R.raw

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


fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = LiveEvent<T>()
    result.addSource(this) {
        result.value = it
    }
    return result
}