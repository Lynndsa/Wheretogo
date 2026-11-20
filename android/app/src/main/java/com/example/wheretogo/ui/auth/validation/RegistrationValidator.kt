package com.example.wheretogo.ui.auth.validation

/**
 * Данные формы регистрации «как ввёл пользователь» (без какой-либо обработки).
 * Пять полей соответствуют пяти полям экрана регистрации
 * (RegistrationActivity.kt, функция RegistrationWindow) и параметрам метода
 * RegisterViewModel.register(...) (RegisterViewModel.kt).
 */
data class RegistrationForm(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

/**
 * Результат проверки формы регистрации: для каждого поля либо null (поле корректно),
 * либо причина ошибки [FieldError] (см. FieldError.kt).
 *
 * Аналог [LoginErrors] из LoginValidator.kt, но для пяти полей.
 */
data class RegistrationErrors(
    val name: FieldError? = null,
    val phone: FieldError? = null,
    val email: FieldError? = null,
    val password: FieldError? = null,
    val confirmPassword: FieldError? = null
) {
    /** true, если во ВСЕХ полях ошибок нет — тогда форму можно отправлять на сервер. */
    val isValid: Boolean
        get() = name == null && phone == null && email == null &&
                password == null && confirmPassword == null
}

/**
 * Локальная проверка формы регистрации до отправки на сервер.
 * Чистая Kotlin-логика без Android-зависимостей — удобно тестировать юнит-тестами.
 *
 * Правила регистрации (ЗАПИШИТЕ их как требования в задаче/MR и подправьте под свои):
 *
 * R1 Имя: после обрезки пробелов по краям длина 2..50, пустое — EMPTY.
 * R2 Телефон: пробелы, дефисы и скобки игнорируются; остаётся +7 или 8 и ровно 10 цифр.
 * R3 Почта: вид «локальная_часть@домен.зона», зона не короче 2 букв; пробелы по краям обрезаются.
 * R4 Пароль: не короче 8 символов и содержит минимум одну букву и одну цифру. Пароль не обрезается.
 * R5 Подтверждение: совпадает с паролем с учётом регистра.
 *
 * Как это соотносится с RegisterViewModel.kt: ViewModel сейчас использует СВОИ правила
 * (имя 3..20 символов без цифр, телефон ровно 11 символов, пароль 8..20 с заглавной,
 * строчной, цифрой и спецсимволом, почта через Patterns.EMAIL_ADDRESS). Правила R1–R5
 * мягче и отличаются, так что при подключении валидатора поведение формы изменится.
 */
object RegistrationValidator {
    /** Минимальная длина имени (после trim). Используется в [validateName]. */
    const val NAME_MIN = 2

    /** Максимальная длина имени (после trim). Используется в [validateName]. */
    const val NAME_MAX = 50

    /** Минимальная длина пароля. Используется в [validatePassword]. */
    const val PASSWORD_MIN = 8

    /**
     * Телефон после очистки: либо «+7», либо «8», затем ровно 10 цифр.
     * Примеры подходящих: +79991234567, 89991234567.
     * Очистка от пробелов/дефисов/скобок происходит в [validatePhone] ДО применения regex.
     */
    private val phoneRegex = Regex("^(\\+7|8)\\d{10}$")

    /**
     * Упрощённая проверка почты, по частям:
     * - [A-Za-z0-9._%+-]+              локальная часть (до @);
     * - @                              разделитель;
     * - [A-Za-z0-9-]+                  первая часть домена;
     * - (\.[A-Za-z0-9-]+)*             необязательные дополнительные части домена (поддомены);
     * - \.[A-Za-z]{2,}                 зона (TLD): точка и минимум 2 буквы.
     * Не поддерживает кириллические домены и некоторые экзотические, но валидные адреса.
     */
    private val emailRegex =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /**
     * R1. Проверка имени. Пробелы по краям отбрасываются (trim), затем проверяются
     * пустота и длина. Порядок веток важен: сначала EMPTY, потом длина.
     * Возвращает null, если имя корректно.
     */
    fun validateName(name: String): FieldError? {
        val n = name.trim()
        return when {
            n.isEmpty() -> FieldError.EMPTY
            n.length < NAME_MIN -> FieldError.TOO_SHORT
            n.length > NAME_MAX -> FieldError.TOO_LONG
            else -> null
        }
    }

    /**
     * R2. Проверка телефона.
     * 1) Строка пустая или из одних пробелов — EMPTY.
     * 2) Из строки убираются пробелы, дефисы и скобки (так пользователь может ввести
     *    «+7 (999) 123-45-67»).
     * 3) Результат сверяется с [phoneRegex]; не подошёл — INVALID_FORMAT.
     * Сам номер при этом не изменяется — функция только проверяет. Что отправлять на
     * сервер (очищенный или исходный номер), решает вызывающий код.
     */
    fun validatePhone(phone: String): FieldError? {
        if (phone.isBlank()) return FieldError.EMPTY
        val cleaned = phone.filterNot { it == ' ' || it == '-' || it == '(' || it == ')' }
        return if (phoneRegex.matches(cleaned)) null else FieldError.INVALID_FORMAT
    }

    /**
     * R3. Проверка почты: trim, затем пустота (EMPTY) и соответствие [emailRegex]
     * (иначе INVALID_FORMAT).
     */
    fun validateEmail(email: String): FieldError? {
        val e = email.trim()
        return when {
            e.isEmpty() -> FieldError.EMPTY
            !emailRegex.matches(e) -> FieldError.INVALID_FORMAT
            else -> null
        }
    }

    /**
     * R4. Проверка пароля. Пароль НЕ обрезается (пробелы — часть пароля).
     * Порядок проверок:
     * 1) пустой — EMPTY;
     * 2) короче [PASSWORD_MIN] — TOO_SHORT;
     * 3) нет хотя бы одной буквы или нет хотя бы одной цифры — WEAK_PASSWORD
     *    (isLetter() принимает буквы любых алфавитов, в том числе кириллицу).
     * Верхнего ограничения длины и требования спецсимвола здесь нет.
     */
    fun validatePassword(password: String): FieldError? = when {
        password.isEmpty() -> FieldError.EMPTY
        password.length < PASSWORD_MIN -> FieldError.TOO_SHORT
        !password.any { it.isLetter() } || !password.any { it.isDigit() } -> FieldError.WEAK_PASSWORD
        else -> null
    }

    /**
     * R5. Проверка подтверждения пароля: пустое — EMPTY, не равно паролю (точное
     * сравнение с учётом регистра) — MISMATCH.
     * Принимает и сам пароль, потому что результат зависит от обоих значений.
     */
    fun validateConfirm(password: String, confirm: String): FieldError? = when {
        confirm.isEmpty() -> FieldError.EMPTY
        password != confirm -> FieldError.MISMATCH
        else -> null
    }

    /**
     * Проверяет всю форму сразу и собирает результаты по каждому полю в [RegistrationErrors].
     * Ошибки по всем полям считаются независимо, поэтому пользователь увидит сразу все
     * проблемы, а не только первую. Для проверки «можно ли отправлять» используйте
     * RegistrationErrors.isValid.
     */
    fun validate(form: RegistrationForm) = RegistrationErrors(
        name = validateName(form.name),
        phone = validatePhone(form.phone),
        email = validateEmail(form.email),
        password = validatePassword(form.password),
        confirmPassword = validateConfirm(form.password, form.confirmPassword)
    )
}