package br.com.gtechconsulting.velocidadedaviapoc.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkUtils {

    companion object {
        fun getRetrofitInstance(path: String): Retrofit {
//            return Retrofit.Builder().baseUrl(path).addConverterFactory(GsonConverterFactory.create()).build()


            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .callTimeout(10,TimeUnit.MINUTES)
                .readTimeout(10,TimeUnit.MINUTES)
                .build()
            val builder = Retrofit.Builder()
                .baseUrl(path)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()).build()

            return builder
        }
    }
}