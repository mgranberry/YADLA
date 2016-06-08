package com.kludgenics.cgmlogger.app.viewmodel

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView

object CardBindings {
    val INTERPOLATOR = FastOutSlowInInterpolator()
    @BindingAdapter("isActive") @JvmStatic
    fun setIsActive(view: CardView, oldValue: Boolean, newValue: Boolean) {
        if (oldValue != newValue) {
            val targets = if (newValue) view.cardElevation to 30f else view.cardElevation to 10f
            val animator = ObjectAnimator.ofFloat(view, "cardElevation", targets.first, targets.second);
            animator.interpolator = INTERPOLATOR
            animator.setAutoCancel(true)
            animator.duration = 500
            animator.start()
        }
    }
}