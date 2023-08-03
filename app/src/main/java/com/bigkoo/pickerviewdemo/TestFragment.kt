package com.bigkoo.pickerviewdemo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class TestFragment : Fragment(), View.OnClickListener {
    private var mView: View? = null
    private var btnShow: Button? = null
    private var pvTime: TimePickerView? = null
    private var mFrameLayout: FrameLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_test, container,false)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnShow = mView!!.findViewById<View>(R.id.btn_show) as Button
        btnShow!!.setOnClickListener(this)
        mFrameLayout = mView!!.findViewById<View>(R.id.fragmen_fragment) as FrameLayout
        initTimePicker()
    }

    private fun initTimePicker() {
        //控制时间范围(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
        //因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        startDate[2013, 0] = 23
        val endDate = Calendar.getInstance()
        endDate[2019, 11] = 28
        //时间选择器
        pvTime = TimePickerBuilder(requireContext(), object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) { //选中事件回调
                // 这里回调过来的v,就是show()方法里面所添加的 View 参数，如果show的时候没有添加参数，v则为null
                /*btn_Time.setText(getTime(date));*/
                val btn = v as Button?
                btn!!.text = getTime(date)
            }
        })
            .setLayoutRes(R.layout.pickerview_custom_time, object : CustomListener {
                override fun customLayout(v: View?) {
                    val tvSubmit = v!!.findViewById<View>(R.id.tv_finish) as TextView
                    val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
                    tvSubmit.setOnClickListener {
                        pvTime!!.returnData()
                        /*pvTime.dismiss();*/
                    }
                    ivCancel.setOnClickListener { /*pvTime.dismiss();*/ }
                }
            })
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setLabel("", "", "", "", "", "") //设置空字符串以隐藏单位提示   hide label
            .setDividerColor(Color.DKGRAY)
            .setContentTextSize(20)
            .setDate(selectedDate)
            .setRangDate(startDate, selectedDate)
            .setDecorView(mFrameLayout) //非dialog模式下,设置ViewGroup, pickerView将会添加到这个ViewGroup中
            .setOutSideColor(0x00000000)
            .setOutSideCancelable(false)
            .build()
        pvTime!!.setKeyBackCancelable(false) //系统返回键监听屏蔽掉
    }

    override fun onClick(v: View) {
        pvTime!!.show(v, false) //弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
    }

    private fun getTime(date: Date?): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(date)
    }
}