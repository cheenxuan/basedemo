package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description: headers 
 * 
 * @param value variable parameter
 *
 * <example>
 *     @Header("name:value","name1:value1")
 * </example>
 * <P>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Headers(vararg val value:String)
