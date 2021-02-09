package com.example.did.common.viewhelpers

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation

/**
 * Fade alpha in with an optional scale animation as well.
 * @param millis the length of the animation.
 * @param scaleFromPercent if this is set, it will also scale in also. Expects between 0-1.0.
*/
fun View.animateVisibilityIn(millis: Long = 200, scaleFromPercent: Float? = null) {
    visibility = View.VISIBLE

    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = AccelerateInterpolator()
    fadeIn.duration = millis

    val animation = AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.fillAfter = true

    if (scaleFromPercent != null) {
        val scaleUp = ScaleAnimation(
            scaleFromPercent,
            1f,
            scaleFromPercent,
            1f,
            Animation.RELATIVE_TO_SELF,
            .5f,
            Animation.RELATIVE_TO_SELF,
            .5f
        )
        scaleUp.interpolator = BounceInterpolator()
        scaleUp.duration = millis
        animation.addAnimation(scaleUp)
    }
    startAnimation(animation)
}

/**
 * Fade alpha out and when the animation completes, set the View.visibility to something.
 * @param millis the length of the animation.
 * @param finishVisibility This should be View.GONE or View.INVISIBLE
 */
fun View.animateVisibilityOut(millis: Long = 200, finishVisibility: Int = View.GONE) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = millis

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
        }

        override fun onAnimationEnd(animation: Animation) {
            visibility = finishVisibility
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    })
    startAnimation(fadeOut)
}

/**
 * Fade alpha in and then out. When the animation completes, set the View.visibility to something.
 * @param millisIn the length of the time spent fading in
 * @param millisOut the length of the time spent fading out
 * @param millisVisible the length of time we keep it visible between fade in/out.
 * @param finishVisibility This should be View.GONE or View.INVISIBLE
 */
fun View.animateVisibilityInOut(
    millisIn: Long = 200,
    millisOut: Long = 200,
    millisVisible: Long = 1500,
    finishVisibility: Int = View.GONE
) {
    visibility = View.VISIBLE

    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = AccelerateInterpolator()
    fadeIn.duration = millisIn

    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.startOffset = millisVisible + fadeIn.duration
    fadeOut.duration = millisOut

    val animation = AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.addAnimation(fadeOut)
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
        }

        override fun onAnimationEnd(animation: Animation) {
            visibility = finishVisibility
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    })
    startAnimation(animation)
}
