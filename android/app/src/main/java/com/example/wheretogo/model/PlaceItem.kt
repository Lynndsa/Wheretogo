package com.example.wheretogo.model

import org.maplibre.android.geometry.LatLng

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
