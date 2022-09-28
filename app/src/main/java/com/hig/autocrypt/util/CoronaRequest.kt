package com.hig.autocrypt.util

import com.hig.autocrypt.BuildConfig
import com.hig.autocrypt.dto.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoronaRequest {
    @GET("15077586/v1/centers")
    suspend fun getPublicHealthOffices(
        @Query("page")page: Int = 1,
        @Query("perPage")perPage: Int = 10,
        @Query("returnType")returnType: String = "json",
        @Query("serviceKey")apiKey: String = BuildConfig.CORONA_PUBLIC_API_KEY_DECODED
    ): Response
}