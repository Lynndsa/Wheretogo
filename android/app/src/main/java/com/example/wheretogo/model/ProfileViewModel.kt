
package com.example.wheretogo.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.network.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    var profile by mutableStateOf<ProfileResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Получение профиля по ID из сохранённой сессии
    fun loadProfile(context: Context) {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val idUser = SessionManager.getUserId(context).first()
                    ?: throw Exception("ID пользователя не найден. Войдите заново.")

                val response = RetrofitClient.apiService.getProfile(idUser)

                if (response.isSuccessful) {
                    profile = response.body()

                    if (profile == null) {
                        error = "Сервер не вернул данные профиля"
                    }
                } else {
                    error = "Не удалось загрузить профиль: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Ошибка загрузки профиля", e)
                error = e.message ?: "Ошибка подключения к серверу"
            } finally {
                isLoading = false
            }
        }
    }

    // Обновление профиля по ID из сохранённой сессии
    fun updateProfile(
        context: Context,
        request: ProfileUpdateRequest,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val idUser = SessionManager.getUserId(context).first()
                    ?: throw Exception("ID пользователя не найден. Войдите заново.")

                val response = RetrofitClient.apiService.updateProfile(
                    idUser = idUser,
                    request = request
                )

                if (response.isSuccessful) {
                    val updatedProfile = response.body()

                    if (updatedProfile != null) {
                        profile = updatedProfile

                        // Обновляем логин в сессии, ID остаётся прежним
                        SessionManager.saveSession(
                            context,
                            idUser,
                            updatedProfile.login
                        )

                        onSuccess()
                    } else {
                        error = "Сервер не вернул обновлённый профиль"
                    }
                } else {
                    error = "Ошибка обновления: HTTP ${response.code()} — ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Ошибка обновления профиля", e)
                error = e.message ?: "Ошибка подключения к серверу"
            } finally {
                isLoading = false
            }
        }
    }
}
