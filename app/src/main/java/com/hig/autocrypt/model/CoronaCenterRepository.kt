package com.hig.autocrypt.model

import com.hig.autocrypt.dto.Response
import com.hig.autocrypt.util.CoronaCenterReadRetrofit

class CoronaCenterRepository {
    companion object {
        private const val TAG: String = "로그"
    }

    private val coronaRequestRetrofit = CoronaCenterReadRetrofit.getCoronaRequest()

    suspend fun getCoronaCenter(page: Int, perPage: Int): Response {
        return coronaRequestRetrofit.getPublicHealthOffices(page = page, perPage = perPage)
    }
}