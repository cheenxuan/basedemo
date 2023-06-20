package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description: GET request params
 * 
 * @param value param name must be String
 * @param nullable when param value is null， remove this param from request url
 * 
 * <P>
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class QUERY(val value: String, val nullable: Boolean = false)
