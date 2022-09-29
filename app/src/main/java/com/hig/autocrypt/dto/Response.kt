package com.hig.autocrypt.dto

import java.io.Serializable

data class Response(
    val currentCnt: Int,
    val data: List<PublicHealth>,
    val matchCount: Int,
    val page: Int,
    val totalCount: Int
): Serializable
