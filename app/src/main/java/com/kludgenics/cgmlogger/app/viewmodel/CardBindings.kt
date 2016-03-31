package com.kludgenics.cgmlogger.app.viewmodel

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView

/**
 * Created by matthias on 3/21/16.
 */

object CardBindings {
    val INTERPOLATOR = FastOutSlowInInterpolator()
    @BindingAdapter("isActive") @JvmStatic
    fun setIsActive(view: CardView, isActive: Boolean) {
        val targets = if (isActive) 2f to 10f else 10f to 2f
        if (view.cardElevation == targets.first) {
            val animator = ObjectAnimator.ofFloat(view, "cardElevation", targets.first, targets.second);
            animator.interpolator = INTERPOLATOR
            animator.setAutoCancel(true)
            animator.duration = 500
            animator.start()
        } else
            view.cardElevation = targets.second
    }
}