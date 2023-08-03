package com.bigkoo.pickerview.view

import android.view.View
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.bigkoo.pickerview.adapter.NumericWheelAdapter
import com.bigkoo.pickerview.listener.ISelectTimeCallback
import com.bigkoo.pickerview.utils.ChinaDate
import com.bigkoo.pickerview.utils.LunarCalendar
import com.contrarywind.listener.OnItemSelectedListener
import com.contrarywind.view.WheelView
import com.contrarywind.view.WheelView.DividerType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar

class WheelTime(
    val view: View,
    private val type: BooleanArray,
    private val gravity: Int,
    private val textSize: Int
) {
    private var wv_year: WheelView? = null
    private var wv_month: WheelView? = null
    private var wv_day: WheelView? = null
    private var wv_hours: WheelView? = null
    private var wv_minutes: WheelView? = null
    private var wv_seconds: WheelView? = null
    @JvmField
    var startYear = DEFAULT_START_YEAR
    @JvmField
    var endYear = DEFAULT_END_YEAR
    private var startMonth = DEFAULT_START_MONTH
    private var endMonth = DEFAULT_END_MONTH
    private var startDay = DEFAULT_START_DAY
    private var endDay = DEFAULT_END_DAY //表示31天的
    private var currentYear = 0
    var isLunarMode = false
    private var mSelectChangeCallback: ISelectTimeCallback? = null
    fun setPicker(year: Int, month: Int, day: Int) {
        this.setPicker(year, month, day, 0, 0, 0)
    }

    fun setPicker(year: Int, month: Int, day: Int, h: Int, m: Int, s: Int) {
        if (isLunarMode) {
            val lunar = LunarCalendar.solarToLunar(year, month + 1, day)
            setLunar(lunar[0], lunar[1] - 1, lunar[2], lunar[3] == 1, h, m, s)
        } else {
            setSolar(year, month, day, h, m, s)
        }
    }

    /**
     * 设置农历
     *
     * @param year
     * @param month
     * @param day
     * @param h
     * @param m
     * @param s
     */
    private fun setLunar(year: Int, month: Int, day: Int, isLeap: Boolean, h: Int, m: Int, s: Int) {
        // 年
        wv_year = view.findViewById<View>(R.id.year) as WheelView
        wv_year!!.setAdapter(
            ArrayWheelAdapter(
                ChinaDate.getYears(
                    startYear,
                    endYear
                )
            )
        ) // 设置"年"的显示数据
        wv_year!!.setLabel("") // 添加文字
        wv_year!!.currentItem = year - startYear // 初始化时显示的数据
        wv_year!!.setGravity(gravity)

        // 月
        wv_month = view.findViewById<View>(R.id.month) as WheelView
        wv_month!!.setAdapter(ArrayWheelAdapter(ChinaDate.getMonths(year)))
        wv_month!!.setLabel("")
        val leapMonth = ChinaDate.leapMonth(year)
        if (leapMonth != 0 && (month > leapMonth - 1 || isLeap)) { //选中月是闰月或大于闰月
            wv_month!!.currentItem = month + 1
        } else {
            wv_month!!.currentItem = month
        }
        wv_month!!.setGravity(gravity)

        // 日
        wv_day = view.findViewById<View>(R.id.day) as WheelView
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (ChinaDate.leapMonth(year) == 0) {
            wv_day!!.setAdapter(
                ArrayWheelAdapter(
                    ChinaDate.getLunarDays(
                        ChinaDate.monthDays(
                            year,
                            month
                        )
                    )
                )
            )
        } else {
            wv_day!!.setAdapter(ArrayWheelAdapter(ChinaDate.getLunarDays(ChinaDate.leapDays(year))))
        }
        wv_day!!.setLabel("")
        wv_day!!.currentItem = day - 1
        wv_day!!.setGravity(gravity)
        wv_hours = view.findViewById<View>(R.id.hour) as WheelView
        wv_hours!!.setAdapter(NumericWheelAdapter(0, 23))
        //wv_hours.setLabel(context.getString(R.string.pickerview_hours));// 添加文字
        wv_hours!!.currentItem = h
        wv_hours!!.setGravity(gravity)
        wv_minutes = view.findViewById<View>(R.id.min) as WheelView
        wv_minutes!!.setAdapter(NumericWheelAdapter(0, 59))
        //wv_minutes.setLabel(context.getString(R.string.pickerview_minutes));// 添加文字
        wv_minutes!!.currentItem = m
        wv_minutes!!.setGravity(gravity)
        wv_seconds = view.findViewById<View>(R.id.second) as WheelView
        wv_seconds!!.setAdapter(NumericWheelAdapter(0, 59))
        //wv_seconds.setLabel(context.getString(R.string.pickerview_minutes));// 添加文字
        wv_seconds!!.currentItem = m
        wv_seconds!!.setGravity(gravity)

        // 添加"年"监听
        wv_year!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                val year_num = index + startYear
                // 判断是不是闰年,来确定月和日的选择
                wv_month!!.setAdapter(ArrayWheelAdapter(ChinaDate.getMonths(year_num)))
                if (ChinaDate.leapMonth(year_num) != 0 && wv_month!!.currentItem > ChinaDate.leapMonth(
                        year_num
                    ) - 1
                ) {
                    wv_month!!.currentItem = wv_month!!.currentItem + 1
                } else {
                    wv_month!!.currentItem = wv_month!!.currentItem
                }
                val currentIndex = wv_day!!.currentItem
                var maxItem = 29
                maxItem =
                    if (ChinaDate.leapMonth(year_num) != 0 && wv_month!!.currentItem > ChinaDate.leapMonth(
                            year_num
                        ) - 1
                    ) {
                        if (wv_month!!.currentItem == ChinaDate.leapMonth(year_num) + 1) {
                            wv_day!!.setAdapter(
                                ArrayWheelAdapter(
                                    ChinaDate.getLunarDays(
                                        ChinaDate.leapDays(year_num)
                                    )
                                )
                            )
                            ChinaDate.leapDays(year_num)
                        } else {
                            wv_day!!.setAdapter(
                                ArrayWheelAdapter(
                                    ChinaDate.getLunarDays(
                                        ChinaDate.monthDays(year_num, wv_month!!.currentItem)
                                    )
                                )
                            )
                            ChinaDate.monthDays(year_num, wv_month!!.currentItem)
                        }
                    } else {
                        wv_day!!.setAdapter(
                            ArrayWheelAdapter(
                                ChinaDate.getLunarDays(
                                    ChinaDate.monthDays(
                                        year_num,
                                        wv_month!!.currentItem + 1
                                    )
                                )
                            )
                        )
                        ChinaDate.monthDays(year_num, wv_month!!.currentItem + 1)
                    }
                if (currentIndex > maxItem - 1) {
                    wv_day!!.currentItem = maxItem - 1
                }
                if (mSelectChangeCallback != null) {
                    mSelectChangeCallback!!.onTimeSelectChanged()
                }
            }
        })

        // 添加"月"监听
        wv_month!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                val year_num = wv_year!!.currentItem + startYear
                val currentIndex = wv_day!!.currentItem
                var maxItem = 29
                maxItem = if (ChinaDate.leapMonth(year_num) != 0 && index > ChinaDate.leapMonth(
                        year_num
                    ) - 1
                ) {
                    if (wv_month!!.currentItem == ChinaDate.leapMonth(year_num) + 1) {
                        wv_day!!.setAdapter(
                            ArrayWheelAdapter(
                                ChinaDate.getLunarDays(
                                    ChinaDate.leapDays(
                                        year_num
                                    )
                                )
                            )
                        )
                        ChinaDate.leapDays(year_num)
                    } else {
                        wv_day!!.setAdapter(
                            ArrayWheelAdapter(
                                ChinaDate.getLunarDays(
                                    ChinaDate.monthDays(
                                        year_num,
                                        index
                                    )
                                )
                            )
                        )
                        ChinaDate.monthDays(year_num, index)
                    }
                } else {
                    wv_day!!.setAdapter(
                        ArrayWheelAdapter(
                            ChinaDate.getLunarDays(
                                ChinaDate.monthDays(
                                    year_num,
                                    index + 1
                                )
                            )
                        )
                    )
                    ChinaDate.monthDays(year_num, index + 1)
                }
                if (currentIndex > maxItem - 1) {
                    wv_day!!.currentItem = maxItem - 1
                }
                if (mSelectChangeCallback != null) {
                    mSelectChangeCallback!!.onTimeSelectChanged()
                }
            }
        })
        setChangedListener(wv_day)
        setChangedListener(wv_hours)
        setChangedListener(wv_minutes)
        setChangedListener(wv_seconds)
        if (type.size != 6) {
            throw RuntimeException("type[] length is not 6")
        }
        wv_year!!.visibility = if (type[0]) View.VISIBLE else View.GONE
        wv_month!!.visibility = if (type[1]) View.VISIBLE else View.GONE
        wv_day!!.visibility = if (type[2]) View.VISIBLE else View.GONE
        wv_hours!!.visibility = if (type[3]) View.VISIBLE else View.GONE
        wv_minutes!!.visibility = if (type[4]) View.VISIBLE else View.GONE
        wv_seconds!!.visibility = if (type[5]) View.VISIBLE else View.GONE
        setContentTextSize()
    }

    /**
     * 设置公历
     *
     * @param year
     * @param month
     * @param day
     * @param h
     * @param m
     * @param s
     */
    private fun setSolar(year: Int, month: Int, day: Int, h: Int, m: Int, s: Int) {
        // 添加大小月月份并将其转换为list,方便之后的判断
        val months_big = arrayOf("1", "3", "5", "7", "8", "10", "12")
        val months_little = arrayOf("4", "6", "9", "11")
        val list_big = Arrays.asList(*months_big)
        val list_little = Arrays.asList(*months_little)
        currentYear = year
        // 年
        wv_year = view.findViewById<View>(R.id.year) as WheelView
        wv_year!!.setAdapter(NumericWheelAdapter(startYear, endYear)) // 设置"年"的显示数据
        wv_year!!.currentItem = year - startYear // 初始化时显示的数据
        wv_year!!.setGravity(gravity)
        // 月
        wv_month = view.findViewById<View>(R.id.month) as WheelView
        if (startYear == endYear) { //开始年等于终止年
            wv_month!!.setAdapter(NumericWheelAdapter(startMonth, endMonth))
            wv_month!!.currentItem = month + 1 - startMonth
        } else if (year == startYear) {
            //起始日期的月份控制
            wv_month!!.setAdapter(NumericWheelAdapter(startMonth, 12))
            wv_month!!.currentItem = month + 1 - startMonth
        } else if (year == endYear) {
            //终止日期的月份控制
            wv_month!!.setAdapter(NumericWheelAdapter(1, endMonth))
            wv_month!!.currentItem = month
        } else {
            wv_month!!.setAdapter(NumericWheelAdapter(1, 12))
            wv_month!!.currentItem = month
        }
        wv_month!!.setGravity(gravity)
        // 日
        wv_day = view.findViewById<View>(R.id.day) as WheelView
        val leapYear = year % 4 == 0 && year % 100 != 0 || year % 400 == 0
        if (startYear == endYear && startMonth == endMonth) {
            if (list_big.contains((month + 1).toString())) {
                if (endDay > 31) {
                    endDay = 31
                }
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, endDay))
            } else if (list_little.contains((month + 1).toString())) {
                if (endDay > 30) {
                    endDay = 30
                }
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, endDay))
            } else {
                // 闰年
                if (leapYear) {
                    if (endDay > 29) {
                        endDay = 29
                    }
                    wv_day!!.setAdapter(NumericWheelAdapter(startDay, endDay))
                } else {
                    if (endDay > 28) {
                        endDay = 28
                    }
                    wv_day!!.setAdapter(NumericWheelAdapter(startDay, endDay))
                }
            }
            wv_day!!.currentItem = day - startDay
        } else if (year == startYear && month + 1 == startMonth) {
            // 起始日期的天数控制
            if (list_big.contains((month + 1).toString())) {
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, 31))
            } else if (list_little.contains((month + 1).toString())) {
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, 30))
            } else {
                // 闰年 29，平年 28
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, if (leapYear) 29 else 28))
            }
            wv_day!!.currentItem = day - startDay
        } else if (year == endYear && month + 1 == endMonth) {
            // 终止日期的天数控制
            if (list_big.contains((month + 1).toString())) {
                if (endDay > 31) {
                    endDay = 31
                }
                wv_day!!.setAdapter(NumericWheelAdapter(1, endDay))
            } else if (list_little.contains((month + 1).toString())) {
                if (endDay > 30) {
                    endDay = 30
                }
                wv_day!!.setAdapter(NumericWheelAdapter(1, endDay))
            } else {
                // 闰年
                if (leapYear) {
                    if (endDay > 29) {
                        endDay = 29
                    }
                    wv_day!!.setAdapter(NumericWheelAdapter(1, endDay))
                } else {
                    if (endDay > 28) {
                        endDay = 28
                    }
                    wv_day!!.setAdapter(NumericWheelAdapter(1, endDay))
                }
            }
            wv_day!!.currentItem = day - 1
        } else {
            // 判断大小月及是否闰年,用来确定"日"的数据
            if (list_big.contains((month + 1).toString())) {
                wv_day!!.setAdapter(NumericWheelAdapter(1, 31))
            } else if (list_little.contains((month + 1).toString())) {
                wv_day!!.setAdapter(NumericWheelAdapter(1, 30))
            } else {
                // 闰年 29，平年 28
                wv_day!!.setAdapter(NumericWheelAdapter(startDay, if (leapYear) 29 else 28))
            }
            wv_day!!.currentItem = day - 1
        }
        wv_day!!.setGravity(gravity)
        //时
        wv_hours = view.findViewById<View>(R.id.hour) as WheelView
        wv_hours!!.setAdapter(NumericWheelAdapter(0, 23))
        wv_hours!!.currentItem = h
        wv_hours!!.setGravity(gravity)
        //分
        wv_minutes = view.findViewById<View>(R.id.min) as WheelView
        wv_minutes!!.setAdapter(NumericWheelAdapter(0, 59))
        wv_minutes!!.currentItem = m
        wv_minutes!!.setGravity(gravity)
        //秒
        wv_seconds = view.findViewById<View>(R.id.second) as WheelView
        wv_seconds!!.setAdapter(NumericWheelAdapter(0, 59))
        wv_seconds!!.currentItem = s
        wv_seconds!!.setGravity(gravity)

        // 添加"年"监听
        wv_year!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                val year_num = index + startYear
                currentYear = year_num
                var currentMonthItem = wv_month!!.currentItem //记录上一次的item位置
                // 判断大小月及是否闰年,用来确定"日"的数据
                if (startYear == endYear) {
                    //重新设置月份
                    wv_month!!.setAdapter(NumericWheelAdapter(startMonth, endMonth))
                    if (currentMonthItem > wv_month!!.getAdapter()!!.itemsCount() - 1) {
                        currentMonthItem = wv_month!!.getAdapter()!!.itemsCount() - 1
                        wv_month!!.currentItem = currentMonthItem
                    }
                    val monthNum = currentMonthItem + startMonth
                    if (startMonth == endMonth) {
                        //重新设置日
                        setReDay(year_num, monthNum, startDay, endDay, list_big, list_little)
                    } else if (monthNum == startMonth) {
                        //重新设置日
                        setReDay(year_num, monthNum, startDay, 31, list_big, list_little)
                    } else if (monthNum == endMonth) {
                        setReDay(year_num, monthNum, 1, endDay, list_big, list_little)
                    } else { //重新设置日
                        setReDay(year_num, monthNum, 1, 31, list_big, list_little)
                    }
                } else if (year_num == startYear) { //等于开始的年
                    //重新设置月份
                    wv_month!!.setAdapter(NumericWheelAdapter(startMonth, 12))
                    if (currentMonthItem > wv_month!!.getAdapter()!!.itemsCount() - 1) {
                        currentMonthItem = wv_month!!.getAdapter()!!.itemsCount() - 1
                        wv_month!!.currentItem = currentMonthItem
                    }
                    val month = currentMonthItem + startMonth
                    if (month == startMonth) {
                        //重新设置日
                        setReDay(year_num, month, startDay, 31, list_big, list_little)
                    } else {
                        //重新设置日
                        setReDay(year_num, month, 1, 31, list_big, list_little)
                    }
                } else if (year_num == endYear) {
                    //重新设置月份
                    wv_month!!.setAdapter(NumericWheelAdapter(1, endMonth))
                    if (currentMonthItem > wv_month!!.getAdapter()!!.itemsCount() - 1) {
                        currentMonthItem = wv_month!!.getAdapter()!!.itemsCount() - 1
                        wv_month!!.currentItem = currentMonthItem
                    }
                    val monthNum = currentMonthItem + 1
                    if (monthNum == endMonth) {
                        //重新设置日
                        setReDay(year_num, monthNum, 1, endDay, list_big, list_little)
                    } else {
                        //重新设置日
                        setReDay(year_num, monthNum, 1, 31, list_big, list_little)
                    }
                } else {
                    //重新设置月份
                    wv_month!!.setAdapter(NumericWheelAdapter(1, 12))
                    //重新设置日
                    setReDay(year_num, wv_month!!.currentItem + 1, 1, 31, list_big, list_little)
                }
                if (mSelectChangeCallback != null) {
                    mSelectChangeCallback!!.onTimeSelectChanged()
                }
            }
        })


        // 添加"月"监听
        wv_month!!.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                var month_num = index + 1
                if (startYear == endYear) {
                    month_num = month_num + startMonth - 1
                    if (startMonth == endMonth) {
                        //重新设置日
                        setReDay(currentYear, month_num, startDay, endDay, list_big, list_little)
                    } else if (startMonth == month_num) {

                        //重新设置日
                        setReDay(currentYear, month_num, startDay, 31, list_big, list_little)
                    } else if (endMonth == month_num) {
                        setReDay(currentYear, month_num, 1, endDay, list_big, list_little)
                    } else {
                        setReDay(currentYear, month_num, 1, 31, list_big, list_little)
                    }
                } else if (currentYear == startYear) {
                    month_num = month_num + startMonth - 1
                    if (month_num == startMonth) {
                        //重新设置日
                        setReDay(currentYear, month_num, startDay, 31, list_big, list_little)
                    } else {
                        //重新设置日
                        setReDay(currentYear, month_num, 1, 31, list_big, list_little)
                    }
                } else if (currentYear == endYear) {
                    if (month_num == endMonth) {
                        //重新设置日
                        setReDay(
                            currentYear,
                            wv_month!!.currentItem + 1,
                            1,
                            endDay,
                            list_big,
                            list_little
                        )
                    } else {
                        setReDay(
                            currentYear,
                            wv_month!!.currentItem + 1,
                            1,
                            31,
                            list_big,
                            list_little
                        )
                    }
                } else {
                    //重新设置日
                    setReDay(currentYear, month_num, 1, 31, list_big, list_little)
                }
                if (mSelectChangeCallback != null) {
                    mSelectChangeCallback!!.onTimeSelectChanged()
                }
            }
        })
        setChangedListener(wv_day)
        setChangedListener(wv_hours)
        setChangedListener(wv_minutes)
        setChangedListener(wv_seconds)
        require(type.size == 6) { "type[] length is not 6" }
        wv_year!!.visibility = if (type[0]) View.VISIBLE else View.GONE
        wv_month!!.visibility = if (type[1]) View.VISIBLE else View.GONE
        wv_day!!.visibility = if (type[2]) View.VISIBLE else View.GONE
        wv_hours!!.visibility = if (type[3]) View.VISIBLE else View.GONE
        wv_minutes!!.visibility = if (type[4]) View.VISIBLE else View.GONE
        wv_seconds!!.visibility = if (type[5]) View.VISIBLE else View.GONE
        setContentTextSize()
    }

    private fun setChangedListener(wheelView: WheelView?) {
        if (mSelectChangeCallback != null) {
            wheelView!!.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    mSelectChangeCallback!!.onTimeSelectChanged()
                }
            })
        }
    }

    private fun setReDay(
        year_num: Int,
        monthNum: Int,
        startD: Int,
        endD: Int,
        list_big: List<String>,
        list_little: List<String>
    ) {
        var endD = endD
        var currentItem = wv_day!!.currentItem

//        int maxItem;
        if (list_big.contains(monthNum.toString())) {
            if (endD > 31) {
                endD = 31
            }
            wv_day!!.setAdapter(NumericWheelAdapter(startD, endD))
            //            maxItem = endD;
        } else if (list_little.contains(monthNum.toString())) {
            if (endD > 30) {
                endD = 30
            }
            wv_day!!.setAdapter(NumericWheelAdapter(startD, endD))
            //            maxItem = endD;
        } else {
            if (year_num % 4 == 0 && year_num % 100 != 0
                || year_num % 400 == 0
            ) {
                if (endD > 29) {
                    endD = 29
                }
                wv_day!!.setAdapter(NumericWheelAdapter(startD, endD))
                //                maxItem = endD;
            } else {
                if (endD > 28) {
                    endD = 28
                }
                wv_day!!.setAdapter(NumericWheelAdapter(startD, endD))
                //                maxItem = endD;
            }
        }
        if (currentItem > wv_day!!.getAdapter()!!.itemsCount() - 1) {
            currentItem = wv_day!!.getAdapter()!!.itemsCount() - 1
            wv_day!!.currentItem = currentItem
        }
    }

    private fun setContentTextSize() {
        wv_day!!.setTextSize(textSize.toFloat())
        wv_month!!.setTextSize(textSize.toFloat())
        wv_year!!.setTextSize(textSize.toFloat())
        wv_hours!!.setTextSize(textSize.toFloat())
        wv_minutes!!.setTextSize(textSize.toFloat())
        wv_seconds!!.setTextSize(textSize.toFloat())
    }

    fun setLabels(
        label_year: String?,
        label_month: String?,
        label_day: String?,
        label_hours: String?,
        label_mins: String?,
        label_seconds: String?
    ) {
        if (isLunarMode) {
            return
        }
        if (label_year != null) {
            wv_year!!.setLabel(label_year)
        } else {
            wv_year!!.setLabel(view.context.getString(R.string.pickerview_year))
        }
        if (label_month != null) {
            wv_month!!.setLabel(label_month)
        } else {
            wv_month!!.setLabel(view.context.getString(R.string.pickerview_month))
        }
        if (label_day != null) {
            wv_day!!.setLabel(label_day)
        } else {
            wv_day!!.setLabel(view.context.getString(R.string.pickerview_day))
        }
        if (label_hours != null) {
            wv_hours!!.setLabel(label_hours)
        } else {
            wv_hours!!.setLabel(view.context.getString(R.string.pickerview_hours))
        }
        if (label_mins != null) {
            wv_minutes!!.setLabel(label_mins)
        } else {
            wv_minutes!!.setLabel(view.context.getString(R.string.pickerview_minutes))
        }
        if (label_seconds != null) {
            wv_seconds!!.setLabel(label_seconds)
        } else {
            wv_seconds!!.setLabel(view.context.getString(R.string.pickerview_seconds))
        }
    }

    fun setTextXOffset(
        x_offset_year: Int, x_offset_month: Int, x_offset_day: Int,
        x_offset_hours: Int, x_offset_minutes: Int, x_offset_seconds: Int
    ) {
        wv_year!!.setTextXOffset(x_offset_year)
        wv_month!!.setTextXOffset(x_offset_month)
        wv_day!!.setTextXOffset(x_offset_day)
        wv_hours!!.setTextXOffset(x_offset_hours)
        wv_minutes!!.setTextXOffset(x_offset_minutes)
        wv_seconds!!.setTextXOffset(x_offset_seconds)
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic
     */
    fun setCyclic(cyclic: Boolean) {
        wv_year!!.setCyclic(cyclic)
        wv_month!!.setCyclic(cyclic)
        wv_day!!.setCyclic(cyclic)
        wv_hours!!.setCyclic(cyclic)
        wv_minutes!!.setCyclic(cyclic)
        wv_seconds!!.setCyclic(cyclic)
    }

    val time: String
        get() {
            if (isLunarMode) {
                //如果是农历 返回对应的公历时间
                return lunarTime
            }
            val sb = StringBuilder()
            if (currentYear == startYear) {
                /* int i = wv_month.getCurrentItem() + startMonth;
            System.out.println("i:" + i);*/
                if (wv_month!!.currentItem + startMonth == startMonth) {
                    sb.append(wv_year!!.currentItem + startYear).append("-")
                        .append(wv_month!!.currentItem + startMonth).append("-")
                        .append(wv_day!!.currentItem + startDay).append(" ")
                        .append(wv_hours!!.currentItem).append(":")
                        .append(wv_minutes!!.currentItem).append(":")
                        .append(wv_seconds!!.currentItem)
                } else {
                    sb.append(wv_year!!.currentItem + startYear).append("-")
                        .append(wv_month!!.currentItem + startMonth).append("-")
                        .append(wv_day!!.currentItem + 1).append(" ")
                        .append(wv_hours!!.currentItem).append(":")
                        .append(wv_minutes!!.currentItem).append(":")
                        .append(wv_seconds!!.currentItem)
                }
            } else {
                sb.append(wv_year!!.currentItem + startYear).append("-")
                    .append(wv_month!!.currentItem + 1).append("-")
                    .append(wv_day!!.currentItem + 1).append(" ")
                    .append(wv_hours!!.currentItem).append(":")
                    .append(wv_minutes!!.currentItem).append(":")
                    .append(wv_seconds!!.currentItem)
            }
            return sb.toString()
        }
    private val lunarTime: String
        /**
         * 农历返回对应的公历时间
         *
         * @return
         */
        private get() {
            val sb = StringBuilder()
            val year = wv_year!!.currentItem + startYear
            var month = 1
            var isLeapMonth = false
            if (ChinaDate.leapMonth(year) == 0) {
                month = wv_month!!.currentItem + 1
            } else {
                if (wv_month!!.currentItem + 1 - ChinaDate.leapMonth(year) <= 0) {
                    month = wv_month!!.currentItem + 1
                } else if (wv_month!!.currentItem + 1 - ChinaDate.leapMonth(year) == 1) {
                    month = wv_month!!.currentItem
                    isLeapMonth = true
                } else {
                    month = wv_month!!.currentItem
                }
            }
            val day = wv_day!!.currentItem + 1
            val solar = LunarCalendar.lunarToSolar(year, month, day, isLeapMonth)
            sb.append(solar[0]).append("-")
                .append(solar[1]).append("-")
                .append(solar[2]).append(" ")
                .append(wv_hours!!.currentItem).append(":")
                .append(wv_minutes!!.currentItem).append(":")
                .append(wv_seconds!!.currentItem)
            return sb.toString()
        }

    fun setRangDate(startDate: Calendar?, endDate: Calendar?) {
        if (startDate == null && endDate != null) {
            val year = endDate[Calendar.YEAR]
            val month = endDate[Calendar.MONTH] + 1
            val day = endDate[Calendar.DAY_OF_MONTH]
            if (year > startYear) {
                endYear = year
                endMonth = month
                endDay = day
            } else if (year == startYear) {
                if (month > startMonth) {
                    endYear = year
                    endMonth = month
                    endDay = day
                } else if (month == startMonth) {
                    if (day > startDay) {
                        endYear = year
                        endMonth = month
                        endDay = day
                    }
                }
            }
        } else if (startDate != null && endDate == null) {
            val year = startDate[Calendar.YEAR]
            val month = startDate[Calendar.MONTH] + 1
            val day = startDate[Calendar.DAY_OF_MONTH]
            if (year < endYear) {
                startMonth = month
                startDay = day
                startYear = year
            } else if (year == endYear) {
                if (month < endMonth) {
                    startMonth = month
                    startDay = day
                    startYear = year
                } else if (month == endMonth) {
                    if (day < endDay) {
                        startMonth = month
                        startDay = day
                        startYear = year
                    }
                }
            }
        } else if (startDate != null && endDate != null) {
            startYear = startDate[Calendar.YEAR]
            endYear = endDate[Calendar.YEAR]
            startMonth = startDate[Calendar.MONTH] + 1
            endMonth = endDate[Calendar.MONTH] + 1
            startDay = startDate[Calendar.DAY_OF_MONTH]
            endDay = endDate[Calendar.DAY_OF_MONTH]
        }
    }

    /**
     * 设置间距倍数,但是只能在1.0-4.0f之间
     *
     * @param lineSpacingMultiplier
     */
    fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        wv_day!!.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_month!!.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_year!!.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_hours!!.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_minutes!!.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_seconds!!.setLineSpacingMultiplier(lineSpacingMultiplier)
    }

    /**
     * 设置分割线的颜色
     *
     * @param dividerColor
     */
    fun setDividerColor(dividerColor: Int) {
        wv_day!!.setDividerColor(dividerColor)
        wv_month!!.setDividerColor(dividerColor)
        wv_year!!.setDividerColor(dividerColor)
        wv_hours!!.setDividerColor(dividerColor)
        wv_minutes!!.setDividerColor(dividerColor)
        wv_seconds!!.setDividerColor(dividerColor)
    }

    /**
     * 设置分割线的类型
     *
     * @param dividerType
     */
    fun setDividerType(dividerType: DividerType?) {
        wv_day!!.setDividerType(dividerType)
        wv_month!!.setDividerType(dividerType)
        wv_year!!.setDividerType(dividerType)
        wv_hours!!.setDividerType(dividerType)
        wv_minutes!!.setDividerType(dividerType)
        wv_seconds!!.setDividerType(dividerType)
    }

    /**
     * 设置分割线之间的文字的颜色
     *
     * @param textColorCenter
     */
    fun setTextColorCenter(textColorCenter: Int) {
        wv_day!!.setTextColorCenter(textColorCenter)
        wv_month!!.setTextColorCenter(textColorCenter)
        wv_year!!.setTextColorCenter(textColorCenter)
        wv_hours!!.setTextColorCenter(textColorCenter)
        wv_minutes!!.setTextColorCenter(textColorCenter)
        wv_seconds!!.setTextColorCenter(textColorCenter)
    }

    /**
     * 设置分割线以外文字的颜色
     *
     * @param textColorOut
     */
    fun setTextColorOut(textColorOut: Int) {
        wv_day!!.setTextColorOut(textColorOut)
        wv_month!!.setTextColorOut(textColorOut)
        wv_year!!.setTextColorOut(textColorOut)
        wv_hours!!.setTextColorOut(textColorOut)
        wv_minutes!!.setTextColorOut(textColorOut)
        wv_seconds!!.setTextColorOut(textColorOut)
    }

    /**
     * @param isCenterLabel 是否只显示中间选中项的
     */
    fun isCenterLabel(isCenterLabel: Boolean) {
        wv_day!!.isCenterLabel(isCenterLabel)
        wv_month!!.isCenterLabel(isCenterLabel)
        wv_year!!.isCenterLabel(isCenterLabel)
        wv_hours!!.isCenterLabel(isCenterLabel)
        wv_minutes!!.isCenterLabel(isCenterLabel)
        wv_seconds!!.isCenterLabel(isCenterLabel)
    }

    fun setSelectChangeCallback(mSelectChangeCallback: ISelectTimeCallback?) {
        this.mSelectChangeCallback = mSelectChangeCallback
    }

    fun setItemsVisible(itemsVisibleCount: Int) {
        wv_day!!.setItemsVisibleCount(itemsVisibleCount)
        wv_month!!.setItemsVisibleCount(itemsVisibleCount)
        wv_year!!.setItemsVisibleCount(itemsVisibleCount)
        wv_hours!!.setItemsVisibleCount(itemsVisibleCount)
        wv_minutes!!.setItemsVisibleCount(itemsVisibleCount)
        wv_seconds!!.setItemsVisibleCount(itemsVisibleCount)
    }

    fun setAlphaGradient(isAlphaGradient: Boolean) {
        wv_day!!.setAlphaGradient(isAlphaGradient)
        wv_month!!.setAlphaGradient(isAlphaGradient)
        wv_year!!.setAlphaGradient(isAlphaGradient)
        wv_hours!!.setAlphaGradient(isAlphaGradient)
        wv_minutes!!.setAlphaGradient(isAlphaGradient)
        wv_seconds!!.setAlphaGradient(isAlphaGradient)
    }

    companion object {
        @JvmField
        var dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private const val DEFAULT_START_YEAR = 1900
        private const val DEFAULT_END_YEAR = 2100
        private const val DEFAULT_START_MONTH = 1
        private const val DEFAULT_END_MONTH = 12
        private const val DEFAULT_START_DAY = 1
        private const val DEFAULT_END_DAY = 31
    }
}