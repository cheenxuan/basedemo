package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description: GET请求注解
 * 
 * @param value relative URL 
 * 
 * <P>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(val value: String)