package com.example.wheretogo.network

import com.example.wheretogo.model.PlaceItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/test")
    suspend fun sendTestData(@Body request: TestRequest): Response<TestResponse>

    @GET("api/places/nearby?latitude=55.75396&longitude=37.620393&radius=2000")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int
    ): Response<List<PlaceItem>>

    @GET("api/v1/custom-places")
    suspend fun getAllPlaceDB(): Response<List<PlaceItem>>
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<String>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<String>
}
