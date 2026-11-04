package com.example.wheretogo.model

data class ProfileUpdateRequest(
    val login: String,
    val name: String,
    val lastname: String?,
    val surname: String?,
    val phonenumber: String,
    val email: String
)