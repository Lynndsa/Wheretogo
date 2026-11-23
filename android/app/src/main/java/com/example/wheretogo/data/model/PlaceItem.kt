package com.example.wheretogo.data.model

import com.google.gson.annotations.SerializedName

data class PlaceItem(
    @SerializedName(value = "id_custom_place")
    val id: Long,
    @SerializedName(value = "title", alternate = ["name"])
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