package com.example.wheretogo.data.remote.dto

data class RegisterRequest(
    val name: String,
    val phonenumber: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)