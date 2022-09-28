package com.hig.autocrypt.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*


@Entity(tableName = "public_health_tb")
data class PublicHealth(
    val address: String,
    val centerName: String,
    val centerType: String,
    val createdAt: Date,
    val facilityName: String,
    @PrimaryKey
    val id: Double,
    val lat: Double,
    val lng: Double,
    val org: String,
    val phoneNumber: String,
    val sido: String,
    val sigungu: String,
    val updatedAt: Date,
    val zipCode: String
): Serializable