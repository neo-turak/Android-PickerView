package com.bigkoo.pickerviewdemo.bean

import com.contrarywind.interfaces.IPickerViewData

/**
 * Created by Sai on 15/11/22.
 */
class ProvinceBean(
    var id: Long,
    var name: String,
    var description: String,
    var others: String) :
    IPickerViewData {

    //这个用来显示在PickerView上面的字符串,PickerView会通过getPickerViewText方法获取字符串显示出来。
    override fun pickerViewText(): String? {
        return name
    }
}