package com.example.wheretogo.ui.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.data.remote.RetrofitClient
import com.example.wheretogo.data.remote.dto.LoginRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

/**
 * ViewModel экрана входа ([LoginWindow] в LoginActivity.kt).
 *
 * ## Что делает
 * 1. Локально проверяет, что имя и пароль заполнены.
 * 2. Отправляет запрос на вход на сервер через Retrofit (RetrofitClient.apiService.login).
 * 3. Разбирает ответ: успех — вызывает колбэк; ошибка — превращает её в текст и кладёт
 *    в нужное поле состояния ([loginError], [passwordError] или общее [error]).
 *
 * ## Как экран получает данные
 * Все поля ниже — Compose-состояния (mutableStateOf). Экран читает их напрямую
 * (viewModel.loginError и т.д.), и при любом изменении Compose сам перерисовывает UI.
 * `private set` означает, что менять их может только эта ViewModel, а экран только читает.
 *
 * ## Связанные файлы
 * - LoginActivity.kt — экран, использующий эту ViewModel.
 * - RetrofitClient / LoginRequest (пакет data.remote) — сеть и тело запроса;
 * - validation/LoginValidator.kt — готовый валидатор формы входа. Сейчас НЕ используется:
 *   проверка в [login] написана вручную (и немного отличается: для пароля здесь
 *   isBlank(), в валидаторе isEmpty()).
 */
class LoginViewModel : ViewModel() {

    /** true, пока выполняется запрос. Экран по нему блокирует кнопку и показывает индикатор. */
    var isLoading by mutableStateOf(false)
        private set

    /** Ошибка поля «Имя»: локальная («Введите имя») или от сервера (LOGIN_NOT_EXIST). */
    var loginError by mutableStateOf<String?>(null)
        private set

    /** Ошибка поля «Пароль»: локальная («Введите пароль») или от сервера (PASSWORD_EXIST). */
    var passwordError by mutableStateOf<String?>(null)
        private set

    /** Общая ошибка, не относящаяся к конкретному полю (сеть, ошибка сервера, пустой ответ). */
    var error by mutableStateOf<String?>(null)
        private set


    /**
     * Основной сценарий входа. Вызывается экраном по кнопке «Войти».
     *
     * @param login    значение поля «Имя» (именуется login, потому что сервер называет
     *                 это поле логином)
     * @param password введённый пароль
     * @param onSuccess колбэк успеха: (idUser, login) — id и логин из ответа сервера.
     *                  Экран пробрасывает их дальше в onLoginSuccess.
     */
    fun login(
        login: String,
        password: String,
        onSuccess: (Long, String) -> Unit
    ) {

        // Локальная проверка полей
        // Имя: пустое или только пробелы — ошибка.
        loginError = if (login.isBlank()) {
            "Введите имя"
        } else {
            null
        }

        // Пароль: здесь isBlank(), т.е. пароль из одних пробелов тоже считается пустым.
        passwordError = if (password.isBlank()) {
            "Введите пароль"
        } else {
            null
        }

        // Сбрасываем общую ошибку от прошлой попытки.
        error = null

        // Есть локальные ошибки — на сервер не идём, экран покажет тексты под полями.
        if (loginError != null || passwordError != null) {
            return
        }

        // Запрос выполняется в корутине, привязанной к жизни ViewModel: если ViewModel
        // уничтожена, запрос будет отменён автоматически.
        viewModelScope.launch {
            isLoading = true

            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(
                        // Имя обрезается по краям; пароль отправляется как есть.
                        login.trim(),
                        password
                    )
                )

                if (response.isSuccessful) {
                    // HTTP 2xx. Тело ответа может быть null — обрабатываем и этот случай.
                    val result = response.body()

                    if (result != null) {
                        // Вход выполнен: отдаём id и логин наружу (в LoginWindow -> навигация).
                        onSuccess(result.idUser, result.login)
                    } else {
                        error = "Сервер вернул пустой ответ"
                    }
                } else {
                    // HTTP не 2xx. Сервер присылает JSON с полями code и message.

                    // Читаем тело ответа только один раз
                    // (errorBody().string() вычитывает поток, повторно его прочитать нельзя).
                    val responseBody =
                        response.errorBody()?.string().orEmpty()

                    Log.e(
                        "LOGIN_ERROR",
                        "HTTP ${response.code()}: $responseBody"
                    )

                    // Если тело не JSON или пустое — берём пустой объект, ниже сработают значения по умолчанию.
                    val json = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        JSONObject()
                    }

                    val code = json.optString("code")       // машинный код ошибки
                    val message = json.optString("message") // текст от сервера (может быть пустым)

                    // По коду выбираем, к какому полю привязать ошибку.
                    // message.ifBlank { ... } — если сервер не прислал текст, берём свой запасной.
                    when (code) {

                        "LOGIN_NOT_EXIST" -> {
                            // Такого пользователя нет -> ошибка под полем имени.
                            loginError = message.ifBlank {
                                "Пользователь с таким логином не найден"
                            }
                        }

                        "PASSWORD_EXIST" -> {
                            // Судя по сообщению, пароль неверный -> ошибка под полем пароля.
                            // (Название кода выглядит как «пароль существует», но здесь
                            // трактуется как «пароль неверный».)
                            passwordError = message.ifBlank {
                                "Неверный пароль"
                            }
                        }

                        else -> {
                            // Любой другой код -> общая ошибка внизу формы.
                            error = message.ifBlank {
                                "Ошибка сервера (${response.code()})"
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                // Сетевая проблема (нет интернета, таймаут, сервер недоступен).
                // К тексту дописывается e.message — удобно при отладке, но пользователю
                // техническое сообщение лучше не показывать (его стоит убрать из релиза).
                error = "Нет соединения с сервером" + e.message

            } catch (e: Exception) {
                // Прочие неожиданные ошибки: логируем со стеком, пользователю — общий текст.
                Log.e(
                    "LOGIN_ERROR",
                    "Ошибка входа",
                    e
                )
                error = "Что-то пошло не так"

            } finally {
                // Выполняется всегда: и после успеха, и после ошибки — снимаем индикатор загрузки.
                isLoading = false
            }
        }
    }


    /**
     * Вызывается экраном при каждом вводе в поле «Имя».
     * Прячет ошибку имени и общую ошибку, чтобы старый текст не висел после правки.
     * Параметр [value] сейчас не используется (оставлен «на будущее», например для
     * проверки на лету).
     */
    fun onLoginChanged(value: String) {
        loginError = null
        error = null
    }

    /** То же для поля «Пароль»: сбрасывает ошибку пароля и общую ошибку. */
    fun onPasswordChanged(value: String) {
        passwordError = null
        error = null
    }
}