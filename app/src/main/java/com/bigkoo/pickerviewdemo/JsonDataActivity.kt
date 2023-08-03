package com.bigkoo.pickerviewdemo

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerviewdemo.bean.JsonBean
import com.google.gson.Gson
import org.json.JSONArray

/**
 * 解析省市区数据示例
 *
 * @author 小嵩
 * @date 2017-3-16
 */
class JsonDataActivity : AppCompatActivity(), View.OnClickListener {
    private var options1Items: List<JsonBean> = ArrayList()
    private val options2Items = ArrayList<ArrayList<String>>()
    private val options3Items = ArrayList<ArrayList<ArrayList<String>>>()
    private var thread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json_data)
        initView()
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_LOAD_DATA -> if (thread == null) { //如果已创建就不再重新创建子线程了
                    Toast.makeText(this@JsonDataActivity, "Begin Parse Data", Toast.LENGTH_SHORT)
                        .show()
                    thread = Thread { // 子线程中解析省市区数据
                        initJsonData()
                    }
                    thread!!.start()
                }

                MSG_LOAD_SUCCESS -> {
                    Toast.makeText(this@JsonDataActivity, "Parse Succeed", Toast.LENGTH_SHORT)
                        .show()
                    isLoaded = true
                }

                MSG_LOAD_FAILED -> Toast.makeText(
                    this@JsonDataActivity,
                    "Parse Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initView() {
        findViewById<View>(R.id.btn_data).setOnClickListener(this)
        findViewById<View>(R.id.btn_show).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_data -> mHandler!!.sendEmptyMessage(MSG_LOAD_DATA)
            R.id.btn_show -> if (isLoaded) {
                showPickerView()
            } else {
                Toast.makeText(
                    this@JsonDataActivity,
                    "Please waiting until the data is parsed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showPickerView() { // 弹出选择器
        val pvOptions: OptionsPickerView<Any> =
            OptionsPickerBuilder(this, object : OnOptionsSelectListener {
                override fun onOptionsSelect(
                    options1: Int,
                    options2: Int,
                    options3: Int,
                    v: View?
                ) {
                    //返回的分别是三个级别的选中位置
                    val opt1tx =
                        if (options1Items.isNotEmpty()) options1Items[options1].pickerViewText() else ""
                    val opt2tx = if (options2Items.size > 0
                        && options2Items[options1].size > 0
                    ) options2Items[options1][options2] else ""
                    val opt3tx =
                        if (options2Items.size > 0 && options3Items[options1].size > 0 && options3Items[options1][options2].size > 0) options3Items[options1][options2][options3] else ""
                    val tx = opt1tx + opt2tx + opt3tx
                    Toast.makeText(this@JsonDataActivity, tx, Toast.LENGTH_SHORT).show()
                }
            })
                .setTitleText("城市选择")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build()

        /*pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items) //三级选择器
        pvOptions.show()
    }

    private fun initJsonData() { //解析数据
        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         */
        val JsonData = GetJsonDataUtil().getJson(this, "province.json") //获取assets目录下的json文件数据
        val jsonBean = parseData(JsonData) //用Gson 转成实体
        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBean
        for (i in jsonBean.indices) { //遍历省份
            val cityList = ArrayList<String>() //该省的城市列表（第二级）
            val province_AreaList = ArrayList<ArrayList<String>>() //该省的所有地区列表（第三极）
            for (c in jsonBean[i].cityList!!.indices) { //遍历该省份的所有城市
                val cityName:String = jsonBean[i].cityList?.get(c)?.name!!
                cityList.add(cityName) //添加城市
                val cityArealist = ArrayList<String>() //该城市的所有地区列表
                cityArealist.addAll(jsonBean[i].cityList?.get(c)?.area!!)
                province_AreaList.add(cityArealist) //添加该省所有地区数据
            }
            /**
             * 添加城市数据
             */
            options2Items.add(cityList)
            /**
             * 添加地区数据
             */
            options3Items.add(province_AreaList)
        }
        mHandler!!.sendEmptyMessage(MSG_LOAD_SUCCESS)
    }

    fun parseData(result: String?): ArrayList<JsonBean> { //Gson 解析
        val detail = ArrayList<JsonBean>()
        try {
            val data = JSONArray(result)
            val gson = Gson()
            for (i in 0 until data.length()) {
                val entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean::class.java)
                detail.add(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mHandler!!.sendEmptyMessage(MSG_LOAD_FAILED)
        }
        return detail
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler?.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val MSG_LOAD_DATA = 0x0001
        private const val MSG_LOAD_SUCCESS = 0x0002
        private const val MSG_LOAD_FAILED = 0x0003
        private var isLoaded = false
    }
}