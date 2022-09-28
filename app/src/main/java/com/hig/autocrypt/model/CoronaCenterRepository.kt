package com.hig.autocrypt.model

import android.app.Application
import androidx.room.Room
import com.hig.autocrypt.dto.PublicHealth
import com.hig.autocrypt.dto.Response
import com.hig.autocrypt.room.database.AppDatabase
import com.hig.autocrypt.util.CoronaCenterReadRetrofit

class CoronaCenterRepository(application: Application) {
    companion object {
        private const val TAG: String = "로그"
    }

    private val coronaRequestRetrofit = CoronaCenterReadRetrofit.getCoronaRequest()
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "app-database").build()
    private val coronaCenterDao = db.coronaCenterDao()

    suspend fun getCoronaCenter(page: Int, perPage: Int): Response {
        return coronaRequestRetrofit.getPublicHealthOffices(page = page, perPage = perPage)
    }

    suspend fun insertCoronaCenter(publicHealth: PublicHealth) {
        coronaCenterDao.insertCoronaCenters(publicHealth)
    }

    suspend fun selectCoronaCenters(): List<PublicHealth> {
        return coronaCenterDao.selectCoronaCenters()
    }
}