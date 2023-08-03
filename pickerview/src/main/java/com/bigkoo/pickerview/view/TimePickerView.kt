package com.bigkoo.pickerview.view

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.configure.PickerOptions
import com.bigkoo.pickerview.listener.ISelectTimeCallback
import java.text.ParseException
import java.util.Calendar

/**
 * 时间选择器
 * Created by Sai on 15/11/22.
 * Updated by XiaoSong on 2017-2-22.
 */
class TimePickerView(pickerOptions: PickerOptions) : BasePickerView(pickerOptions.context),
    View.OnClickListener {
    private var wheelTime: WheelTime? = null //自定义控件

    init {
        mPickerOptions = pickerOptions
        initView(pickerOptions.context)
    }

    private fun initView(context: Context?) {
        setDialogOutSideCancelable()
        initViews()
        initAnim()
        if (mPickerOptions.customListener == null) {
            LayoutInflater.from(context).inflate(R.layout.pickerview_time, dialogContainerLayout)

            //顶部标题
            val tvTitle = findViewById(R.id.tvTitle) as TextView
            val rvTopBar = findViewById(R.id.rv_topbar) as RelativeLayout

            //确定和取消按钮
            val btnSubmit = findViewById(R.id.btnSubmit) as Button
            val btnCancel = findViewById(R.id.btnCancel) as Button
            btnSubmit.tag = TAG_SUBMIT
            btnCancel.tag = TAG_CANCEL
            btnSubmit.setOnClickListener(this)
            btnCancel.setOnClickListener(this)

            //设置文字
            btnSubmit.text =
                if (TextUtils.isEmpty(mPickerOptions.textContentConfirm)) context!!.resources.getString(
                    R.string.pickerview_submit
                ) else mPickerOptions.textContentConfirm
            btnCancel.text =
                if (TextUtils.isEmpty(mPickerOptions.textContentCancel)) context!!.resources.getString(
                    R.string.pickerview_cancel
                ) else mPickerOptions.textContentCancel
            tvTitle.text =
                if (TextUtils.isEmpty(mPickerOptions.textContentTitle)) "" else mPickerOptions.textContentTitle //默认为空

            //设置color
            btnSubmit.setTextColor(mPickerOptions.textColorConfirm)
            btnCancel.setTextColor(mPickerOptions.textColorCancel)
            tvTitle.setTextColor(mPickerOptions.textColorTitle)
            rvTopBar.setBackgroundColor(mPickerOptions.bgColorTitle)

            //设置文字大小
            btnSubmit.textSize = mPickerOptions.textSizeSubmitCancel.toFloat()
            btnCancel.textSize = mPickerOptions.textSizeSubmitCancel.toFloat()
            tvTitle.textSize = mPickerOptions.textSizeTitle.toFloat()
        } else {
            mPickerOptions.customListener!!.customLayout(
                LayoutInflater.from(context).inflate(
                    mPickerOptions.layoutRes, dialogContainerLayout
                )
            )
        }
        // 时间转轮 自定义控件
        val timePickerView = findViewById(R.id.timepicker) as LinearLayout
        timePickerView.setBackgroundColor(mPickerOptions.bgColorWheel)
        initWheelTime(timePickerView)
    }

    private fun initWheelTime(timePickerView: LinearLayout) {
        wheelTime = WheelTime(
            timePickerView,
            mPickerOptions.type,
            mPickerOptions.textGravity,
            mPickerOptions.textSizeContent
        )
        if (mPickerOptions.timeSelectChangeListener != null) {
            wheelTime!!.setSelectChangeCallback(object : ISelectTimeCallback {
                override fun onTimeSelectChanged() {
                    try {
                        val date = WheelTime.dateFormat.parse(wheelTime!!.time)
                        mPickerOptions.timeSelectChangeListener!!.onTimeSelectChanged(date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            })
        }
        wheelTime!!.isLunarMode = mPickerOptions.isLunarCalendar
        if (mPickerOptions.startYear != 0 && mPickerOptions.endYear != 0 && mPickerOptions.startYear <= mPickerOptions.endYear) {
            setRange()
        }

        //若手动设置了时间范围限制
        if (mPickerOptions.startDate != null && mPickerOptions.endDate != null) {
            require(mPickerOptions.startDate!!.timeInMillis <= mPickerOptions.endDate!!.timeInMillis) { "startDate can't be later than endDate" }
            setRangDate()
        } else if (mPickerOptions.startDate != null) {
            require(mPickerOptions.startDate!![Calendar.YEAR] >= 1900) { "The startDate can not as early as 1900" }
            setRangDate()
        } else if (mPickerOptions.endDate != null) {
            require(mPickerOptions.endDate!![Calendar.YEAR] <= 2100) { "The endDate should not be later than 2100" }
            setRangDate()
        } else { //没有设置时间范围限制，则会使用默认范围。
            setRangDate()
        }
        setTime()
        wheelTime!!.setLabels(
            mPickerOptions.label_year,
            mPickerOptions.label_month,
            mPickerOptions.label_day,
            mPickerOptions.label_hours,
            mPickerOptions.label_minutes,
            mPickerOptions.label_seconds
        )
        wheelTime!!.setTextXOffset(
            mPickerOptions.x_offset_year,
            mPickerOptions.x_offset_month,
            mPickerOptions.x_offset_day,
            mPickerOptions.x_offset_hours,
            mPickerOptions.x_offset_minutes,
            mPickerOptions.x_offset_seconds
        )
        wheelTime!!.setItemsVisible(mPickerOptions.itemsVisibleCount)
        wheelTime!!.setAlphaGradient(mPickerOptions.isAlphaGradient)
        setOutSideCancelable(mPickerOptions.cancelable)
        wheelTime!!.setCyclic(mPickerOptions.cyclic)
        wheelTime!!.setDividerColor(mPickerOptions.dividerColor)
        wheelTime!!.setDividerType(mPickerOptions.dividerType)
        wheelTime!!.setLineSpacingMultiplier(mPickerOptions.lineSpacingMultiplier)
        wheelTime!!.setTextColorOut(mPickerOptions.textColorOut)
        wheelTime!!.setTextColorCenter(mPickerOptions.textColorCenter)
        wheelTime!!.isCenterLabel(mPickerOptions.isCenterLabel)
    }

    /**
     * 设置默认时间
     */
    fun setDate(date: Calendar?) {
        mPickerOptions.date = date
        setTime()
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private fun setRange() {
        wheelTime!!.startYear = mPickerOptions.startYear
        wheelTime!!.endYear = mPickerOptions.endYear
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private fun setRangDate() {
        wheelTime!!.setRangDate(mPickerOptions.startDate, mPickerOptions.endDate)
        initDefaultSelectedDate()
    }

    private fun initDefaultSelectedDate() {
        //如果手动设置了时间范围
        if (mPickerOptions.startDate != null && mPickerOptions.endDate != null) {
            //若默认时间未设置，或者设置的默认时间越界了，则设置默认选中时间为开始时间。
            if (mPickerOptions.date == null || mPickerOptions.date!!.timeInMillis < mPickerOptions.startDate!!.timeInMillis || mPickerOptions.date!!.timeInMillis > mPickerOptions.endDate!!.timeInMillis) {
                mPickerOptions.date = mPickerOptions.startDate
            }
        } else if (mPickerOptions.startDate != null) {
            //没有设置默认选中时间,那就拿开始时间当默认时间
            mPickerOptions.date = mPickerOptions.startDate
        } else if (mPickerOptions.endDate != null) {
            mPickerOptions.date = mPickerOptions.endDate
        }
    }

    /**
     * 设置选中时间,默认选中当前时间
     */
    private fun setTime() {
        val year: Int
        val month: Int
        val day: Int
        val hours: Int
        val minute: Int
        val seconds: Int
        val calendar = Calendar.getInstance()
        if (mPickerOptions.date == null) {
            calendar.timeInMillis = System.currentTimeMillis()
            year = calendar[Calendar.YEAR]
            month = calendar[Calendar.MONTH]
            day = calendar[Calendar.DAY_OF_MONTH]
            hours = calendar[Calendar.HOUR_OF_DAY]
            minute = calendar[Calendar.MINUTE]
            seconds = calendar[Calendar.SECOND]
        } else {
            year = mPickerOptions.date!![Calendar.YEAR]
            month = mPickerOptions.date!![Calendar.MONTH]
            day = mPickerOptions.date!![Calendar.DAY_OF_MONTH]
            hours = mPickerOptions.date!![Calendar.HOUR_OF_DAY]
            minute = mPickerOptions.date!![Calendar.MINUTE]
            seconds = mPickerOptions.date!![Calendar.SECOND]
        }
        wheelTime!!.setPicker(year, month, day, hours, minute, seconds)
    }

    override fun onClick(v: View) {
        val tag = v.tag as String
        if (tag == TAG_SUBMIT) {
            returnData()
        } else if (tag == TAG_CANCEL) {
            if (mPickerOptions.cancelListener != null) {
                mPickerOptions.cancelListener!!.onClick(v)
            }
        }
        dismiss()
    }

    fun returnData() {
        if (mPickerOptions.timeSelectListener != null) {
            try {
                val date = WheelTime.dateFormat.parse(wheelTime!!.time)
                mPickerOptions.timeSelectListener!!.onTimeSelect(date, clickView)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 动态设置标题
     *
     * @param text 标题文本内容
     */
    fun setTitleText(text: String?) {
        val tvTitle = findViewById(R.id.tvTitle) as TextView
        tvTitle.text = text
    }

    var isLunarCalendar: Boolean
        get() = wheelTime!!.isLunarMode
        /**
         * 目前暂时只支持设置1900 - 2100年
         *
         * @param lunar 农历的开关
         */
        set(lunar) {
            try {
                val year: Int
                val month: Int
                val day: Int
                val hours: Int
                val minute: Int
                val seconds: Int
                val calendar = Calendar.getInstance()
                calendar.time = WheelTime.dateFormat.parse(wheelTime!!.time)!!
                year = calendar[Calendar.YEAR]
                month = calendar[Calendar.MONTH]
                day = calendar[Calendar.DAY_OF_MONTH]
                hours = calendar[Calendar.HOUR_OF_DAY]
                minute = calendar[Calendar.MINUTE]
                seconds = calendar[Calendar.SECOND]
                wheelTime!!.isLunarMode = lunar
                wheelTime!!.setLabels(
                    mPickerOptions.label_year,
                    mPickerOptions.label_month,
                    mPickerOptions.label_day,
                    mPickerOptions.label_hours,
                    mPickerOptions.label_minutes,
                    mPickerOptions.label_seconds
                )
                wheelTime!!.setPicker(year, month, day, hours, minute, seconds)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

    override fun isDialog(): Boolean {
        return mPickerOptions.isDialog
    }

    companion object {
        private const val TAG_SUBMIT = "submit"
        private const val TAG_CANCEL = "cancel"
    }
}