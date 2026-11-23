package com.example.wheretogo.test

import android.util.Patterns
import android.util.Log
import com.example.wheretogo.ui.auth.validation.RegistrationForm
import kotlin.system.measureNanoTime

data class RegistrationValidationResult(
    val loginError: String?,
    val phoneError: String?,
    val emailError: String?,
    val passwordError: String?,
    val confirmPasswordError: String?
)

object RegistrationValidationExperiment {

    private const val TAG = "REGISTRATION_P5"
    private const val WARMUP_RUNS = 2
    private const val MEASURE_RUNS = 5
    private const val ITERATIONS = 10_000

    // Регулярные выражения альтернативной реализации
    // создаются один раз, а не при каждой проверке пароля.
    private val uppercaseRegex = Regex("""[A-ZА-ЯЁ]""")
    private val lowercaseRegex = Regex("""[a-zа-яё]""")
    private val digitRegex = Regex("""\d""")
    private val specialRegex =
        Regex("""[!@#$%^&*()_+\-=\[\]{};':",.<>?/\\|~`]""")

    private val testForms = listOf(
        RegistrationForm(
            "Alex", "89991234567", "alex@example.com",
            "Password1!", "Password1!"
        ),
        RegistrationForm(
            "", "89991234567", "alex@example.com",
            "Password1!", "Password1!"
        ),
        RegistrationForm(
            "Alexander", "8999123456", "alex@example.com",
            "Password1!", "Password1!"
        ),
        RegistrationForm(
            "Maria", "89991234567", "invalid-email",
            "Password1!", "Password1!"
        ),
        RegistrationForm(
            "Ivan", "89991234567", "ivan@example.com",
            "password123!", "password123!"
        ),
        RegistrationForm(
            "Elena", "89991234567", "elena@example.com",
            "Password1!", "Password2!"
        )
    )

    // Базовая реализация.
    // Повторяет проверки из RegisterViewModel.register().
    private fun validateBaseline(
        form: RegistrationForm
    ): RegistrationValidationResult {

        val name = form.name
        val phone = form.phone
        val email = form.email
        val password = form.password
        val confirmPassword = form.confirmPassword

        val loginError = when {
            name.isBlank() -> "Введите имя"
            name.length > 20 -> "Имя слишком длинное"
            name.length < 3 -> "Имя слишком короткое"
            name.any { it.isDigit() } ->
                "Имя не может содержать цифры"
            else -> null
        }

        val phoneError = when {
            phone.isBlank() -> "Введите номер телефона"
            phone.length != 11 -> "Некорректный номер телефона"
            !Patterns.PHONE.matcher(phone.trim()).matches() ->
                "Некорректно указан номер"
            else -> null
        }

        val emailError = when {
            email.isBlank() -> "Введите почту"
            email.length > 150 -> "Почта слишком длинная"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Некорректная почта"
            else -> null
        }

        val passwordError = when {
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

        val confirmPasswordError = when {
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }

        return RegistrationValidationResult(
            loginError,
            phoneError,
            emailError,
            passwordError,
            confirmPasswordError
        )
    }

    // Альтернативная реализация.
    // Правила и порядок проверок сохранены,
    // регулярные выражения заранее скомпилированы.
    private fun validateOptimized(
        form: RegistrationForm
    ): RegistrationValidationResult {

        val name = form.name
        val phone = form.phone
        val email = form.email
        val password = form.password
        val confirmPassword = form.confirmPassword

        val loginError = when {
            name.isBlank() -> "Введите имя"
            name.length > 20 -> "Имя слишком длинное"
            name.length < 3 -> "Имя слишком короткое"
            name.any { it.isDigit() } ->
                "Имя не может содержать цифры"
            else -> null
        }

        val phoneError = when {
            phone.isBlank() -> "Введите номер телефона"
            phone.length != 11 -> "Некорректный номер телефона"
            !Patterns.PHONE.matcher(phone.trim()).matches() ->
                "Некорректно указан номер"
            else -> null
        }

        val emailError = when {
            email.isBlank() -> "Введите почту"
            email.length > 150 -> "Почта слишком длинная"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Некорректная почта"
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < 8 -> "Пароль слишком короткий"
            password.length > 20 -> "Пароль слишком длинный"
            !uppercaseRegex.containsMatchIn(password) ->
                "Пароль должен содержать заглавную букву"
            !lowercaseRegex.containsMatchIn(password) ->
                "Пароль должен содержать строчную букву"
            !digitRegex.containsMatchIn(password) ->
                "Пароль должен содержать цифру"
            !specialRegex.containsMatchIn(password) ->
                "Пароль должен содержать спецсимвол"
            else -> null
        }

        val confirmPasswordError = when {
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }

        return RegistrationValidationResult(
            loginError,
            phoneError,
            emailError,
            passwordError,
            confirmPasswordError
        )
    }

    private fun measure(
        validator: (RegistrationForm) -> RegistrationValidationResult
    ): Pair<Long, Int> {
        var checksum = 0

        val time = measureNanoTime {
            repeat(ITERATIONS) {
                for (form in testForms) {
                    checksum += validator(form).hashCode()
                }
            }
        }

        return time to checksum
    }

    fun runExperiment() {
        Log.d(TAG, "--- P5: ЭКСПЕРИМЕНТ ВАЛИДАЦИИ РЕГИСТРАЦИИ ---")

        // Сначала проверяем, что обе реализации дают одинаковые результаты.
        for (form in testForms) {
            val baselineResult = validateBaseline(form)
            val optimizedResult = validateOptimized(form)

            check(baselineResult == optimizedResult) {
                "Результаты валидации отличаются для формы: $form"
            }
        }

        Log.d(TAG, "Проверка корректности: результаты совпадают")

        // Прогрев обеих реализаций.
        repeat(WARMUP_RUNS) {
            measure(::validateBaseline)
            measure(::validateOptimized)
        }

        val baselineTimes = mutableListOf<Long>()
        val optimizedTimes = mutableListOf<Long>()

        // Чередуем порядок измерений, чтобы уменьшить влияние порядка запуска.
        repeat(MEASURE_RUNS) { run ->
            val baseline: Pair<Long, Int>
            val optimized: Pair<Long, Int>

            if (run % 2 == 0) {
                baseline = measure(::validateBaseline)
                optimized = measure(::validateOptimized)
            } else {
                optimized = measure(::validateOptimized)
                baseline = measure(::validateBaseline)
            }

            check(baseline.second == optimized.second) {
                "Контрольные суммы результатов отличаются"
            }

            baselineTimes.add(baseline.first)
            optimizedTimes.add(optimized.first)

            Log.d(
                TAG,
                "Запуск ${run + 1}: " +
                        "baseline=${baseline.first / 1_000_000.0} мс, " +
                        "optimized=${optimized.first / 1_000_000.0} мс"
            )
        }

        fun median(values: List<Long>): Long =
            values.sorted()[values.size / 2]

        val baselineMedian = median(baselineTimes)
        val optimizedMedian = median(optimizedTimes)

        val speedup = baselineMedian.toDouble() / optimizedMedian

        Log.d(
            TAG,
            "Базовая реализация: медиана = " +
                    "${baselineMedian / 1_000_000.0} мс"
        )

        Log.d(
            TAG,
            "Альтернативная реализация: медиана = " +
                    "${optimizedMedian / 1_000_000.0} мс"
        )

        Log.d(TAG, "Ускорение: ${"%.3f".format(speedup)}x")
        Log.d(TAG, "--- P5: ЭКСПЕРИМЕНТ ЗАВЕРШЁН ---")
    }
}