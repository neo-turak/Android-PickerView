package com.bigkoo.pickerviewdemo.bean

import com.contrarywind.interfaces.IPickerViewData

/**
 * Created by KyuYi on 2017/3/2.
 * E-Mail:kyu_yi@sina.com
 * 功能：
 */
class CardBean(var id: Int, var cardNo: String) : IPickerViewData {

    override fun pickerViewText(): String {
        return cardNo
    }
}