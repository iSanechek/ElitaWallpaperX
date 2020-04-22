package com.isanechek.elitawallpaperx.widgets

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.util.AttributeSet
import com.isanechek.elitawallpaperx.R

enum class TypeWriterMode {
    CHARACTERS(),
    WORDS()
}

/**
 * Created by Joshua de Guzman on 18/07/2018.
 */

class TypedKtView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attributeSet) {
    private lateinit var mText: CharSequence
    private var mTextCopy: CharSequence = ""
    private var mTextList: List<String> = emptyList()
    private var mTextListCopy: List<String> = emptyList()
    private var mTextListDelimiter: String = "\\s+"
    private var currentIndex: Int = 0
    private var delay: Long = 150
    private var isAnimating: Boolean = false
    private val textHandler = Handler()
    private var listener: (() -> Unit)? = null
    private var isLooped: Boolean = false
    private var typeWriterMode = TypeWriterMode.CHARACTERS
    private var typedArray: TypedArray = context.theme.obtainStyledAttributes(
        attributeSet,
        R.styleable.TypedKtView,
        0, 0)

    private var isTypedArrayRecycled: Boolean = false
    private var isAnimatedOnLoad = false
    private var isSkipped = false
    private var isStopped = false

    // Configuration validators if set programmatically
    private var isAnimatedByWordSet = false
    private var isDelaySet = false
    private var isLoopedSet = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAnimatedOnLoad = typedArray.getBoolean(R.styleable.TypedKtView_animateOnLoad, false)
        if (isAnimatedOnLoad) {
            animateText()
        }
    }

    private val characterAdder = object : Runnable {
        override fun run() {
            when {
                isSkipped || isStopped -> return
                currentIndex <= mText.length -> {
                    text = mText.subSequence(0, currentIndex++)
                    handler.postDelayed(this, delay)
                }
                else -> validateLoop(::run)
            }
        }
    }

    private val wordAdder: Runnable = object : Runnable {
        override fun run() {
            when {
                isSkipped || isStopped -> return
                currentIndex <= mTextList.size -> {
                    text = mTextList.subList(0, currentIndex).joinToString(" ")
                    currentIndex++
                    handler.postDelayed(this, delay)
                }
                else -> validateLoop(::run)
            }
        }
    }

    /**
     * Validates loop configuration
     * Invokes end of animation
     * @param run: callback method from the handler
     */
    private fun validateLoop(run: () -> Unit) {
        if (isLooped) {
            currentIndex = 0
            text = ""
            run()
        } else {
            isAnimating = false
            listener?.invoke()
        }
    }

    /**
     * Configuration for setting the mode to character animate mode
     */
    fun setAnimationByCharacter() {
        typeWriterMode = TypeWriterMode.CHARACTERS
    }

    /**
     * Configuration for changing the animation state to word and overriding default delimiter
     * @param delimiter: default is set to space / (" ") pattern
     */
    fun setAnimationByWord(delimiter: String = mTextListDelimiter) {
        this.isAnimatedByWordSet = true
        mTextListDelimiter = delimiter
        typeWriterMode = TypeWriterMode.WORDS
    }

    /**
     * Configuration for callback method
     * @param endAnimationListener: callback called after animation ended
     */
    fun setEndAnimationListener(endAnimationListener: (() -> Unit)? = null) {
        listener = endAnimationListener
    }

    /**
     * Configuration for delay value
     * @param delay: default is 150 ms
     */
    fun setDelay(delay: Long) {
        this.isDelaySet = true
        this.delay = delay
    }

    /**
     * Configuration for loop behavior
     * @param isLooped: default is false
     */
    fun setLooped(isLooped: Boolean) {
        this.isLoopedSet = true
        this.isLooped = isLooped
    }

    /**
     * Trigger for animating the view with respect to the default and set configurations
     * @param charSequence: custom string passed on top of the view's text attribute
     */
    fun animateText(charSequence: CharSequence = "") {
        try {
            if (!isAnimating) {
                // Loads and validates resources from the attribute set
                if (!isTypedArrayRecycled) {
                    validateConfigurations()
                }

                // Validates current text in the view
                val mString: CharSequence = validateString(charSequence)
                mTextCopy = mString

                // Set animation status
                currentIndex = 0
                isAnimating = true
                isStopped = false
                isSkipped = false

                // Validates callback handlers for specific animation mode
                if (typeWriterMode == TypeWriterMode.CHARACTERS) {
                    mText = mString
                    textHandler.removeCallbacks(characterAdder)
                    textHandler.postDelayed(characterAdder, delay)
                } else {
                    mTextList = mString.split(Regex(mTextListDelimiter))
                    mTextListCopy = mString.split(Regex(mTextListDelimiter))
                    textHandler.removeCallbacks(wordAdder)
                    textHandler.postDelayed(wordAdder, delay)
                }
            }
        } finally {
            /**
             * This is required for caching purpose. When you call recycle it means that this object can be reused from this point.
             * Internally TypedArray contains few arrays so in order not to allocate memory each time when
             * TypedArray is used it is cached in Resources class as static field.
             * https://stackoverflow.com/questions/13805502/why-should-a-typedarray-be-recycled
             */
            if (!isTypedArrayRecycled) {
                isTypedArrayRecycled = true
                typedArray.recycle()
            }
        }
    }

    /**
     * Validate rendered attributes vs programmatically set configurations
     */
    private fun validateConfigurations() {
        // Check type mode
        val isAnimatedByWord = typedArray.getBoolean(R.styleable.TypedKtView_isAnimatedByWord, false)
        if (!isAnimatedByWordSet && isAnimatedByWord) {
            typeWriterMode = TypeWriterMode.WORDS
        }

        // Check loop
        val isLooped = typedArray.getBoolean(R.styleable.TypedKtView_isLooped, false)
        if (!isLoopedSet) {
            this.isLooped = isLooped
        }

        // Check delay
        val delay = typedArray.getInt(R.styleable.TypedKtView_delay, 150)
        if (!isDelaySet) {
            this.delay = delay.toLong()
        }
    }

    /**
     * @param charSequence: custom string passed on top of the view's text attribute
     * @return charSequence evaluated
     */
    private fun validateString(charSequence: CharSequence): CharSequence {
        if (!charSequence.trim().isEmpty()) {
            return charSequence
        } else if (isStopped || isSkipped) {
            return mTextCopy
        }
        return text
    }

    /**
     * Checks view's animation status
     * @return boolean whether the view is animating
     */
    fun isAnimating(): Boolean {
        return isAnimating
    }

    /**
     * Skips animation and resets view
     */
    fun skipAnimation() {
        if (!isStopped) {
            isAnimating = false
            isSkipped = true
            isStopped = false
            mTextList = mTextListCopy
            mText = mTextCopy
            currentIndex = 0
        }
    }

    /**
     * Stops the view from animating
     */
    fun stopAnimation() {
        if (!isSkipped) {
            isAnimating = false
            isStopped = true
            isSkipped = false
            mTextList = mTextListCopy
            mText = mTextCopy
            currentIndex = 0
            text = mTextCopy
        }
    }

    /**
     * Removes animation callbacks from the view
     */
    fun removeAnimation() {
        isAnimating = false
        if (typeWriterMode == TypeWriterMode.CHARACTERS) {
            textHandler.removeCallbacks(characterAdder)
        } else {
            textHandler.removeCallbacks(wordAdder)
        }
    }
}