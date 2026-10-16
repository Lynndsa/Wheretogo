package com.example.wheretogo.network

data class TestRequest(
    val title : String,
    val message: String
)

data class TestResponse(
    val success: Boolean,
    val responseMessage: String
)