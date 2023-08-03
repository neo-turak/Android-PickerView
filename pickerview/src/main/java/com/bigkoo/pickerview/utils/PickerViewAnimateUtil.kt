package com.bigkoo.pickerview.utils

import android.view.Gravity
import com.bigkoo.pickerview.R

/**
 * Created by Sai on 15/8/9.
 */
object PickerViewAnimateUtil {
    private const val INVALID = -1

    /**
     * Get default animation resource when not defined by the user
     *
     * @param gravity       the animGravity of the dialog
     * @param isInAnimation determine if is in or out animation. true when is is
     * @return the id of the animation resource
     */
    @JvmStatic
    fun getAnimationResource(gravity: Int, isInAnimation: Boolean): Int {
        when (gravity) {
            Gravity.BOTTOM -> return if (isInAnimation) R.anim.pickerview_slide_in_bottom else R.anim.pickerview_slide_out_bottom
        }
        return INVALID
    }
}