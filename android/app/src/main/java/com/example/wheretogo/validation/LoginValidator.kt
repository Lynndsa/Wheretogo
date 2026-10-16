package com.example.wheretogo.validation

data class LoginErrors(val name: FieldError? = null, val password: FieldError? = null) {
    val isValid: Boolean get() = name == null && password == null
}

/**
 * L1 Имя: не пустое и не состоит только из пробелов.
 * L2 Пароль: не пустой. Пароль не обрезается: строка из пробелов — допустимый ввод.
 */
object LoginValidator {
    fun validate(name: String, password: String) = LoginErrors(
        name = if (name.isBlank()) FieldError.EMPTY else null,
        password = if (password.isEmpty()) FieldError.EMPTY else null
    )
}
