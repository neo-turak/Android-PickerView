package com.bigkoo.pickerviewdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager


class FragmentTestActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragmenttest)
        mFragmentManager = supportFragmentManager
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.frame_activity_main, TestFragment())
        fragmentTransaction.commitAllowingStateLoss()
    }
}