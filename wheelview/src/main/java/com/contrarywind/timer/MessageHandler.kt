package com.contrarywind.timer

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.contrarywind.view.WheelView

/**
 * Handler 消息类
 *
 * @author 小嵩
 * date: 2017-12-23 23:20:44
 */
class MessageHandler(private val wheelView: WheelView) : Handler(Looper.myLooper()!!) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            WHAT_INVALIDATE_LOOP_VIEW -> wheelView.invalidate()
            WHAT_SMOOTH_SCROLL -> wheelView.smoothScroll(WheelView.ACTION.FLING)
            WHAT_ITEM_SELECTED -> wheelView.onItemSelected()
        }
    }

    companion object {
        const val WHAT_INVALIDATE_LOOP_VIEW = 1000
        const val WHAT_SMOOTH_SCROLL = 2000
        const val WHAT_ITEM_SELECTED = 3000
    }
}