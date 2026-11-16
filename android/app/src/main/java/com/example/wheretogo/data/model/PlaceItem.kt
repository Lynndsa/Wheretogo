package com.example.wheretogo.data.model

data class PlaceItem(
    val title: String?,
    val description: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val category: String?,
    val imageUrl: String?,
    val websiteUrl: String?,
    val wikipediaUrl: String?
)