package com.tech.android.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
object JsonUtil {

    val gson by lazy {
        Gson()
    }

    inline fun <reified T : Any> String?.fromJson(): T? = this?.let {
        val type = object : TypeToken<T>() {}.type
        gson.fromJson(this, type)
    }

    inline fun <reified T : Any> T?.toJson() = this?.let { Gson().toJson(this, T::class.java) }
}