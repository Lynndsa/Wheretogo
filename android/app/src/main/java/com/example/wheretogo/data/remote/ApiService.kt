package com.example.wheretogo.data.remote

import com.example.wheretogo.data.remote.dto.LoginResponse
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.dto.ProfileResponse
import com.example.wheretogo.data.remote.dto.ProfileUpdateRequest
import com.example.wheretogo.data.remote.dto.LoginRequest
import com.example.wheretogo.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/test")
    suspend fun sendTestData(@Body request: TestRequest): Response<TestResponse>


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

    // Получение всех достопримечательностей из MySQL
    @GET("api/v1/custom-places")
    suspend fun getDataBase(): Response<List<PlaceItem>>

    // Получение подробностей достопримечательности по ID
    @GET("api/v1/custom-places/{id}")
    suspend fun getPlaceDetails(
        @Path("id") id: Long
    ): Response<PlaceItem>

    @GET("api/favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: Long): Response<List<Long>>

    @POST("api/favorites/{userId}/{attractionId}")
    suspend fun addFavorite(
        @Path("userId") userId: Long,
        @Path("attractionId") attractionId: Long
    ): Response<Unit>

    @DELETE("api/favorites/{userId}/{attractionId}")
    suspend fun removeFavorite(
        @Path("userId") userId: Long,
        @Path("attractionId") attractionId: Long
    ): Response<Unit>
}
