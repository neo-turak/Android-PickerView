package com.bigkoo.pickerview.view

import android.graphics.Typeface
import android.view.View
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener
import com.contrarywind.listener.OnItemSelectedListener
import com.contrarywind.view.WheelView
import com.contrarywind.view.WheelView.DividerType

class WheelOptions<T>(
    var view: View, //切换时，还原第一项
    private val isRestoreItem: Boolean
) {
    private val wvOption1: WheelView = view.findViewById<View>(R.id.options1) as WheelView
    private val wvOption2: WheelView = view.findViewById<View>(R.id.options2) as WheelView
    private val wvOption3: WheelView = view.findViewById<View>(R.id.options3) as WheelView
    private  var mOptions1Items: List<T>? = null
    private  var mOptions2Items: List<List<T>>? = null
    private  var mOptions3Items: List<List<List<T>>> ? = null
    private  var linkage = true //默认联动
    private var wheelListenerOption1: OnItemSelectedListener? = null
    private var wheelListenerOption2: OnItemSelectedListener? = null
    private var optionsSelectChangeListener: OnOptionsSelectChangeListener? = null

    init {
        // 初始化时显示的数据
    }

    fun setPicker(
        options1Items: List<T>?,
        options2Items: List<List<T>>?,
        options3Items: List<List<List<T>>>?
    ) {
        mOptions1Items = options1Items
        mOptions2Items = options2Items
        mOptions3Items = options3Items

        // 选项1
        if (mOptions1Items!=null){
            wvOption1.setAdapter(ArrayWheelAdapter(mOptions1Items!!)) // 设置显示数据
            wvOption1.currentItem = 0 // 初始化时显示的数据
        }
        // 选项2
        if (mOptions2Items != null) {
            wvOption2.setAdapter(ArrayWheelAdapter(mOptions2Items!![0])) // 设置显示数据
        }
        wvOption2.currentItem = wvOption2.currentItem // 初始化时显示的数据
        // 选项3
        if (mOptions3Items != null) {
            wvOption3.setAdapter(ArrayWheelAdapter(mOptions3Items!![0][0])) // 设置显示数据
        }
        wvOption3.currentItem = wvOption3.currentItem
        wvOption1.setIsOptions(true)
        wvOption2.setIsOptions(true)
        wvOption3.setIsOptions(true)
        if (mOptions2Items == null) {
            wvOption2.visibility = View.GONE
        } else {
            wvOption2.visibility = View.VISIBLE
        }
        if (mOptions3Items == null) {
            wvOption3.visibility = View.GONE
        } else {
            wvOption3.visibility = View.VISIBLE
        }

        // 联动监听器
        wheelListenerOption1 = object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                var opt2Select = 0
                if (mOptions2Items == null) { //只有1级联动数据
                    if (optionsSelectChangeListener != null) {
                        optionsSelectChangeListener!!.onOptionsSelectChanged(
                            wvOption1.currentItem,
                            0,
                            0
                        )
                    }
                } else {
                    if (!isRestoreItem) {
                        opt2Select = wvOption2.currentItem //上一个opt2的选中位置
                        //新opt2的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                        opt2Select =
                            if (opt2Select >= mOptions2Items!![index].size - 1) mOptions2Items!![index].size - 1 else opt2Select
                    }
                    wvOption2.setAdapter(ArrayWheelAdapter<Any?>(mOptions2Items!![index]))
                    wvOption2.currentItem = opt2Select
                    if (mOptions3Items != null) {
                        wheelListenerOption2!!.onItemSelected(opt2Select)
                    } else { //只有2级联动数据，滑动第1项回调
                        if (optionsSelectChangeListener != null) {
                            optionsSelectChangeListener!!.onOptionsSelectChanged(
                                index,
                                opt2Select,
                                0
                            )
                        }
                    }
                }
            }
        }
        wheelListenerOption2 = object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                var index1 = index
                if (mOptions3Items != null) {
                    var opt1Select = wvOption1.currentItem
                    opt1Select =
                        if (opt1Select >= mOptions3Items!!.size - 1) mOptions3Items!!.size - 1 else opt1Select
                    index1 =
                        if (index1 >= mOptions2Items!![opt1Select].size - 1) mOptions2Items!![opt1Select].size - 1 else index1
                    var opt3 = 0
                    if (!isRestoreItem) {
                        // wv_option3.getCurrentItem() 上一个opt3的选中位置
                        //新opt3的位置，判断如果旧位置没有超过数据范围，则沿用旧位置，否则选中最后一项
                        opt3 =
                            if (wvOption3.currentItem >= mOptions3Items!![opt1Select][index1].size - 1) mOptions3Items!![opt1Select][index1].size - 1 else wvOption3.currentItem
                    }
                    wvOption3.setAdapter(ArrayWheelAdapter<Any?>(mOptions3Items!![wvOption1.currentItem][index1]))
                    wvOption3.currentItem = opt3

                    //3级联动数据实时回调
                    if (optionsSelectChangeListener != null) {
                        optionsSelectChangeListener!!.onOptionsSelectChanged(
                            wvOption1.currentItem,
                            index1,
                            opt3
                        )
                    }
                } else { //只有2级联动数据，滑动第2项回调
                    if (optionsSelectChangeListener != null) {
                        optionsSelectChangeListener!!.onOptionsSelectChanged(
                            wvOption1.currentItem,
                            index1,
                            0
                        )
                    }
                }
            }
        }

        // 添加联动监听
        if (options1Items != null && linkage) {
            wvOption1.setOnItemSelectedListener(wheelListenerOption1!!)
        }
        if (options2Items != null && linkage) {
            wvOption2.setOnItemSelectedListener(wheelListenerOption2!!)
        }
        if (options3Items != null && linkage && optionsSelectChangeListener != null) {
            wvOption3.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    optionsSelectChangeListener!!.onOptionsSelectChanged(
                        wvOption1.currentItem,
                        wvOption2.currentItem,
                        index
                    )
                }
            })
        }
    }

    //不联动情况下
    fun setNPicker(options1Items: List<T>?, options2Items: List<T>?, options3Items: List<T>?) {

        // 选项1
        if (options1Items!=null){
            wvOption1.setAdapter(ArrayWheelAdapter(options1Items)) // 设置显示数据
            wvOption1.currentItem = 0 // 初始化时显示的数据
        }
        // 选项2
        if (options2Items != null) {
            wvOption2.setAdapter(ArrayWheelAdapter(options2Items)) // 设置显示数据
        }
        wvOption2.currentItem = wvOption2.currentItem // 初始化时显示的数据
        // 选项3
        if (options3Items != null) {
            wvOption3.setAdapter(ArrayWheelAdapter(options3Items)) // 设置显示数据
        }
        wvOption3.currentItem = wvOption3.currentItem
        wvOption1.setIsOptions(true)
        wvOption2.setIsOptions(true)
        wvOption3.setIsOptions(true)
        if (optionsSelectChangeListener != null) {
            wvOption1.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    optionsSelectChangeListener!!.onOptionsSelectChanged(
                        index,
                        wvOption2.currentItem,
                        wvOption3.currentItem
                    )
                }
            })
        }
        if (options2Items == null) {
            wvOption2.visibility = View.GONE
        } else {
            wvOption2.visibility = View.VISIBLE
            if (optionsSelectChangeListener != null) {
                wvOption2.setOnItemSelectedListener(object : OnItemSelectedListener {
                    override fun onItemSelected(index: Int) {
                        optionsSelectChangeListener!!.onOptionsSelectChanged(
                            wvOption1.currentItem,
                            index,
                            wvOption3.currentItem
                        )
                    }
                })
            }
        }
        if (options3Items == null) {
            wvOption3.visibility = View.GONE
        } else {
            wvOption3.visibility = View.VISIBLE
            if (optionsSelectChangeListener != null) {
                wvOption3.setOnItemSelectedListener(object : OnItemSelectedListener {
                    override fun onItemSelected(index: Int) {
                        optionsSelectChangeListener!!.onOptionsSelectChanged(
                            wvOption1.currentItem,
                            wvOption2.currentItem,
                            index
                        )
                    }
                })
            }
        }
    }

    fun setTextContentSize(textSize: Int) {
        wvOption1.setTextSize(textSize.toFloat())
        wvOption2.setTextSize(textSize.toFloat())
        wvOption3.setTextSize(textSize.toFloat())
    }

    private fun setLineSpacingMultiplier() {}

    /**
     * 设置选项的单位
     *
     * @param label1 单位
     * @param label2 单位
     * @param label3 单位
     */
    fun setLabels(label1: String?, label2: String?, label3: String?) {
        if (label1 != null) {
            wvOption1.setLabel(label1)
        }
        if (label2 != null) {
            wvOption2.setLabel(label2)
        }
        if (label3 != null) {
            wvOption3.setLabel(label3)
        }
    }

    /**
     * 设置x轴偏移量
     */
    fun setTextXOffset(x_offset_one: Int, x_offset_two: Int, x_offset_three: Int) {
        wvOption1.setTextXOffset(x_offset_one)
        wvOption2.setTextXOffset(x_offset_two)
        wvOption3.setTextXOffset(x_offset_three)
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    fun setCyclic(cyclic: Boolean) {
        wvOption1.setCyclic(cyclic)
        wvOption2.setCyclic(cyclic)
        wvOption3.setCyclic(cyclic)
    }

    /**
     * 设置字体样式
     *
     * @param font 系统提供的几种样式
     */
    fun setTypeface(font: Typeface?) {
        wvOption1.setTypeface(font!!)
        wvOption2.setTypeface(font)
        wvOption3.setTypeface(font)
    }

    /**
     * 分别设置第一二三级是否循环滚动
     *
     * @param cyclic1,cyclic2,cyclic3 是否循环
     */
    fun setCyclic(cyclic1: Boolean, cyclic2: Boolean, cyclic3: Boolean) {
        wvOption1.setCyclic(cyclic1)
        wvOption2.setCyclic(cyclic2)
        wvOption3.setCyclic(cyclic3)
    }

    val currentItems: IntArray
        /**
         * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引，0，1，2。
         * 在快速滑动未停止时，点击确定按钮，会进行判断，如果匹配数据越界，则设为0，防止index出错导致崩溃。
         *
         * @return 索引数组
         */
        get() {
            val currentItems = IntArray(3)
            currentItems[0] = wvOption1.currentItem
            if (mOptions2Items != null && mOptions2Items!!.size > 0) { //非空判断
                currentItems[1] =
                    if (wvOption2.currentItem > mOptions2Items!![currentItems[0]].size - 1) 0 else wvOption2.currentItem
            } else {
                currentItems[1] = wvOption2.currentItem
            }
            if (mOptions3Items != null && mOptions3Items!!.size > 0) { //非空判断
                currentItems[2] =
                    if (wvOption3.currentItem > mOptions3Items!![currentItems[0]][currentItems[1]].size - 1) 0 else wvOption3.currentItem
            } else {
                currentItems[2] = wvOption3.currentItem
            }
            return currentItems
        }

    fun setCurrentItems(option1: Int, option2: Int, option3: Int) {
        if (linkage) {
            itemSelected(option1, option2, option3)
        } else {
            wvOption1.currentItem = option1
            wvOption2.currentItem = option2
            wvOption3.currentItem = option3
        }
    }

    private fun itemSelected(opt1Select: Int, opt2Select: Int, opt3Select: Int) {
        if (mOptions1Items != null) {
            wvOption1.currentItem = opt1Select
        }
        if (mOptions2Items != null) {
            wvOption2.setAdapter(ArrayWheelAdapter<Any?>(mOptions2Items!![opt1Select]))
            wvOption2.currentItem = opt2Select
        }
        if (mOptions3Items != null) {
            wvOption3.setAdapter(ArrayWheelAdapter<Any?>(mOptions3Items!![opt1Select][opt2Select]))
            wvOption3.currentItem = opt3Select
        }
    }

    /**
     * 设置间距倍数,但是只能在1.2-4.0f之间
     *
     * @param lineSpacingMultiplier
     */
    fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        wvOption1.setLineSpacingMultiplier(lineSpacingMultiplier)
        wvOption2.setLineSpacingMultiplier(lineSpacingMultiplier)
        wvOption3.setLineSpacingMultiplier(lineSpacingMultiplier)
    }

    /**
     * 设置分割线的颜色
     *
     * @param dividerColor
     */
    fun setDividerColor(dividerColor: Int) {
        wvOption1.setDividerColor(dividerColor)
        wvOption2.setDividerColor(dividerColor)
        wvOption3.setDividerColor(dividerColor)
    }

    /**
     * 设置分割线的类型
     *
     * @param dividerType
     */
    fun setDividerType(dividerType: DividerType?) {
        wvOption1.setDividerType(dividerType)
        wvOption2.setDividerType(dividerType)
        wvOption3.setDividerType(dividerType)
    }

    /**
     * 设置分割线之间的文字的颜色
     *
     * @param textColorCenter
     */
    fun setTextColorCenter(textColorCenter: Int) {
        wvOption1.setTextColorCenter(textColorCenter)
        wvOption2.setTextColorCenter(textColorCenter)
        wvOption3.setTextColorCenter(textColorCenter)
    }

    /**
     * 设置分割线以外文字的颜色
     *
     * @param textColorOut
     */
    fun setTextColorOut(textColorOut: Int) {
        wvOption1.setTextColorOut(textColorOut)
        wvOption2.setTextColorOut(textColorOut)
        wvOption3.setTextColorOut(textColorOut)
    }

    /**
     * Label 是否只显示中间选中项的
     *
     * @param isCenterLabel
     */
    fun isCenterLabel(isCenterLabel: Boolean) {
        wvOption1.isCenterLabel(isCenterLabel)
        wvOption2.isCenterLabel(isCenterLabel)
        wvOption3.isCenterLabel(isCenterLabel)
    }

    fun setOptionsSelectChangeListener(optionsSelectChangeListener: OnOptionsSelectChangeListener?) {
        this.optionsSelectChangeListener = optionsSelectChangeListener
    }

    fun setLinkage(linkage: Boolean) {
        this.linkage = linkage
    }

    /**
     * 设置最大可见数目
     *
     * @param itemsVisible 建议设置为 3 ~ 9之间。
     */
    fun setItemsVisible(itemsVisible: Int) {
        wvOption1.setItemsVisibleCount(itemsVisible)
        wvOption2.setItemsVisibleCount(itemsVisible)
        wvOption3.setItemsVisibleCount(itemsVisible)
    }

    fun setAlphaGradient(isAlphaGradient: Boolean) {
        wvOption1.setAlphaGradient(isAlphaGradient)
        wvOption2.setAlphaGradient(isAlphaGradient)
        wvOption3.setAlphaGradient(isAlphaGradient)
    }
}