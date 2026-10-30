package com.example.wheretogo.network

data class RegisterRequest(
    val name: String,
    val phonenumber: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)