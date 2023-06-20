package com.tech.android.network

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
interface ICallBack<T> {
    
    fun success(data: T?)
    
    fun failed(e: BizResponse)

}