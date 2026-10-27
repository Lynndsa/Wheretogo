package com.example.wheretogo.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/test")
    suspend fun sendTestData(@Body request: TestRequest): Response<TestResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<String>
}