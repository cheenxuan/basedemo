package com.tech.android.network

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
object HttpUtil {

    fun request(request: BaseRequest, callback: ICallBack<*>) {
        NetworkFactory().request(request, callback)
    }

    private fun getAuthNetWorkFactory(): NetworkFactory {
        return NetworkFactory()
    }
}