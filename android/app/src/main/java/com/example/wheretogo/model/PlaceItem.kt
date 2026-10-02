package com.example.wheretogo.model

import org.maplibre.android.geometry.LatLng

data class PlaceItem(
    val id: Int,
    val title: String,
    val description: String,
    val latLng: LatLng
)
