package com.example.wheretogo.validation

data class RegistrationForm(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class RegistrationErrors(
    val name: FieldError? = null,
    val phone: FieldError? = null,
    val email: FieldError? = null,
    val password: FieldError? = null,
    val confirmPassword: FieldError? = null
) {
    val isValid: Boolean
        get() = name == null && phone == null && email == null &&
            password == null && confirmPassword == null
}

/**
 * Правила регистрации (ЗАПИШИТЕ их как требования в задаче/MR и подправьте под свои):
 *
 * R1 Имя: после обрезки пробелов по краям длина 2..50, пустое — EMPTY.
 * R2 Телефон: пробелы, дефисы и скобки игнорируются; остаётся +7 или 8 и ровно 10 цифр.
 * R3 Почта: вид «локальная_часть@домен.зона», зона не короче 2 букв; пробелы по краям обрезаются.
 * R4 Пароль: не короче 8 символов и содержит минимум одну букву и одну цифру. Пароль не обрезается.
 * R5 Подтверждение: совпадает с паролем с учётом регистра.
 */
object RegistrationValidator {
    const val NAME_MIN = 2
    const val NAME_MAX = 50
    const val PASSWORD_MIN = 8

    private val phoneRegex = Regex("^(\\+7|8)\\d{10}$")
    private val emailRegex =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun validateName(name: String): FieldError? {
        val n = name.trim()
        return when {
            n.isEmpty() -> FieldError.EMPTY
            n.length < NAME_MIN -> FieldError.TOO_SHORT
            n.length > NAME_MAX -> FieldError.TOO_LONG
            else -> null
        }
    }

    fun validatePhone(phone: String): FieldError? {
        if (phone.isBlank()) return FieldError.EMPTY
        val cleaned = phone.filterNot { it == ' ' || it == '-' || it == '(' || it == ')' }
        return if (phoneRegex.matches(cleaned)) null else FieldError.INVALID_FORMAT
    }

    fun validateEmail(email: String): FieldError? {
        val e = email.trim()
        return when {
            e.isEmpty() -> FieldError.EMPTY
            !emailRegex.matches(e) -> FieldError.INVALID_FORMAT
            else -> null
        }
    }

    fun validatePassword(password: String): FieldError? = when {
        password.isEmpty() -> FieldError.EMPTY
        password.length < PASSWORD_MIN -> FieldError.TOO_SHORT
        !password.any { it.isLetter() } || !password.any { it.isDigit() } -> FieldError.WEAK_PASSWORD
        else -> null
    }

    fun validateConfirm(password: String, confirm: String): FieldError? = when {
        confirm.isEmpty() -> FieldError.EMPTY
        password != confirm -> FieldError.MISMATCH
        else -> null
    }

    fun validate(form: RegistrationForm) = RegistrationErrors(
        name = validateName(form.name),
        phone = validatePhone(form.phone),
        email = validateEmail(form.email),
        password = validatePassword(form.password),
        confirmPassword = validateConfirm(form.password, form.confirmPassword)
    )
}
