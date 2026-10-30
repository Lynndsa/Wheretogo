package com.example.wheretogo.model

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.network.RegisterRequest
import com.example.wheretogo.network.RetrofitClient
import org.json.JSONObject
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.String

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
        loginError = when {
            (name.isBlank()) -> "Введите имя"
            (name.length > 20) -> "Имя слишком длинное"
            (name.length < 3) -> "Имя слишком короткое"
            (name.contains(Regex("""\d"""))) -> "Имя не может состоять из цифр"

            else -> null
        }
        phoneError = when {
            (phone.length != 11) -> "Некорректный номер телефона"
            (phone.isBlank()) -> "Введите номер телефона"
            !Patterns.PHONE.matcher(phone.trim()).matches() -> "Некорректно указан номер"
            (phone.contains(Regex("""\p{L}"""))) -> "Телефон не может состоять из букв"
            else -> null
        }
        emailError = when {
            email.isBlank() -> "Введите почту"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Некорректная почта"
            email.length > 150 -> "Почта слишком длинная"
            else -> null
        }
        passwordError = when{
            password.length < 8 -> "Пароль слишком короткий"
            password.length > 20 -> "Пароль слишком длинный"
            (password.isBlank()) -> "Введите пароль"
            (!password.contains(Regex("""[A-ZА-ЯЁ]"""))) -> "Пароль должен содержать заглавную букву"
            (!password.contains(Regex("""[a-zа-яё]"""))) -> "Пароль должен содержать строчную букву"
            (!password.contains(Regex("""\d"""))) -> "Пароль должен содержать цифру"
            (!password.contains(Regex("""[!@#$%^&*()_+\-=\[\]{};':",.<>?/\\|~`]"""))) -> "Пароль должен содержать спецсимвол"
            else -> null
        }
        confirmPasswordError =
            if (confirmPassword.isBlank()) "Подтвердите пароль" else null

        error = null

        if (password.isNotBlank() && confirmPassword.isNotBlank()
            && password != confirmPassword
        ) {
            confirmPasswordError = "Пароли не совпадают"
        }

        if (
            loginError != null ||
            phoneError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) return

        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(name, phone, email, password, confirmPassword)
                )

                when {
                    response.isSuccessful -> onSuccess()

                    else -> {
                        val responseBody = response.errorBody()?.string()

                        android.util.Log.e(
                            "REGISTER",
                            "HTTP ${response.code()}: $responseBody"
                        )


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
                            "LOGIN_ERROR" ->
                                loginError = message.ifBlank { "Это имя уже занято" }

                            "EMAIL_ERROR" ->
                                emailError = message.ifBlank {
                                    "Эта почта уже зарегистрирована"
                                }

                            "PHONE_ERROR" ->
                                phoneError = message.ifBlank {
                                    "Этот номер уже зарегистрирован"
                                }

                            "PASSWORD_ERROR" ->
                                confirmPasswordError = message.ifBlank {
                                    "Пароли не совпадают"
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
    fun onPhoneChanged(value: String) {
        phoneError = null
        error = null
    }

    fun oEmailChanged(value: String) {
        emailError = null
        error = null
    }

    fun onPasswordConfirmChanged(value: String) {
        confirmPasswordError = null
        error = null
    }
}

