package com.example.wheretogo.ui.auth


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wheretogo.ui.theme.AppDimens
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.InputFieldShape
import com.example.wheretogo.ui.theme.LinkTextStyle
import com.example.wheretogo.ui.theme.TextHeadStyle
import com.example.wheretogo.ui.theme.TextLabelStyle
import com.example.wheretogo.ui.theme.customTextFieldColors
import com.example.wheretogo.ui.theme.exceptionTypeErrorStyle
import com.example.wheretogo.ui.theme.primaryButtonColors


/**
 * Экран регистрации (Jetpack Compose).
 *
 * ## Разделение обязанностей
 * - Этот файл отвечает за ОТРИСОВКУ и хранение вводимых значений.
 * - Проверка полей, запрос на сервер и разбор ошибок находятся в [RegisterViewModel]
 *   (RegisterViewModel.kt). Экран вызывает её методы и читает её состояния.
 * - Оформление (стили, цвета, отступы) берётся из пакета ui.theme: AppDimens, TextHeadStyle,
 *   TextLabelStyle, InputFieldShape, customTextFieldColors(), primaryButtonColors() и др.
 *   В отличие от экрана входа (LoginActivity.kt), где значения заданы прямо в коде,
 *   здесь всё вынесено в тему.
 *
 * ## Параметры
 * @param onLoginClick вызывается по тапу на «Есть аккаунт» — переход на экран входа
 *   (LoginWindow в LoginActivity.kt).
 * @param onRegisterSuccess вызывается после успешной регистрации с id пользователя (Long) и
 *   логином (String) из ответа сервера. Дальнейшие действия решает вызывающий код.
 * @param viewModel [RegisterViewModel]; создаётся функцией viewModel() и переживает
 *   поворот экрана.
 */
@Composable
fun RegistrationWindow(
    onLoginClick: () -> Unit,
    onRegisterSuccess: (Long, String) -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    // Введённые значения полей хранятся в самом экране (remember + mutableStateOf:
    // значение сохраняется между перерисовками и вызывает перерисовку при изменении).
    // Во ViewModel они передаются только при нажатии кнопки.
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // Один флаг видимости на оба поля пароля: «глаз» в любом из них переключает оба.
    var passwordVisible by remember { mutableStateOf(false) }

    // Корневой контейнер: на весь экран, фон из темы, вертикальная прокрутка
    // (форма длинная, нужна на небольших экранах и при открытой клавиатуре).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(
                top = AppDimens.ScreenPaddingTop,
                start = AppDimens.ScreenPaddingHorizontal,
                end = AppDimens.ScreenPaddingHorizontal
            )
    ) {
        // Заголовок экрана.
        Text(
            text = "РЕГИСТРАЦИЯ",
            style = TextHeadStyle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppDimens.TitleBottomSpacer))

        // ---------- Поле «Имя» ----------
        OutlinedTextField(
            value = name,
            // Сохраняем текст и сообщаем ViewModel, чтобы она убрала старую ошибку
            // (RegisterViewModel.onLoginChanged).
            onValueChange = { name = it; viewModel.onLoginChanged(it) },
            label = { Text("Имя", style = TextLabelStyle) },
            shape = InputFieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors()
        )
        // Текст ошибки под полем. Источник: RegisterViewModel.loginError
        // (локальная проверка или ответ сервера LOGIN_ERROR / LOGIN_EXISTS).
        // Обратите внимание: у этого поля нет isError = ..., поэтому рамка не краснеет
        // (в LoginActivity.kt это сделано).
        viewModel.loginError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

        // ---------- Поле «Номер телефона» ----------
        OutlinedTextField(
            value = number,
            // RegisterViewModel.onPhoneChanged сбрасывает ошибку телефона.
            onValueChange = { number = it; viewModel.onPhoneChanged(it) },
            label = { Text("Номер телефона", style = TextLabelStyle) },
            shape = InputFieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors()
        )
        // Ошибка телефона: RegisterViewModel.phoneError (локально или PHONE_ERROR / PHONE_EXISTS).
        viewModel.phoneError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

        // ---------- Поле «Почта» ----------
        OutlinedTextField(
            value = email,
            // RegisterViewModel.onEmailChanged сбрасывает ошибку почты.
            onValueChange = { email = it; viewModel.onEmailChanged(it) },
            label = { Text("Почта", style = TextLabelStyle) },
            shape = InputFieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors()
        )

        // Ошибка почты: RegisterViewModel.emailError (локально или EMAIL_ERROR / EMAIL_EXISTS).
        viewModel.emailError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

        // Иконка-«глаз», общая для обоих полей пароля. Вынесена в переменную-composable,
        // чтобы не дублировать код. Переключает passwordVisible и меняет иконку и
        // описание (описание читают скринридеры).
        val passwordIcon: @Composable () -> Unit = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }

        // Как отображать введённый пароль: открытым текстом или звёздочками.
        // Тоже общая для обоих полей пароля.
        val passwordTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation('*')

        // ---------- Поле «Пароль» ----------
        OutlinedTextField(
            value = password,
            // RegisterViewModel.onPasswordChanged сбрасывает ошибку пароля.
            onValueChange = { password = it; viewModel.onPasswordChanged(it)  },
            label = { Text("Пароль", style = TextLabelStyle) },
            shape = InputFieldShape,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = passwordTransformation,
            colors = customTextFieldColors(),
            trailingIcon = passwordIcon
        )

        // Ошибка пароля: RegisterViewModel.passwordError (правила длины и состава пароля
        // из RegisterViewModel.register).
        viewModel.passwordError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

        // ---------- Поле «Повторите пароль» ----------
        OutlinedTextField(
            value = confirmPassword,
            // RegisterViewModel.onPasswordConfirmChanged сбрасывает ошибку подтверждения.
            onValueChange = { confirmPassword = it; viewModel.onPasswordConfirmChanged(it) },
            label = { Text("Повторите пароль", style = TextLabelStyle) },
            shape = InputFieldShape,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = passwordTransformation,
            colors = customTextFieldColors(),
            trailingIcon = passwordIcon
        )
        // Ошибка подтверждения: RegisterViewModel.confirmPasswordError (несовпадение паролей
        // локально или коды PASSWORD_ERROR / PASSWORD_MISMATCH от сервера).
        viewModel.confirmPasswordError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Общая ошибка, не привязанная к полю: сеть, VALIDATION_ERROR, неизвестный код.
        // Источник: RegisterViewModel.error.
        viewModel.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(AppDimens.ButtonTopSpacer))

        // ---------- Кнопка «Зарегистрироваться» ----------
        Button(
            // По нажатию передаём все значения в RegisterViewModel.register(...).
            // ViewModel сама проверит поля и отправит запрос; при успехе вызовет onSuccess,
            // а мы пробрасываем результат наружу в onRegisterSuccess.
            // Примечание: у кнопки нет enabled = !viewModel.isLoading (как в LoginActivity.kt),
            // поэтому во время запроса её можно нажать повторно.
            onClick = {
                viewModel.register(name,
                    phone = number,
                    email,
                    password,
                    confirmPassword, onSuccess = { idUser, login ->
                        onRegisterSuccess (idUser, login)}
                )
            },
            modifier = Modifier
                .padding(horizontal = AppDimens.ButtonHorizontalPadding)
                .fillMaxWidth()
                .height(AppDimens.ButtonHeight),
            colors = primaryButtonColors()
        ) {
            // Во время загрузки вместо надписи — индикатор, иначе текст кнопки.
            if( viewModel.isLoading){
                CircularProgressIndicator(
                    color = BackgroundColor,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text(text = "Зарегистрироваться",
                    style = ButtonTextStyle)
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.LinkTopSpacer))

        // Ссылка «Есть аккаунт» -> переход на экран входа (onLoginClick).
        // clickable с indication = null убирает «волну» при нажатии, чтобы текст
        // вёл себя как ссылка, а не как кнопка.
        Text(
            text = "Есть аккаунт",
            style = LinkTextStyle,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLoginClick
                )
        )
    }
}