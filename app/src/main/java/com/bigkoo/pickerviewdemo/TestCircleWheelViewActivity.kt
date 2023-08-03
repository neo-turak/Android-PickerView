package com.bigkoo.pickerviewdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.contrarywind.listener.OnItemSelectedListener
import com.contrarywind.view.WheelView


/**
 * desc:
 * author: Created by lixiaotong on 2019-07-30
 * e-mail: 516030811@qq.com
 */
class TestCircleWheelViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_circle_wheelview)
        val wheelView = findViewById<WheelView>(R.id.wheelview)
        wheelView.setTextSize(20f)
        wheelView.setLineSpacingMultiplier(2f)
        // wheelView.setDividerWidth(6);
        wheelView.setDividerType(WheelView.DividerType.CIRCLE)
        val mOptionsItems: MutableList<String?> = ArrayList()
        mOptionsItems.add("10")
        mOptionsItems.add("20")
        mOptionsItems.add("30")
        mOptionsItems.add("40")
        mOptionsItems.add("50")
        mOptionsItems.add("60")
        mOptionsItems.add("70")
        wheelView.setAdapter(ArrayWheelAdapter<Any?>(mOptionsItems))
        wheelView.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                Toast.makeText(
                    this@TestCircleWheelViewActivity,
                    "" + mOptionsItems[index],
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}