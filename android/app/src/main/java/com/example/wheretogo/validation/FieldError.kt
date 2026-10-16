package com.example.wheretogo.validation

/** Причина, по которой значение поля формы не принято. null вместо ошибки означает «значение корректно». */
enum class FieldError { EMPTY, TOO_SHORT, TOO_LONG, INVALID_FORMAT, WEAK_PASSWORD, MISMATCH }
