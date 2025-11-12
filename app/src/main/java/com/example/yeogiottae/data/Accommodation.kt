package com.example.yeogiottae.data

data class Accommodation(
    val id: String,
    val name: String,
    val address: String,
    val distanceMeters: Double,
    val pricePerNight: Int,
    val thumbnailUrl: String? = null
)
