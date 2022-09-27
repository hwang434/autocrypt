package com.hig.autocrypt.dto

import java.io.Serializable

data class PublicHealth(
    val address: String,
    val centerName: String,
    val centerType: String,
    val createdAt: String,
    val facilityName: String,
    val id: Double,
    val lat: Double,
    val lng: Double,
    val org: String,
    val phoneNumber: String,
    val sido: String,
    val sigungu: String,
    val updatedAt: String,
    val zipCode: String
): Serializable