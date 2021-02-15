@file:JvmName("Global")

package com.isanechek.elitawallpaperx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.github.razir.progressbutton.*
import com.isanechek.elitawallpaperx.utils.LiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

typealias _color = R.color
typealias _layout = R.layout
typealias _id = R.id
typealias _drawable = R.drawable
typealias _string = R.string
typealias _anim = R.anim
typealias _raw = R.raw
typealias _dimen = R.dimen

inline fun delay(milliseconds: Long, crossinline action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        action()
    }, milliseconds)
}

fun Button.progressShow(progressTitle: String) {
    this.apply {
        isEnabled = false
        showProgress {
            buttonText = progressTitle
            progressColor = Color.WHITE
        }
    }
}

fun Button.progressDone(doneTitle: String, buttonTitle: String) {
    this.apply {
        val animatedDrawable = ContextCompat.getDrawable(this.context, R.drawable.animated_check)!!
        animatedDrawable.setBounds(0, 0, 50, 50)
        hideProgress()
        showDrawable(animatedDrawable) {
            buttonText = doneTitle
            gravity = DrawableButton.GRAVITY_TEXT_END
        }
        delay(1000) {
            this.isEnabled = true
            this.hideDrawable(buttonTitle)
        }
    }
}

fun Button.progressHide(doneTitle: String, buttonTitle: String) {
    this.apply {
        isEnabled = true
        hideProgress(doneTitle)
        delay(1000) {
            this.text = buttonTitle
        }
    }
}

infix fun ViewGroup.inflate(layoutResId: Int): View =
    LayoutInflater.from(this.context).inflate(layoutResId, this, false)

fun View.onClick(function: () -> Unit) {
    setOnClickListener {
        function()
    }
}

fun LottieAnimationView.update(@RawRes anim: Int) {
    with(this) {
        if (isAnimating) cancelAnimation()
        setAnimation(anim)
        playAnimation()
    }
}

fun LottieAnimationView.stop() {
    if (this.isAnimating) this.cancelAnimation()
}

inline fun hasDebug(callback: () -> Unit) {
    if (BuildConfig.DEBUG) {
        callback.invoke()
    }
}

inline fun debugLog(message: () -> String) {
    hasDebug {
        Log.e("DEBUG", message())
    }
}

fun hasMinimumSdk(minimumSdk: Int): Boolean = Build.VERSION.SDK_INT >= minimumSdk

val hasIsNotMiUi: Boolean
    get() = getSystemProperty("ro.miui.ui.version.name").isEmpty()

private fun getSystemProperty(propName: String): String = try {
    val p = Runtime.getRuntime().exec("getprop $propName")
    BufferedReader(InputStreamReader(p.inputStream), 1024).use {
        it.readLine()
    }
} catch (ex: IOException) {
    ""
}


fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = LiveEvent<T>()
    result.addSource(this) {
        result.value = it
    }
    return result
}

@ExperimentalCoroutinesApi
fun tickerFlow(
    period: Long,
    initialDelay: Long = 0
): Flow<Unit> = callbackFlow {
    require(period > 0)
    require(initialDelay > -1)

    delay(initialDelay)
    var isDone = false
    while (!isDone) {
        offer(Unit)
        delay(period)
    }
    awaitClose { isDone = true }
}

fun Context.sendEmail(
    subject: String,
    senderMail: String,
    sendText: String
) {
    val emailIntent = Intent(
        Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", senderMail, null
        )
    )
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(senderMail))
    startActivity(Intent.createChooser(emailIntent, sendText))
}

inline fun Context.actionView(url: () -> String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url()))
    startActivity(intent)
}

fun Fragment.shortToast(@StringRes msgId: Int) {
    Toast.makeText(this.requireContext(), msgId, Toast.LENGTH_SHORT).show()
}

fun Fragment.shortToast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}

fun Activity.shortToast(@StringRes msgId: Int) {
    Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()
}

fun Activity.shortToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
