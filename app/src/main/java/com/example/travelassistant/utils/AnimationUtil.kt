package com.example.travelassistant.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AnticipateInterpolator

const val ANIMATION_DURATION = 500L
private const val ANIMATION_CHAIN_DELAY = 100L
fun View.slideUpAnimation(
    duration: Long = ANIMATION_DURATION,
    startOffset: Float = 1000f,
    delay: Long = ANIMATION_CHAIN_DELAY
): ViewPropertyAnimator {
    this.translationY = startOffset
    return this.animate()
        .translationY(0f)
        .setInterpolator(AnticipateInterpolator())
        .setStartDelay(delay)
        .setDuration(duration)
}

fun View.shakeAnimation() {
    val shake =
        ObjectAnimator.ofFloat(
            this,
            "translationX",
            0f,
            25f,
            -25f,
            25f,
            -25f,
            15f,
            -15f,
            6f,
            -6f,
            0f
        )
    shake.duration = 1000
    shake.start()
}