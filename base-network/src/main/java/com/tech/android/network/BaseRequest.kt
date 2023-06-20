package com.tech.android.network


import androidx.annotation.IntDef
import java.lang.reflect.Type


/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
open class BaseRequest {

    @METHOD
    var httpMethod: Int = 0
    var headers: MutableMap<String, String> = mutableMapOf()
    var parammeters: HashMap<String, Any?> = HashMap()
    var domainUrl: String? = null
    var relativeUrl: String? = null
    var jsonPost: Boolean = true
    var streaming: Boolean = false
    var returnType: Type? = null
    
    @IntDef(value = [METHOD.GET, METHOD.POST])
    annotation class METHOD {
        companion object {
            const val GET = 0
            const val POST = 1
        }
    }

    /**
     *  get full url
     */
    fun endPointUrl(): String {
        
        if (relativeUrl == null || (relativeUrl?.length ?: 0) <= 0) {
            throw IllegalStateException("relative url must not be null.")
        }

        if (httpMethod == METHOD.GET) {
            var isHasParams = relativeUrl?.contains("?") == true
            parammeters?.forEach { (key, value) -> 
                if(isHasParams){
                    relativeUrl += "&${key}=${value.toString()}"
                }else{
                    relativeUrl += "?${key}=${value.toString()}"
                    isHasParams = true
                }
            }
        }

        return if (relativeUrl!!.startsWith("http://") || relativeUrl!!.startsWith("https://")) {
            relativeUrl!!
        }else {
            domainUrl + relativeUrl
        }
    }

    /**
     * add header 
     */
    fun addHeader(name: String, value: String) {
        headers[name] = value
    }
}