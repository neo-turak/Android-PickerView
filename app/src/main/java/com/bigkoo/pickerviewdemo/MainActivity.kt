package com.bigkoo.pickerviewdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.bigkoo.pickerviewdemo.bean.CardBean
import com.bigkoo.pickerviewdemo.bean.ProvinceBean
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val options1Items = ArrayList<ProvinceBean>()
    private val options2Items = ArrayList<ArrayList<String>>()
    private var btnOptions: Button? = null
    private var btnCustomoptions: Button? = null
    private var btnCustomtime: Button? = null
    private var pvTime: TimePickerView? = null
    private var pvCustomTime: TimePickerView? = null
    private var pvCustomLunar: TimePickerView? = null
    private var pvOptions: OptionsPickerView<Any>? = null
    private var pvCustomOptions: OptionsPickerView<CardBean>? = null
    private var pvNoLinkOptions: OptionsPickerView<String>? = null
    private val cardItem = ArrayList<CardBean>()
    private val food = ArrayList<String>()
    private val clothes = ArrayList<String>()
    private val computer = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //等数据加载完毕再初始化并显示Picker,以免还未加载完数据就显示,造成APP崩溃。
        optionData
        initTimePicker()
        initCustomTimePicker()
        initLunarPicker()
        initOptionPicker()
        initCustomOptionPicker()
        initNoLinkOptionsPicker()
        val btnTime = findViewById<View>(R.id.btn_Time) as Button
        btnOptions = findViewById<View>(R.id.btn_Options) as Button
        btnCustomoptions = findViewById<View>(R.id.btn_CustomOptions) as Button
        btnCustomtime = findViewById<View>(R.id.btn_CustomTime) as Button
        val btnNoLinkage = findViewById<View>(R.id.btn_no_linkage) as Button
        val btnToFragment = findViewById<View>(R.id.btn_fragment) as Button
        val btnCircle = findViewById<View>(R.id.btn_circle) as Button
        btnTime.setOnClickListener(this)
        btnOptions!!.setOnClickListener(this)
        btnCustomoptions!!.setOnClickListener(this)
        btnCustomtime!!.setOnClickListener(this)
        btnNoLinkage.setOnClickListener(this)
        btnToFragment.setOnClickListener(this)
        btnCircle.setOnClickListener(this)
        findViewById<View>(R.id.btn_GotoJsonData).setOnClickListener(this)
        findViewById<View>(R.id.btn_lunar).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_Time && pvTime != null) {
            // pvTime.setDate(Calendar.getInstance());
            /* pvTime.show(); //show timePicker*/
            pvTime!!.show(v) //弹出时间选择器，传递参数过去，回调的时候则可以绑定此view
        } else if (v.id == R.id.btn_Options && pvOptions != null) {
            pvOptions!!.show() //弹出条件选择器
        } else if (v.id == R.id.btn_CustomOptions && pvCustomOptions != null) {
            pvCustomOptions!!.show() //弹出自定义条件选择器
        } else if (v.id == R.id.btn_CustomTime && pvCustomTime != null) {
            pvCustomTime!!.show() //弹出自定义时间选择器
        } else if (v.id == R.id.btn_no_linkage && pvNoLinkOptions != null) { //不联动数据选择器
            pvNoLinkOptions!!.show()
        } else if (v.id == R.id.btn_GotoJsonData) { //跳转到 省市区解析示例页面
            startActivity(Intent(this@MainActivity, JsonDataActivity::class.java))
        } else if (v.id == R.id.btn_fragment) { //跳转到 fragment
            startActivity(Intent(this@MainActivity, FragmentTestActivity::class.java))
        } else if (v.id == R.id.btn_lunar) {
            pvCustomLunar!!.show()
        } else if (v.id == R.id.btn_circle) {
            startActivity(Intent(this@MainActivity, TestCircleWheelViewActivity::class.java))
        }
    }

    /**
     * 农历时间已扩展至 ： 1900 - 2100年
     */
    private fun initLunarPicker() {
        val selectedDate = Calendar.getInstance() //系统当前时间
        val startDate = Calendar.getInstance()
        startDate[2014, 1] = 23
        val endDate = Calendar.getInstance()
        endDate[2069, 2] = 28
        //时间选择器 ，自定义布局
        pvCustomLunar = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) { //选中事件回调
                Toast.makeText(this@MainActivity, getTime(date), Toast.LENGTH_SHORT).show()
            }
        })
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.pickerview_custom_lunar, object : CustomListener {
                override fun customLayout(v: View?) {
                    val tvSubmit = v!!.findViewById<View>(R.id.tv_finish) as TextView
                    val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
                    tvSubmit.setOnClickListener {
                        pvCustomLunar!!.returnData()
                        pvCustomLunar!!.dismiss()
                    }
                    ivCancel.setOnClickListener { pvCustomLunar!!.dismiss() }
                    //公农历切换
                    val cb_lunar = v.findViewById<View>(R.id.cb_lunar) as CheckBox
                    cb_lunar.setOnCheckedChangeListener { buttonView, isChecked ->
                        pvCustomLunar!!.isLunarCalendar = !pvCustomLunar!!.isLunarCalendar
                        //自适应宽
                        setTimePickerChildWeight(
                            v,
                            if (isChecked) 0.8f else 1f,
                            if (isChecked) 1f else 1.1f
                        )
                    }
                }

                /**
                 * 公农历切换后调整宽
                 * @param v
                 * @param yearWeight
                 * @param weight
                 */
                private fun setTimePickerChildWeight(v: View?, yearWeight: Float, weight: Float) {
                    val timePicker = v!!.findViewById<View>(R.id.timepicker) as ViewGroup
                    val year = timePicker.getChildAt(0)
                    val lp = year.layoutParams as LinearLayout.LayoutParams
                    lp.weight = yearWeight
                    year.layoutParams = lp
                    for (i in 1 until timePicker.childCount) {
                        val childAt = timePicker.getChildAt(i)
                        val childLp = childAt.layoutParams as LinearLayout.LayoutParams
                        childLp.weight = weight
                        childAt.layoutParams = childLp
                    }
                }
            })
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setDividerColor(Color.RED)
            .build()
    }

    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        pvTime = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) {
                Toast.makeText(this@MainActivity, getTime(date), Toast.LENGTH_SHORT).show()
                Log.i("pvTime", "onTimeSelect")
            }
        })
            .setTimeSelectChangeListener(object : OnTimeSelectChangeListener {
                override fun onTimeSelectChanged(date: Date?) {
                    Log.i("pvTime", "onTimeSelectChanged")
                }
            })
            .setType(booleanArrayOf(true, true, true, true, true, true))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .build()
        val mDialog = pvTime!!.dialog
        if (mDialog != null) {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
            params.leftMargin = 0
            params.rightMargin = 0
            pvTime!!.dialogContainerLayout?.layoutParams = params
            val dialogWindow = mDialog.window
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f)
            }
        }
    }

    private fun initCustomTimePicker() {
        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */
        val selectedDate = Calendar.getInstance() //系统当前时间
        val startDate = Calendar.getInstance()
        startDate[2014, 1] = 23
        val endDate = Calendar.getInstance()
        endDate[2027, 2] = 28
        //时间选择器 ，自定义布局
        pvCustomTime = TimePickerBuilder(this, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date?, v: View?) { //选中事件回调
                btnCustomtime!!.text = getTime(date)
            }
        }) /*.setType(TimePickerView.Type.ALL)//default is all
                .setCancelText("Cancel")
                .setSubmitText("Sure")
                .setContentTextSize(18)
                .setTitleSize(20)
                .setTitleText("Title")
                .setTitleColor(Color.BLACK)
               / *.setDividerColor(Color.WHITE)//设置分割线的颜色
                .setTextColorCenter(Color.LTGRAY)//设置选中项的颜色
                .setLineSpacingMultiplier(1.6f)//设置两横线之间的间隔倍数
                .setTitleBgColor(Color.DKGRAY)//标题背景颜色 Night mode
                .setBgColor(Color.BLACK)//滚轮背景颜色 Night mode
                .setSubmitColor(Color.WHITE)
                .setCancelColor(Color.WHITE)*/
            /*.animGravity(Gravity.RIGHT)// default is center*/
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.pickerview_custom_time, object : CustomListener {
                override fun customLayout(v: View?) {
                    val tvSubmit = v!!.findViewById<View>(R.id.tv_finish) as TextView
                    val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
                    tvSubmit.setOnClickListener {
                        pvCustomTime!!.returnData()
                        pvCustomTime!!.dismiss()
                    }
                    ivCancel.setOnClickListener { pvCustomTime!!.dismiss() }
                }
            })
            .setContentTextSize(18)
            .setType(booleanArrayOf(false, false, false, true, true, true))
            .setLabel("年", "月", "日", "时", "分", "秒")
            .setLineSpacingMultiplier(1.2f)
            .setTextXOffset(0, 0, 0, 40, 0, -40)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setDividerColor(-0xdb5263)
            .build()
    }

    private fun initOptionPicker() { //条件选择器初始化
        /**
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         */
        pvOptions = OptionsPickerBuilder(this, object : OnOptionsSelectListener {
            override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {
                //返回的分别是三个级别的选中位置
                val tx = (options1Items[options1].pickerViewText()
                        + options2Items[options1][options2] /* + options3Items.get(options1).get(options2).get(options3).getPickerViewText()*/)
                btnOptions!!.text = tx
            }
        })
            .setTitleText("城市选择")
            .setContentTextSize(20) //设置滚轮文字大小
            .setDividerColor(Color.LTGRAY) //设置分割线的颜色
            .setSelectOptions(0, 1) //默认选中项
            .setBgColor(Color.BLACK)
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(Color.LTGRAY)
            .isRestoreItem(true) //切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setLabels("省", "市", "区")
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .setOptionsSelectChangeListener(object : OnOptionsSelectChangeListener {
                override fun onOptionsSelectChanged(options1: Int, options2: Int, options3: Int) {
                    val str = "options1: $options1\noptions2: $options2\noptions3: $options3"
                    Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
                }
            })
            .build()

//        pvOptions.setSelectOptions(1,1);
    //    pvOptions!!.setPicker(options1Items);//一级选择器*/
         pvOptions!!.setPicker(options1Items, options2Items) //二级选择器
       // pvOptions!!.setPicker(options1Items, options2Items,options3Items);//三级选择器*/
    }

    private fun initCustomOptionPicker() { //条件选择器初始化，自定义布局
        /**
         * @description
         *
         * 注意事项：
         * 自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针。
         * 具体可参考demo 里面的两个自定义layout布局。
         */
        pvCustomOptions = OptionsPickerBuilder(this, object : OnOptionsSelectListener {
            override fun onOptionsSelect(options1: Int, option2: Int, options3: Int, v: View?) {
                //返回的分别是三个级别的选中位置
                val tx = cardItem[options1].pickerViewText()
                btnCustomoptions!!.text = tx
            }
        })
            .setLayoutRes(R.layout.pickerview_custom_options, object : CustomListener {
                override fun customLayout(v: View?) {
                    val tvSubmit = v!!.findViewById<View>(R.id.tv_finish) as TextView
                    val tvAdd = v.findViewById<View>(R.id.tv_add) as TextView
                    val ivCancel = v.findViewById<View>(R.id.iv_cancel) as ImageView
                    tvSubmit.setOnClickListener {
                        pvCustomOptions!!.returnData()
                        pvCustomOptions!!.dismiss()
                    }
                    ivCancel.setOnClickListener { pvCustomOptions!!.dismiss() }
                    tvAdd.setOnClickListener {
                        cardData
                        pvCustomOptions!!.setPicker(cardItem)
                    }
                }
            })
            .isDialog(true)
            .setOutSideCancelable(false)
            .build()
        pvCustomOptions!!.setPicker(cardItem) //添加数据
    }

    private fun initNoLinkOptionsPicker() { // 不联动的多级选项
        pvNoLinkOptions = OptionsPickerBuilder(this, object : OnOptionsSelectListener {
            override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {
                val str = """
                    food:${food[options1]}
                    clothes:${clothes[options2]}
                    computer:${computer[options3]}
                    """.trimIndent()
                Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
            }
        })
            .setOptionsSelectChangeListener(object : OnOptionsSelectChangeListener {
                override fun onOptionsSelectChanged(options1: Int, options2: Int, options3: Int) {
                    val str = "options1: $options1\noptions2: $options2\noptions3: $options3"
                    Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
                }
            })
            .setItemVisibleCount(5) // .setSelectOptions(0, 1, 1)
            .build()
        pvNoLinkOptions!!.setNPicker(food, clothes, computer)
        pvNoLinkOptions!!.setSelectOptions(0, 1, 1)
    }

    private fun getTime(date: Date?): String { //可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date!!.time)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(date)
    }

    private val optionData: Unit
        get() {
            /*
         * 注意：如果是添加JavaBean实体数据，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
            cardData
            noLinkData

            //选项1
            options1Items.add(ProvinceBean(0, "广东", "描述部分", "其他数据"))
            options1Items.add(ProvinceBean(1, "湖南", "描述部分", "其他数据"))
            options1Items.add(ProvinceBean(2, "广西", "描述部分", "其他数据"))

            //选项2
            val options2items01 = ArrayList<String>()
            options2items01.add("广州")
            options2items01.add("佛山")
            options2items01.add("东莞")
            options2items01.add("珠海")
            val options2items02 = ArrayList<String>()
            options2items02.add("长沙")
            options2items02.add("岳阳")
            options2items02.add("株洲")
            options2items02.add("衡阳")
            val options2items03 = ArrayList<String>()
            options2items03.add("桂林")
            options2items03.add("玉林")
            options2Items.add(options2items01)
            options2Items.add(options2items02)
            options2Items.add(options2items03)
            /*--------数据源添加完毕---------*/
        }
    private val cardData: Unit
        get() {
            for (i in 0..4) {
                cardItem.add(CardBean(i, "No.ABC12345 $i"))
            }
            for (i in cardItem.indices) {
                if (cardItem[i].cardNo.length > 6) {
                    val str_item = cardItem[i].cardNo.substring(0, 6) + "..."
                    cardItem[i].cardNo = str_item
                }
            }
        }
    private val noLinkData: Unit
        get() {
            food.add("KFC")
            food.add("MacDonald")
            food.add("Pizza hut")
            clothes.add("Nike")
            clothes.add("Adidas")
            clothes.add("Armani")
            computer.add("ASUS")
            computer.add("Lenovo")
            computer.add("Apple")
            computer.add("HP")
        }
}