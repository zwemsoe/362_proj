package com.example.travelassistant.utils

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