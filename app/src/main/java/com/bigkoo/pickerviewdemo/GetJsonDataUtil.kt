package com.bigkoo.pickerviewdemo

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * <读取Json文件的工具类>
 *
 * @author: 小嵩
 * @date: 2017/3/16 16:22
</读取Json文件的工具类> */
class GetJsonDataUtil {
    fun getJson(context: Context, fileName: String?): String {
        val stringBuilder = StringBuilder()
        try {
            val assetManager = context.assets
            val bf = BufferedReader(
                InputStreamReader(
                    assetManager.open(fileName!!)
                )
            )
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}