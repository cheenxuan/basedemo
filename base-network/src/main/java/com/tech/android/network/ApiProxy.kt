package com.tech.android.network

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
open class ApiProxy(private val baseUrl: String) {

    private var methodService: ConcurrentHashMap<Method, MethodParser> = ConcurrentHashMap()

    fun <T> create(service: Class<T>): T {
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf<Class<*>>(service), object : InvocationHandler {
                override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                    var methodParser = methodService[method]
                    if (methodParser == null) {
                        methodParser = MethodParser.parse(baseUrl, method)
                        methodService[method] = methodParser
                    }

                    val callback: ICallBack<*> = args?.get(args.size - 1) as ICallBack<*>
                    methodParser.parseCallbackReturnType(callback.javaClass.genericInterfaces[0])

                    if (args.size > 1) {
                        val argArray = args.copyOfRange(0, args.size - 1)
                        val request = methodParser.newRequest(method, argArray)

                        //执行网络请求
                        HttpUtil.request(request, callback)
                    } else {
                        return null
                    }
                    return null
                }
            }
        ) as T
    }

}