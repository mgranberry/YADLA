package com.kludgenics.cgmlogger.app.viewmodel

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.databinding.BindingAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView
import android.view.View
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation

/**
 * Created by matthias on 3/21/16.
 */

object CardBindings {
    val INTERPOLATOR = FastOutSlowInInterpolator()
    @BindingAdapter("isActive") @JvmStatic
    fun setIsActive(view: CardView, isActive: Boolean) {
        val targets = if (isActive) 2f to 10f else 10f to 2f
        val animator = ObjectAnimator.ofFloat(view, "cardElevation", targets.first, targets.second);
        animator.interpolator = INTERPOLATOR
        animator.setAutoCancel(true)
        animator.duration = 500
        animator.start()
    }
}