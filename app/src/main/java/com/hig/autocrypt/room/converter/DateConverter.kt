package com.hig.autocrypt.room.converter

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    @TypeConverter
    fun convertDateToLong(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun convertLongToDate(time: Long): Date {
        return Date(time)
    }
}