package org.doctorate.aktool.network

import android.util.Log
import org.doctorate.aktool.config.AppConfig
import org.doctorate.aktool.utils.JsonUtil
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object Network {

    private const val TAG = "Create_Retrofit"
    private val serviceMap = mutableMapOf<String, Any>()
    private var retrofit = createRetrofit()

    fun createRetrofit(uri: String = AppConfig.config.serverUri): Retrofit? {
        serviceMap.clear()
        try {
            return Retrofit.Builder()
                .baseUrl(uri.also { Log.d(TAG, "URI: $it") })
                .addConverterFactory(JacksonConverterFactory.create(JsonUtil.mapper))
                .build()
        } catch (_: Exception) {
            Log.e(TAG, "error uri : ${AppConfig.config.serverUri}")
            return null
        }
    }

    fun <T> createService(clazz: Class<T>): T? {
        if (retrofit == null) {
            retrofit = createRetrofit()
        }
        return serviceMap[clazz.simpleName] as? T ?: retrofit?.create(clazz)?.also {
            Log.d(TAG, clazz.simpleName)
            serviceMap[clazz.simpleName] = it
        }
    }

}