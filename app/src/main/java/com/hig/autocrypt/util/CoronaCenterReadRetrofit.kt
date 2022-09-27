package com.hig.autocrypt.util

import com.hig.autocrypt.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CoronaCenterReadRetrofit {
    private const val BASE_URL = BuildConfig.CORONA_BASE_URL

    private fun getCoronaCenterReadRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    fun getCoronaRequest(): CoronaRequest {
        return getCoronaCenterReadRetrofit().create(CoronaRequest::class.java)
    }
}