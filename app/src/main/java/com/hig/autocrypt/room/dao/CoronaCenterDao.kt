package com.hig.autocrypt.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hig.autocrypt.dto.PublicHealth

@Dao
interface CoronaCenterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoronaCenters(vararg center: PublicHealth)

    @Query("SELECT * FROM public_health_tb")
    suspend fun selectCoronaCenters(): List<PublicHealth>
}