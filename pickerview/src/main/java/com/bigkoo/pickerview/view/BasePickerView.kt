package com.bigkoo.pickerview.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.configure.PickerOptions
import com.bigkoo.pickerview.listener.OnDismissListener
import com.bigkoo.pickerview.utils.PickerViewAnimateUtil.getAnimationResource

/**
 * Created by Sai on 15/11/22.
 * 精仿iOSPickerViewController控件
 */
open class BasePickerView(private val context: Context) {
    var dialogContainerLayout: ViewGroup? = null
        protected set
    private var rootView: ViewGroup? = null //附加View 的 根View
    private var dialogView: ViewGroup? = null //附加Dialog 的 根View

    protected lateinit var mPickerOptions: PickerOptions
    private var onDismissListener: OnDismissListener? = null
    private var dismissing = false
    private var outAnim: Animation? = null
    private var inAnim: Animation? = null
    private var isShowing = false
    protected var animGravity = Gravity.BOTTOM
    var dialog: Dialog? = null
        private set
    @JvmField
    protected var clickView: View? = null //是通过哪个View弹出的
    private var isAnim = true
    protected fun initViews() {
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
        )
        val layoutInflater = LayoutInflater.from(context)
        if (isDialog()) {
            //如果是对话框模式
            dialogView =
                layoutInflater.inflate(R.layout.layout_basepickerview, null, false) as ViewGroup
            //设置界面的背景为透明
            dialogView!!.setBackgroundColor(Color.TRANSPARENT)
            //这个是真正要加载选择器的父布局
            dialogContainerLayout =
                dialogView!!.findViewById<View>(R.id.content_container) as ViewGroup
            //设置对话框 默认左右间距屏幕30
            params.leftMargin = 30
            params.rightMargin = 30
            dialogContainerLayout!!.layoutParams = params
            //创建对话框
            createDialog()
            //给背景设置点击事件,这样当点击内容以外的地方会关闭界面
            dialogView!!.setOnClickListener { dismiss() }
        } else {
            //如果只是要显示在屏幕的下方
            //decorView是activity的根View,包含 contentView 和 titleView
            if (mPickerOptions.decorView == null) {
                mPickerOptions.decorView = (context as Activity).window.decorView as ViewGroup
            }
            //将控件添加到decorView中
            rootView = layoutInflater.inflate(
                R.layout.layout_basepickerview,
                mPickerOptions.decorView,
                false
            ) as ViewGroup
            rootView!!.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            if (mPickerOptions.outSideColor != -1) {
                rootView!!.setBackgroundColor(mPickerOptions.outSideColor)
            }
            //这个是真正要加载时间选取器的父布局
            dialogContainerLayout =
                rootView!!.findViewById<View>(R.id.content_container) as ViewGroup
            dialogContainerLayout!!.layoutParams = params
        }
        setKeyBackCancelable(true)
    }

    protected fun initAnim() {
        inAnim = inAnimation
        outAnim = outAnimation
    }

    protected fun initEvents() {}

    /**
     * @param v      (是通过哪个View弹出的)
     * @param isAnim 是否显示动画效果
     */
    fun show(v: View?, isAnim: Boolean) {
        clickView = v
        this.isAnim = isAnim
        show()
    }

    fun show(isAnim: Boolean) {
        show(null, isAnim)
    }

    fun show(v: View?) {
        clickView = v
        show()
    }

    /**
     * 添加View到根视图
     */
    fun show() {
        if (isDialog()) {
            showDialog()
        } else {
            if (isShowing()) {
                return
            }
            isShowing = true
            onAttached(rootView)
            rootView!!.requestFocus()
        }
    }

    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private fun onAttached(view: View?) {
        mPickerOptions.decorView!!.addView(view)
        if (isAnim) {
            dialogContainerLayout!!.startAnimation(inAnim)
        }
    }

    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    fun isShowing(): Boolean {
        return if (isDialog()) {
            false
        } else {
            rootView!!.parent != null || isShowing
        }
    }

    fun dismiss() {
        if (isDialog()) {
            dismissDialog()
        } else {
            if (dismissing) {
                return
            }
            if (isAnim) {
                //消失动画
                outAnim!!.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        dismissImmediately()
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                dialogContainerLayout!!.startAnimation(outAnim)
            } else {
                dismissImmediately()
            }
            dismissing = true
        }
    }

    fun dismissImmediately() {
        mPickerOptions.decorView!!.post { //从根视图移除
            mPickerOptions.decorView!!.removeView(rootView)
            isShowing = false
            dismissing = false
            if (onDismissListener != null) {
                onDismissListener!!.onDismiss(this@BasePickerView)
            }
        }
    }

    private val inAnimation: Animation
        get() {
            val res = getAnimationResource(animGravity, true)
            return AnimationUtils.loadAnimation(context, res)
        }
    private val outAnimation: Animation
         get() {
            val res = getAnimationResource(animGravity, false)
            return AnimationUtils.loadAnimation(context, res)
        }

    fun setOnDismissListener(onDismissListener: OnDismissListener?): BasePickerView {
        this.onDismissListener = onDismissListener
        return this
    }

    fun setKeyBackCancelable(isCancelable: Boolean) {
        val viewGroup: ViewGroup? = if (isDialog()) {
            dialogView
        } else {
            rootView
        }
        viewGroup!!.isFocusable = isCancelable
        viewGroup.isFocusableInTouchMode = isCancelable
        if (isCancelable) {
            viewGroup.setOnKeyListener(onKeyBackListener)
        } else {
            viewGroup.setOnKeyListener(null)
        }
    }

    private val onKeyBackListener = View.OnKeyListener { v, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_DOWN && isShowing()) {
            dismiss()
            return@OnKeyListener true
        }
        false
    }

    protected fun setOutSideCancelable(isCancelable: Boolean): BasePickerView {
        if (rootView != null) {
            val view = rootView!!.findViewById<View>(R.id.outmost_container)
            if (isCancelable) {
                view.setOnTouchListener(onCancelableTouchListener)
            } else {
                view.setOnTouchListener(null)
            }
        }
        return this
    }

    /**
     * 设置对话框模式是否可以点击外部取消
     */
    fun setDialogOutSideCancelable() {
        if (dialog != null) {
            dialog!!.setCancelable(mPickerOptions.cancelable)
        }
    }

    /**
     * Called when the user touch on black overlay, in order to dismiss the dialog.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val onCancelableTouchListener = OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            dismiss()
        }
        false
    }

    fun findViewById(id: Int): View {
        return dialogContainerLayout!!.findViewById(id)
    }

    fun createDialog() {
        if (dialogView != null) {
            dialog = Dialog(context, R.style.custom_dialog2)
            dialog!!.setCancelable(mPickerOptions.cancelable) //不能点外面取消,也不能点back取消
            dialog!!.setContentView(dialogView!!)
            val dialogWindow = dialog!!.window
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(R.style.picker_view_scale_anim)
                dialogWindow.setGravity(Gravity.CENTER) //可以改成Bottom
            }
            dialog!!.setOnDismissListener {
                if (onDismissListener != null) {
                    onDismissListener!!.onDismiss(this@BasePickerView)
                }
            }
        }
    }

    private fun showDialog() {
        if (dialog != null) {
            dialog!!.show()
        }
    }

    private fun dismissDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }

    open fun isDialog(): Boolean {
        return false
    }
}