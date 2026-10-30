
package com.example.wheretogo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.network.LoginRequest
import com.example.wheretogo.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
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

        // Локальная проверка полей
        loginError = if (login.isBlank()) {
            "Введите имя"
        } else {
            null
        }

        passwordError = if (password.isBlank()) {
            "Введите пароль"
        } else {
            null
        }

        error = null

        if (loginError != null || passwordError != null) {
            return
        }

        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        login.trim(),
                        password
                    )
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {

                    // Читаем тело ответа только один раз
                    val responseBody =
                        response.errorBody()?.string().orEmpty()

                    android.util.Log.e(
                        "LOGIN_ERROR",
                        "HTTP ${response.code()}: $responseBody"
                    )

                    val json = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        JSONObject()
                    }

                    val code = json.optString("code")
                    val message = json.optString("message")

                    when (code) {

                        "LOGIN_NOT_EXIST" -> {
                            loginError = message.ifBlank {
                                "Пользователь с таким логином не найден"
                            }
                        }

                        "PASSWORD_EXIST" -> {
                            passwordError = message.ifBlank {
                                "Неверный пароль"
                            }
                        }

                        else -> {
                            error = message.ifBlank {
                                "Ошибка сервера (${response.code()})"
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                error = "Нет соединения с сервером"

            } catch (e: Exception) {
                android.util.Log.e(
                    "LOGIN_ERROR",
                    "Ошибка входа",
                    e
                )
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
