package com.tech.android.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type


/**
 * @auther: xuan
 * @date  : 2023/6/19 .
 * <P>
 * Description:
 * <P>
 */
class NetworkFactory() {

    companion object {
        const val MEDIA_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8"
        const val FIELD_TIME_STAMP = "rt"
    }

    private var client: OkHttpClient? = null

    fun setOkhttpClient(client: OkHttpClient): NetworkFactory {
        this.client = client
        return this
    }

    fun getOkHttpClient(): OkHttpClient {
        return client ?: OkHttpClient().newBuilder().build()
    }

    fun <T> get(url: String, params: HashMap<String, Any?>, callback: ICallBack<T>?) {
        val request = BaseRequest()
        request.httpMethod = BaseRequest.METHOD.GET
        request.relativeUrl = url
        request.parammeters = params
        request(request, callback)
    }

    fun post(url: String, params: HashMap<String, Any?>, callback: ICallBack<*>?) {
        val request = BaseRequest()
        request.httpMethod = BaseRequest.METHOD.POST
        request.jsonPost = false
        request.relativeUrl = url
        request.parammeters = params
        request(request, callback)
    }

    fun request(request: BaseRequest, callback: ICallBack<*>?) {

        getOkHttpClient().newCall(createRequest(request)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback?.failed(BizResponse(e = e))
            }

            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body?.string() ?: "{}"
                convertResponse(callback,request.returnType, resStr)
            }
        })
    }

    private fun createRequest(request: BaseRequest): Request {

        val okhttpRequest = Request.Builder()
        request.headers.forEach { (key, value) ->
            okhttpRequest.addHeader(key, value)
        }

        okhttpRequest.url(request.endPointUrl())

        when (request.httpMethod) {
            BaseRequest.METHOD.GET -> okhttpRequest.get()
            BaseRequest.METHOD.POST -> {

                var parammeters = request.parammeters
                if (parammeters == null) {
                    parammeters = hashMapOf()
                }

                parammeters[FIELD_TIME_STAMP] = System.currentTimeMillis().toString()

                val requestBody = if (request.jsonPost) {
                    val mediaType = MEDIA_TYPE_APPLICATION_JSON.toMediaTypeOrNull()
                    val jsonBody = JSONObject(parammeters).toString();
                    RequestBody.create(mediaType, jsonBody)
                } else {
                    val builder = FormBody.Builder()
                    for ((key, value) in parammeters) {
                        builder.add(key, value.toString())
                    }
                    builder.build()
                }

                okhttpRequest.post(requestBody)
            }
            else -> {
                throw IllegalStateException("restful only support GET POST for now, url = " + request.endPointUrl())
            }
        }

        return okhttpRequest.build()
    }

    private fun convertResponse(callback: ICallBack<*>?, respType: Type?, respData: String) {

        if (isJson(respData)) {
            val responseData = JSONObject(respData)
            val code = responseData.optString("code")
            val msg = responseData.optString("message")
            val failCode = responseData.optString("failCode")
            val data = responseData.opt("object")

            
        } else {

        }
    }

    private fun isJson(jsonString: String): Boolean {
        return try {
            // 尝试将字符串解析为 JSONObject 或 JsonArray，如果解析成功则表示为有效的 JSON 字符串
            JSONObject(jsonString)
            true
        } catch (e: Exception) {
            try {
                JSONArray(jsonString)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}