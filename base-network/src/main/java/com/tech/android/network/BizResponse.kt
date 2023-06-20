package com.tech.android.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @auther: xuan
 * @date  : 2023/6/20 .
 * <P>
 * Description:
 * <P>
 */
class BizResponse(
    val code: String? = "",
    val failCode: String? = "",
    @SerializedName("message")
    val msg: String? = "",
    @SerializedName("object")
    val data: Any? = null,
    val e: Exception? = null,
) : Serializable {


    val netFailed: Boolean
        get() = this.e != null

    val bizFailed: Boolean
        get() = this.code != "00"

    val isSuccessful: Boolean
        get() = !netFailed && !bizFailed

    val body: Any?
        get() = this.data
}