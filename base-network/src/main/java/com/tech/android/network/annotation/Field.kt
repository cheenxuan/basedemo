package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description: POST request params
 * 
 * @param value param name must be String
 * @param nullable when param value is null， remove this param from request body
 * 
 * <P>
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val value: String, val nullable: Boolean = false)

