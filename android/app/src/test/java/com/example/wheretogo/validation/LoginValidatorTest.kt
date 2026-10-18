package com.example.wheretogo.validation

import com.example.wheretogo.validation.FieldError.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Таблица решений для правил L1 и L2. */
class LoginValidatorTest {
    @Test fun `имя и пароль заполненыd`() =
        assertTrue(LoginValidator.validate("Иван", "pass").isValid)

    @Test fun `пустое имя`() =
        assertEquals(LoginErrors(name = EMPTY), LoginValidator.validate("", "pass"))

    @Test fun `имя из пробелов считается пустым`() =
        assertEquals(LoginErrors(name = EMPTY), LoginValidator.validate("   ", "pass"))

    @Test fun `пустой пароль`() =
        assertEquals(LoginErrors(password = EMPTY), LoginValidator.validate("Иван", ""))

    @Test fun `пароль из пробелов допустим, пароль не обрезается`() =
        assertTrue(LoginValidator.validate("Иван", "   ").isValid)

    @Test fun `оба поля пустые`() =
        assertEquals(LoginErrors(EMPTY, EMPTY), LoginValidator.validate("", ""))
}
