package com.example.wheretogo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.network.LoginRequest
import com.example.wheretogo.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun login(login: String, password: String, onSuccess: () -> Unit) {
        if (login.isBlank() || password.isBlank()) {
            error = "Введите имя и пароль"
            return
        }
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(login.trim(), password))
                when {
                    response.isSuccessful -> onSuccess()
                    response.code() == 401 -> error = "Неверный логин или пароль"
                    else -> error = "Ошибка сервера (${response.code()})"
                }
            } catch (e: IOException) {
                error = "Нет соединения с сервером"
            } catch (e: Exception) {
                error = "Что-то пошло не так"
            } finally {
                isLoading = false
            }
        }
    }
}