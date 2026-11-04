
package com.example.wheretogo.network

import com.example.wheretogo.model.LoginResponse
import com.example.wheretogo.model.PlaceItem
import com.example.wheretogo.model.ProfileResponse
import com.example.wheretogo.model.ProfileUpdateRequest
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

    @GET("api/places/nearby?latitude=55.75396&longitude=37.620393&radius=2000")
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
}
