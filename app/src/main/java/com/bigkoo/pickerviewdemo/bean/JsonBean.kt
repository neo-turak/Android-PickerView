package com.bigkoo.pickerviewdemo.bean

import com.contrarywind.interfaces.IPickerViewData

/**
 * TODO<json数据源>
 *
 * @author: 小嵩
 * @date: 2017/3/16 15:36
</json数据源> */
class JsonBean : IPickerViewData {
    /**
     * name : 省份
     * city : [{"name":"北京市","area":["东城区","西城区","崇文区","宣武区","朝阳区"]}]
     */
    var name: String? = null
    var cityList: List<CityBean>? = null

    // 实现 IPickerViewData 接口，
    // 这个用来显示在PickerView上面的字符串，
    // PickerView会通过IPickerViewData获取getPickerViewText方法显示出来。
    override fun pickerViewText(): String? {
        return name
    }

    class CityBean {
        /**
         * name : 城市
         * area : ["东城区","西城区","崇文区","昌平区"]
         */
        var name: String? = null
        var area: List<String>? = null
    }
}