package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description: POST请求注解 
 * 
 * @param value relativeUrl
 * @param jsonPost request body is json  
 * 
 * <P>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class POST(val value: String, val jsonPost: Boolean = true)

