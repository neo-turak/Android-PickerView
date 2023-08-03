package com.contrarywind.timer

import com.contrarywind.view.WheelView
import java.util.TimerTask
import kotlin.math.abs

/**
 * 滚动惯性的实现
 *
 * @author 小嵩
 * date:  2017-12-23 23:20:44
 */
/**
 * @param mWheelView 滚轮对象 //手指离开屏幕时的初始速度
 * @param mFirstVelocityY Y轴滑行速度
 */
class InertiaTimerTask(
    private val mWheelView: WheelView,
    private val mFirstVelocityY: Float
) : TimerTask() {
    //当前滑动速度
    private var mCurrentVelocityY : Float
    init {
        mCurrentVelocityY = Int.MAX_VALUE.toFloat()
    }

    override fun run() {

        //防止闪动，对速度做一个限制。
        if (mCurrentVelocityY == Int.MAX_VALUE.toFloat()) {
            mCurrentVelocityY = if (abs(mFirstVelocityY) > 2000f) {
                if (mFirstVelocityY > 0) 2000f else -2000f
            } else {
                mFirstVelocityY
            }
        }

        //发送handler消息 处理平顺停止滚动逻辑
        if (abs(mCurrentVelocityY) in 0.0f..20f) {
            mWheelView.cancelFuture()
            mWheelView.handler.sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL)
            return
        }
        val dy = (mCurrentVelocityY / 100f).toInt()
        mWheelView.totalScrollY = mWheelView.totalScrollY - dy
        if (!mWheelView.isLoop) {
            val itemHeight = mWheelView.itemHeight
            var top = -mWheelView.initPosition * itemHeight
            var bottom = (mWheelView.itemsCount - 1 - mWheelView.initPosition) * itemHeight
            if (mWheelView.totalScrollY - itemHeight * 0.25 < top) {
                top = mWheelView.totalScrollY + dy
            } else if (mWheelView.totalScrollY + itemHeight * 0.25 > bottom) {
                bottom = mWheelView.totalScrollY + dy
            }
            if (mWheelView.totalScrollY <= top) {
                mCurrentVelocityY = 40f
                mWheelView.totalScrollY = top.toInt().toFloat()
            } else if (mWheelView.totalScrollY >= bottom) {
                mWheelView.totalScrollY = bottom.toInt().toFloat()
                mCurrentVelocityY = -40f
            }
        }
        mCurrentVelocityY = if (mCurrentVelocityY < 0.0f) {
            mCurrentVelocityY + 20f
        } else {
            mCurrentVelocityY - 20f
        }

        //刷新UI
        mWheelView.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW)
    }
}