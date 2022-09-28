package com.hig.autocrypt.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hig.autocrypt.dto.PublicHealth
import com.hig.autocrypt.room.converter.DateConverter
import com.hig.autocrypt.room.dao.CoronaCenterDao

@Database(entities = [PublicHealth::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun coronaCenterDao(): CoronaCenterDao
}