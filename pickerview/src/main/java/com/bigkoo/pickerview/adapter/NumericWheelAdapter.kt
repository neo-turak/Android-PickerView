package com.bigkoo.pickerview.adapter

import com.contrarywind.adapter.WheelAdapter

/**
 * Numeric Wheel adapter.
 */
/**
 * Constructor
 * @param minValue the wheel min value
 * @param maxValue the wheel max value
 */
class NumericWheelAdapter(private val minValue: Int, private val maxValue: Int) :
    WheelAdapter<Any?> {
    override fun getItem(index: Int): Any {
        return if (index >= 0 && index < itemsCount()) {
            minValue + index
        } else 0
    }

    override fun itemsCount(): Int {
        return maxValue - minValue + 1
    }

    override fun indexOf(o: Any?): Int {
        return try {
            o as Int - minValue
        } catch (e: Exception) {
            -1
        }
    }
}