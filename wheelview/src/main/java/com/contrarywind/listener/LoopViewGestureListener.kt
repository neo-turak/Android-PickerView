package com.contrarywind.listener

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.contrarywind.view.WheelView

/**
 * 手势监听
 */
class LoopViewGestureListener(private val wheelView: WheelView) : SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        wheelView.scrollBy(velocityY)
        return true
    }
}