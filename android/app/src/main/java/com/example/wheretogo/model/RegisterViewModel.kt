package com.example.wheretogo.model

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.network.RegisterRequest
import com.example.wheretogo.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class RegisterViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    var phoneError by mutableStateOf<String?>(null)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set


    fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {

        // Проверка имени
        loginError = when {
            name.isBlank() -> "Введите имя"
            name.length > 20 -> "Имя слишком длинное"
            name.length < 3 -> "Имя слишком короткое"
            name.any { it.isDigit() } -> "Имя не может содержать цифры"
            else -> null
        }

        // Проверка телефона
        phoneError = when {
            phone.isBlank() -> "Введите номер телефона"
            phone.length != 11 -> "Некорректный номер телефона"
            !Patterns.PHONE.matcher(phone.trim()).matches() ->
                "Некорректно указан номер"
            else -> null
        }

        // Проверка почты
        emailError = when {
            email.isBlank() -> "Введите почту"
            email.length > 150 -> "Почта слишком длинная"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Некорректная почта"
            else -> null
        }

        // Проверка пароля
        passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < 8 -> "Пароль слишком короткий"
            password.length > 20 -> "Пароль слишком длинный"
            !password.contains(Regex("""[A-ZА-ЯЁ]""")) ->
                "Пароль должен содержать заглавную букву"
            !password.contains(Regex("""[a-zа-яё]""")) ->
                "Пароль должен содержать строчную букву"
            !password.contains(Regex("""\d""")) ->
                "Пароль должен содержать цифру"
            !password.contains(
                Regex("""[!@#$%^&*()_+\-=\[\]{};':",.<>?/\\|~`]""")
            ) -> "Пароль должен содержать спецсимвол"
            else -> null
        }

        // Подтверждение пароля
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }

        error = null

        // Если есть ошибки — запрос не отправляем
        if (
            loginError != null ||
            phoneError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            return
        }

        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        name,
                        phone,
                        email,
                        password,
                        confirmPassword
                    )
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {

                    // Читаем тело ответа только один раз
                    val responseBody = response.errorBody()?.string().orEmpty()

                    android.util.Log.e(
                        "REGISTER",
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

                        "LOGIN_ERROR",
                        "LOGIN_EXISTS" -> {
                            loginError = message.ifBlank {
                                "Это имя уже занято"
                            }
                        }

                        "EMAIL_ERROR",
                        "EMAIL_EXISTS" -> {
                            emailError = message.ifBlank {
                                "Эта почта уже зарегистрирована"
                            }
                        }

                        "PHONE_ERROR",
                        "PHONE_EXISTS" -> {
                            phoneError = message.ifBlank {
                                "Этот номер уже зарегистрирован"
                            }
                        }

                        "PASSWORD_ERROR",
                        "PASSWORD_MISMATCH" -> {
                            confirmPasswordError = message.ifBlank {
                                "Пароли не совпадают"
                            }
                        }

                        "VALIDATION_ERROR" -> {
                            error = message.ifBlank {
                                "Проверьте правильность введённых данных"
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
                    "REGISTER",
                    "Ошибка регистрации",
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

    fun onPhoneChanged(value: String) {
        phoneError = null
        error = null
    }

    fun onEmailChanged(value: String) {
        emailError = null
        error = null
    }

    fun onPasswordConfirmChanged(value: String) {
        confirmPasswordError = null
        error = null
    }
}