package com.example.wheretogo.ui.auth

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.data.remote.RetrofitClient
import com.example.wheretogo.data.remote.dto.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

/**
 * ViewModel экрана регистрации ([RegistrationWindow] в RegistrationActivity.kt).
 *
 * ## Что делает
 * 1. Локально проверяет все пять полей формы и записывает тексты ошибок в состояния.
 * 2. Если ошибок нет — отправляет запрос регистрации на сервер (RetrofitClient.apiService.register).
 * 3. Разбирает ответ: успех — вызывает колбэк; ошибка — раскладывает по полям по коду
 *    ошибки сервера.
 *
 * ## Как работает связь с экраном
 * Поля ошибок — Compose-состояния (mutableStateOf с `private set`): экран их читает и
 * автоматически перерисовывается, а менять их может только ViewModel.
 * Для каждого поля формы есть свой метод onXxxChanged, который экран вызывает при вводе
 * и который прячет устаревшую ошибку.
 *
 * ## Связанные файлы
 * - RegistrationActivity.kt — экран.
 * - LoginViewModel.kt — аналогичная ViewModel для входа (та же схема работы).
 * - RetrofitClient / RegisterRequest (пакет data.remote) — сеть и тело запроса (в
 *   загруженных файлах их нет).
 * - validation/RegistrationValidator.kt — готовый валидатор с ДРУГИМИ правилами. Сейчас не
 *   используется: проверка в [register] написана вручную и строже (см. комментарии ниже).
 */
class RegisterViewModel : ViewModel() {

    /** true, пока идёт запрос; экран по нему показывает индикатор вместо текста на кнопке. */
    var isLoading by mutableStateOf(false)
        private set

    /** Общая ошибка (не привязана к полю): VALIDATION_ERROR, сеть, неизвестный код. */
    var error by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Имя». Называется loginError, т.к. сервер считает имя логином. */
    var loginError by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Номер телефона». */
    var phoneError by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Почта». */
    var emailError by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Пароль». */
    var passwordError by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Повторите пароль». */
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set


    /**
     * Основной сценарий регистрации. Вызывается экраном по кнопке «Зарегистрироваться».
     *
     * @param name            имя (оно же логин)
     * @param phone           телефон как введён
     * @param email           почта
     * @param password        пароль
     * @param confirmPassword повтор пароля
     * @param onSuccess       колбэк успеха: (idUser, login) из ответа сервера; экран
     *                        пробрасывает их в onRegisterSuccess
     */
    fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (Long, String) -> Unit
    ) {

        // ---------- Локальная проверка. Каждое поле проверяется независимо ----------
        // Внутри when ветки проверяются сверху вниз, срабатывает первая подходящая.

        // Проверка имени: 3..20 символов, без цифр. Длина считается БЕЗ trim (пробелы учитываются).
        loginError = when {
            name.isBlank() -> "Введите имя"
            name.length > 20 -> "Имя слишком длинное"
            name.length < 3 -> "Имя слишком короткое"
            name.any { it.isDigit() } -> "Имя не может содержать цифры"
            else -> null
        }

        // Проверка телефона: ровно 11 символов и соответствие Android-шаблону Patterns.PHONE.
        // Следствие: форматы с пробелами, скобками и «+7» (12 символов) не пройдут.
        phoneError = when {
            phone.isBlank() -> "Введите номер телефона"
            phone.length != 11 -> "Некорректный номер телефона"
            !Patterns.PHONE.matcher(phone.trim()).matches() ->
                "Некорректно указан номер"
            else -> null
        }

        // Проверка почты: до 150 символов и соответствие стандартному шаблону Android
        // (Patterns.EMAIL_ADDRESS).
        emailError = when {
            email.isBlank() -> "Введите почту"
            email.length > 150 -> "Почта слишком длинная"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Некорректная почта"
            else -> null
        }

        // Проверка пароля: 8..20 символов; обязательны заглавная буква, строчная буква,
        // цифра и спецсимвол. Буквы проверяются и для латиницы, и для кириллицы.
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

        // Подтверждение пароля: не пустое и совпадает с паролем (с учётом регистра).
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }

        // Сбрасываем общую ошибку прошлой попытки.
        error = null

        // Если есть ошибки — запрос не отправляем; экран покажет тексты под полями.
        if (
            loginError != null ||
            phoneError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            return
        }

        // ---------- Запрос на сервер ----------
        // Корутина привязана к жизни ViewModel и отменяется вместе с ней.
        viewModelScope.launch {
            isLoading = true

            try {
                // Данные отправляются КАК ВВЕДЕНЫ (без trim и очистки телефона), включая
                // повтор пароля — сервер дополнительно сверяет их сам (см. PASSWORD_MISMATCH ниже).
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
                    // HTTP 2xx: регистрация прошла.
                    val result = response.body()

                    if (result != null) {
                        // Отдаём id и логин наружу (экран -> навигация).
                        onSuccess(result.idUser, result.login)
                    } else {
                        error = "Сервер вернул пустой ответ"
                    }
                } else {
                    // HTTP не 2xx. Сервер присылает JSON с полями code и message.

                    // Читаем тело ответа только один раз (повторно поток прочитать нельзя).
                    val responseBody = response.errorBody()?.string().orEmpty()

                    Log.e(
                        "REGISTER",
                        "HTTP ${response.code()}: $responseBody"
                    )

                    // Не JSON или пустое тело — берём пустой объект, сработают запасные тексты.
                    val json = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        JSONObject()
                    }

                    val code = json.optString("code")       // машинный код ошибки
                    val message = json.optString("message") // текст сервера (может быть пустым)

                    // По коду ошибки привязываем сообщение к нужному полю.
                    // message.ifBlank { ... } — если сервер текст не прислал, берём свой.
                    when (code) {

                        "LOGIN_ERROR",
                        "LOGIN_EXISTS" -> {
                            // Имя некорректно или уже занято -> под полем «Имя».
                            loginError = message.ifBlank {
                                "Это имя уже занято"
                            }
                        }

                        "EMAIL_ERROR",
                        "EMAIL_EXISTS" -> {
                            // Почта некорректна или уже зарегистрирована -> под полем «Почта».
                            emailError = message.ifBlank {
                                "Эта почта уже зарегистрирована"
                            }
                        }

                        "PHONE_ERROR",
                        "PHONE_EXISTS" -> {
                            // Телефон некорректен или уже зарегистрирован -> под полем «Номер телефона».
                            phoneError = message.ifBlank {
                                "Этот номер уже зарегистрирован"
                            }
                        }

                        "PASSWORD_ERROR",
                        "PASSWORD_MISMATCH" -> {
                            // Ошибка пароля показывается под полем подтверждения пароля.
                            confirmPasswordError = message.ifBlank {
                                "Пароли не совпадают"
                            }
                        }

                        "VALIDATION_ERROR" -> {
                            // Общая ошибка валидации на сервере -> общий текст внизу формы.
                            error = message.ifBlank {
                                "Проверьте правильность введённых данных"
                            }
                        }

                        else -> {
                            // Неизвестный код -> общая ошибка.
                            error = message.ifBlank {
                                "Ошибка сервера (${response.code()})"
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                // Сетевая проблема: нет интернета, таймаут, сервер недоступен.
                error = "Нет соединения с сервером"

            } catch (e: Exception) {
                // Прочие неожиданные ошибки: логируем со стеком, пользователю — общий текст.
                Log.e(
                    "REGISTER",
                    "Ошибка регистрации",
                    e
                )
                error = "Что-то пошло не так"

            } finally {
                // Всегда (успех или ошибка) снимаем индикатор загрузки.
                isLoading = false
            }
        }
    }


    // ---------- Обработчики ввода ----------
    // Экран (RegistrationActivity.kt) вызывает их из onValueChange каждого поля. Они прячут
    // ошибку этого поля и общую ошибку, чтобы старый текст не висел после правки.
    // Параметр value сейчас не используется (задел под проверку «на лету»).

    /** Ввод в поле «Имя». */
    fun onLoginChanged(value: String) {
        loginError = null
        error = null
    }

    /** Ввод в поле «Пароль». */
    fun onPasswordChanged(value: String) {
        passwordError = null
        error = null
    }

    /** Ввод в поле «Номер телефона». */
    fun onPhoneChanged(value: String) {
        phoneError = null
        error = null
    }

    /** Ввод в поле «Почта». */
    fun onEmailChanged(value: String) {
        emailError = null
        error = null
    }

    /** Ввод в поле «Повторите пароль». */
    fun onPasswordConfirmChanged(value: String) {
        confirmPasswordError = null
        error = null
    }
}