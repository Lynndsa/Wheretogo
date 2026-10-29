
package com.example.wheretogo.model

import org.json.JSONObject
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

    var loginError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun login(
        login: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        loginError = if (login.isBlank()) "Введите имя" else null
        passwordError = if (password.isBlank()) "Введите пароль" else null
        error = null

        if (loginError != null || passwordError != null) {
            return
        }

        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(login.trim(), password)
                )

                when {
                    response.isSuccessful -> onSuccess()
                    else -> {
                        val responseBody = response.errorBody()?.string()

                        val code = try {
                            JSONObject(responseBody ?: "{}").optString("code")
                        } catch (e: Exception) {
                            ""
                        }

                        val message = try {
                            JSONObject(responseBody ?: "{}").optString("message")
                        } catch (e: Exception) {
                            ""
                        }

                        when (code) {
                            "LOGIN_NOT_EXIST" ->
                                loginError = message.ifBlank { "Пользователь с таким логином не найде" }

                            "PASSWORD_EXIST" ->
                                passwordError = message.ifBlank {
                                    "Пароль указан не верно"
                                }

                            else ->
                                error = message.ifBlank {
                                    "Ошибка сервера (${response.code()})"
                                }

                        }


                    }
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

    fun onLoginChanged(value: String) {
        loginError = null
        error = null
    }

    fun onPasswordChanged(value: String) {
        passwordError = null
        error = null
    }
}
