package com.example.wheretogo.data.remote

import com.example.wheretogo.data.remote.dto.LoginResponse
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.dto.ProfileResponse
import com.example.wheretogo.data.remote.dto.ProfileUpdateRequest
import com.example.wheretogo.data.remote.dto.LoginRequest
import com.example.wheretogo.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/test")
    suspend fun sendTestData(@Body request: TestRequest): Response<TestResponse>

    @GET("api/places/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int
    ): Response<List<PlaceItem>>

    @GET("api/v1/custom-places")
    suspend fun getAllPlaceDB(): Response<List<PlaceItem>>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    // Получение профиля по ID
    @GET("api/profile/{idUser}")
    suspend fun getProfile(
        @Path("idUser") idUser: Long
    ): Response<ProfileResponse>

    // Обновление профиля по ID
    @PUT("api/profile/{idUser}")
    suspend fun updateProfile(
        @Path("idUser") idUser: Long,
        @Body request: ProfileUpdateRequest
    ): Response<ProfileResponse>

    @GET("api/places/search")
    suspend fun searchPlaces(
        @Query("query") query: String
    ): Response<List<PlaceItem>>
}