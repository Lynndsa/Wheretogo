package com.example.wheretogo.validation

import com.example.wheretogo.validation.FieldError.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ожидаемые значения взяты из правил R1–R5 (см. KDoc RegistrationValidator), а не из запуска кода. */
class RegistrationValidatorTest {

    private fun check(expected: FieldError?, vararg inputs: String, f: (String) -> FieldError?) =
        inputs.forEach { assertEquals("вход: '$it'", expected, f(it)) }

    // ---- R1 Имя: классы эквивалентности + границы 2 и 50 ----
    @Test fun `имя - пустое или из пробелов`() =
        check(EMPTY, "", "   ", f = RegistrationValidator::validateName)

    @Test fun `имя - короче минимума`() =
        check(TOO_SHORT, "А", f = RegistrationValidator::validateName)

    @Test fun `имя - граница минимума и штатное`() =
        check(null, "Ан", "Анна", f = RegistrationValidator::validateName)

    @Test fun `имя - пробелы по краям обрезаются перед проверкой длины`() =
        check(null, " Ан ", f = RegistrationValidator::validateName)

    @Test fun `имя - граница максимума 50 допустима, 51 - ошибка`() {
        assertEquals(null, RegistrationValidator.validateName("а".repeat(50)))
        assertEquals(TOO_LONG, RegistrationValidator.validateName("а".repeat(51)))
    }

    // ---- R2 Телефон ----
    @Test fun `телефон - пустой`() = check(EMPTY, "", "  ", f = RegistrationValidator::validatePhone)

    @Test fun `телефон - допустимые форматы`() = check(
        null, "+79991234567", "89991234567", "+7 (999) 123-45-67",
        f = RegistrationValidator::validatePhone
    )

    @Test fun `телефон - граница по количеству цифр`() = check(
        INVALID_FORMAT, "+7999123456", "+799912345678",
        f = RegistrationValidator::validatePhone
    )

    @Test fun `телефон - неверный префикс или символы`() = check(
        INVALID_FORMAT, "79991234567", "+7999123456a", "+19991234567",
        f = RegistrationValidator::validatePhone
    )

    // ---- R3 Почта ----
    @Test fun `почта - пустая`() = check(EMPTY, "", "   ", f = RegistrationValidator::validateEmail)

    @Test fun `почта - корректная, пробелы по краям игнорируются`() = check(
        null, "user@mail.ru", " user@mail.ru ", "a.b+c@sub.domain.com",
        f = RegistrationValidator::validateEmail
    )

    @Test fun `почта - некорректный формат`() = check(
        INVALID_FORMAT, "usermail.ru", "user@mail", "@mail.ru", "user@mail.r", "user@@mail.ru",
        f = RegistrationValidator::validateEmail
    )

    // ---- R4 Пароль: граница длины 7/8, состав ----
    @Test fun `пароль - пустой`() = check(EMPTY, "", f = RegistrationValidator::validatePassword)

    @Test fun `пароль - 7 символов слишком короткий даже если состав верный`() =
        check(TOO_SHORT, "abc1234", f = RegistrationValidator::validatePassword)

    @Test fun `пароль - 8 символов с буквой и цифрой допустим`() =
        check(null, "abcd1234", "abcdefg1", f = RegistrationValidator::validatePassword)

    @Test fun `пароль - только буквы или только цифры слабый`() = check(
        WEAK_PASSWORD, "abcdefgh", "12345678",
        f = RegistrationValidator::validatePassword
    )

    // ---- R5 Подтверждение ----
    @Test fun `подтверждение - пустое`() =
        assertEquals(EMPTY, RegistrationValidator.validateConfirm("abcd1234", ""))

    @Test fun `подтверждение - совпадает`() =
        assertEquals(null, RegistrationValidator.validateConfirm("abcd1234", "abcd1234"))

    @Test fun `подтверждение - не совпадает, регистр учитывается`() {
        assertEquals(MISMATCH, RegistrationValidator.validateConfirm("abcd1234", "abcd1235"))
        assertEquals(MISMATCH, RegistrationValidator.validateConfirm("abcd1234", "Abcd1234"))
    }

    // ---- Таблица решений: форма целиком ----
    private val valid = RegistrationForm(
        name = "Анна", phone = "+79991234567", email = "anna@mail.ru",
        password = "abcd1234", confirmPassword = "abcd1234"
    )

    @Test fun `форма - всё корректно`() {
        val r = RegistrationValidator.validate(valid)
        assertTrue(r.isValid)
        assertEquals(RegistrationErrors(), r)
    }

    @Test fun `форма - ровно одно неверное поле даёт ровно одну ошибку`() {
        val cases = listOf(
            valid.copy(name = "") to RegistrationErrors(name = EMPTY),
            valid.copy(phone = "123") to RegistrationErrors(phone = INVALID_FORMAT),
            valid.copy(email = "x") to RegistrationErrors(email = INVALID_FORMAT),
            valid.copy(password = "abc", confirmPassword = "abc") to
                RegistrationErrors(password = TOO_SHORT),
            valid.copy(confirmPassword = "zzzz1111") to RegistrationErrors(confirmPassword = MISMATCH)
        )
        cases.forEach { (form, expected) ->
            val r = RegistrationValidator.validate(form)
            assertEquals("форма: $form", expected, r)
            assertFalse(r.isValid)
        }
    }

    @Test fun `форма - все поля пустые, ошибка у каждого`() {
        val r = RegistrationValidator.validate(RegistrationForm("", "", "", "", ""))
        assertEquals(RegistrationErrors(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY), r)
    }
}
