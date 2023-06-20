package com.tech.android.network.annotation

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:  Dynamic configuration path
 * 
 * @param value Dynamic path replace relative url or absolute url content
 * 
 * <P>
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path(val value:String)
