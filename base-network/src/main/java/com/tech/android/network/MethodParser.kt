package com.tech.android.network

import com.tech.android.network.annotation.*
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
class MethodParser(private val baseUrl: String, private val method: Method) {

    /**
     *  domain url
     */
    private var domainUrl: String = baseUrl

    /**
     * relative url default ""
     */
    private lateinit var relativeUrl: String

    /**
     *  http method
     */
    private var httpMethod: Int = 0

    /**
     *  request body is json
     */
    private var jsonPost: Boolean = true

    /**
     * streaming tag
     */
    private var streaming: Boolean = false

    /**
     * headers
     */
    private var headers: MutableMap<String, String> = mutableMapOf()

    /**
     * params Map
     */
    private var parameters: MutableMap<String, Any> = mutableMapOf()

    private var returnType: Type? = null

    init {
        //parse method annotations such get,headers,post,streaming
        parseMethodAnnotations(method)
    }

    private fun parseMethodAnnotations(method: Method) {

        for (annotation: Annotation in method.annotations) {
            when (annotation) {
                is GET -> {
                    relativeUrl = annotation.value
                    httpMethod = BaseRequest.METHOD.GET
                }
                is POST -> {
                    relativeUrl = annotation.value
                    httpMethod = BaseRequest.METHOD.POST
                    jsonPost = annotation.jsonPost
                }
                is Headers -> {
                    val headersArray: Array<out String> = annotation.value
                    for (header: String in headersArray) {
                        val colon = header.indexOf(":")
                        check(!(colon == 0 || colon == -1)) {
                            String.format(
                                "@headers value must be in the from [name:value],but found [%s].",
                                header
                            )
                        }
                        val name = header.substring(0, colon)
                        val value = header.substring(colon + 1).trim()
                        headers[name] = value
                    }
                }
                is Streaming -> {
                    streaming = true
                }
                else -> {
                    throw IllegalStateException("cannot handle method annotation : " + annotation.javaClass.toString())
                }
            }

            require(httpMethod == BaseRequest.METHOD.GET || httpMethod == BaseRequest.METHOD.POST) {
                String.format("method %s must has one of GET,POST", method.name)
            }
        }
    }

    fun parseCallbackReturnType(genericReturnType: Type) {
        if (genericReturnType is ParameterizedType) {
            val actualTypeArguments = genericReturnType.actualTypeArguments
            require(actualTypeArguments.size == 1) { "method ${method.name} can only has one generic return type." }
            returnType = actualTypeArguments[0]
        } else {
            throw java.lang.IllegalStateException("mthod ${method.name} must has one generic return type.")
        }
    }

    fun newRequest(method: Method, args: Array<out Any>): BaseRequest {
        val arguments = args as Array<Any>
        parseMethodParameters(method, arguments)

        val request = BaseRequest()
        request.domainUrl = domainUrl
        request.relativeUrl = relativeUrl
        request.parammeters = parameters
        request.returnType = returnType
        request.headers = headers
        request.httpMethod = httpMethod
        request.jsonPost = jsonPost
        request.streaming = streaming
        return request
    }

    private fun parseMethodParameters(method: Method, args: Array<out Any>) {
        val parameterAnnotations = method.parameterAnnotations
        val equals = (parameterAnnotations.size - 1) == args.size

        require(equals) {
            String.format(
                "argument annotations count %s don't match expect count %s",
                parameterAnnotations.size,
                args.size
            )
        }

        for (index in args.indices) {
            val annotations = parameterAnnotations[index]
            require(annotations.size <= 1) {
                "field can only has one annotation: index = $index"
            }
            val paramValue = args[index]

            if (paramValue is kotlin.coroutines.Continuation<*>) {
                continue
            }

            require(isPrimitive(paramValue)) {
                "basic types and callback are supported for now,index = $index"
            }

            val annotation = annotations[0]
            if (annotation is Field) {
                val paramKey = annotation.value
                val nullable = annotation.nullable

                if (nullable && isEmpty(paramValue)) {
                    if (parameters.containsKey(paramKey)) parameters.remove(paramKey)
                    continue
                } else {
                    parameters[paramKey] = paramValue
                }
            } else if (annotation is QUERY) {
                val paramKey = annotation.value
                val nullable = annotation.nullable

                if (nullable && isEmpty(paramValue)) {
                    if (parameters.containsKey(paramKey)) parameters.remove(paramKey)
                    continue
                } else {
                    parameters[paramKey] = paramValue
                }
            } else if (annotation is Path) {
                val replaceName = annotation.value
                val replaceValue = paramValue.toString()
                if (replaceName != null && replaceValue != null) {
                    relativeUrl = relativeUrl.replace("{$replaceName}", replaceValue)
                }
            } else {
                throw java.lang.IllegalStateException("cannot handle parameter annotation: " + annotation.javaClass.toString())
            }

        }
    }

    private fun isPrimitive(value: Any): Boolean {
        //String
        if (value.javaClass == String::class.java) {
            return true
        }

        //List 、 ArrayList
        if (value.javaClass == List::class.java || value.javaClass == ArrayList::class.java) {
            return true
        }

        //callback
        if (value.javaClass == ICallBack::class.java) {
            return true
        }

        try {
            //int byte short long boolean char double float
            val field = value.javaClass.getField("TYPE")
            val clazz = field[null] as Class<*>
            if (clazz.isPrimitive) {
                return true
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

        return false
    }

    private fun isEmpty(value: Any?): Boolean {
        return if (value?.javaClass == List::class.java || value?.javaClass == ArrayList::class.java) {
            false
        } else {
            "" == value.toString()
        }
    }

    companion object {
        fun parse(baseUrl: String, method: Method): MethodParser {
            return MethodParser(baseUrl, method)
        }
    }
}