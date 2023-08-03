package com.bigkoo.pickerview.adapter

import com.contrarywind.adapter.WheelAdapter

/**
 * The simple Array wheel adapter
 * @param <T> the element type
</T> */
/**
 * // items
 * Constructor
 * @param items the items
 */
class ArrayWheelAdapter<T>(private val items: List<T>) : WheelAdapter<T> {
    override fun getItem(index: Int): T {
        return if (index >= 0 && index < items.size) {
            items[index]
        } else "" as T
    }

    override fun itemsCount(): Int {
        return items.size
    }

    override fun indexOf(o: T): Int {
        return items.indexOf(o)
    }
}