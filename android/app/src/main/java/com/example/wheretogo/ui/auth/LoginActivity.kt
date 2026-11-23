package com.example.wheretogo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.LinkTextStyle

import com.example.wheretogo.ui.theme.exceptionTypeErrorStyle


/**
 * Экран входа (Jetpack Compose).
 *
 * ## Как устроено разделение обязанностей
 * - Этот файл отвечает только за ОТРИСОВКУ и сбор ввода.
 * - Логика (проверка полей, запрос на сервер, разбор ошибок) находится в [LoginViewModel]
 *   (файл LoginViewModel.kt). Экран вызывает его методы и читает его состояния.
 * - Тексты полей, которые пользователь вводит ([name], [password]), хранятся
 *   в самом экране через remember, а во ViewModel передаются при нажатии «Войти».
 *
 * ## Параметры
 * @param onRedistrationClick вызывается по тапу на «Нет аккаунта? Зарегистрируйтесь».
 *   Навигация к экрану регистрации (RegistrationWindow в RegistrationActivity.kt).
 *   (В имени параметра опечатка «Redistration», на работу не влияет.)
 * @param onLoginSuccess вызывается после успешного входа с двумя значениями:
 *   id пользователя (Long) и его логин (String) — они приходят из ответа сервера и
 *   передаются через [LoginViewModel.login]. Что с ними делать дальше (сохранить сессию,
 *   перейти на главный экран), решает вызывающий код.
 * @param viewModel [LoginViewModel]; по умолчанию создаётся функцией viewModel() и
 *   живёт, пока жив владелец (экран/граф навигации), поэтому переживает поворот экрана.
 */
@Composable
fun LoginWindow(
    onRedistrationClick: () -> Unit,
    onLoginSuccess: (Long, String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // Локальное состояние UI. remember сохраняет значение между перерисовками (рекомпозициями),
    // mutableStateOf заставляет Compose перерисовать экран при изменении значения.
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Показывать ли пароль открытым текстом (кнопка-«глаз» в поле пароля).
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Корневой контейнер: на весь экран, фон из темы, прокручивается по вертикали
    // (нужно, чтобы форма влезала на маленьких экранах и при открытой клавиатуре).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(top = 50.dp, start = 5.dp, end = 5.dp)

    ) {

        // Заголовок экрана.
        Text(
            "ВХОД",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            color = ButtonColor,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(90.dp))

        // ---------- Поле «Имя» ----------
        OutlinedTextField(
            value = name,
            // При каждом вводе: 1) сохраняем текст локально, 2) сообщаем ViewModel,
            // чтобы она убрала старую ошибку поля (LoginViewModel.onLoginChanged).
            onValueChange = { name = it; viewModel.onLoginChanged(it) },
            // Красная рамка, если ViewModel выставила ошибку имени.
            isError = viewModel.loginError != null,
            label = {
                Text(
                    "Имя",
                    fontSize = 17.sp,
                    color = ButtonColor,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ButtonColor,
                unfocusedBorderColor = ButtonColor,
                cursorColor = ButtonColor,
                focusedTextColor = ButtonColor,
            )
        )

        // Текст ошибки под полем имени. Источник: LoginViewModel.loginError — туда попадает
        // и локальная ошибка («Введите имя»), и ответ сервера LOGIN_NOT_EXIST.
        // let выполняется только если ошибка не null.
        viewModel.loginError?.let { errorText ->
            Text(
                text = errorText,
                // exceptionTypeErrorStyle — стиль текста ошибки из темы (пакет ui.theme).
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))


        // ---------- Поле «Пароль» ----------
        OutlinedTextField(
            value = password,
            // Аналогично имени: сохранить текст и сбросить ошибку пароля во ViewModel
            // (LoginViewModel.onPasswordChanged).
            onValueChange = {     password = it
                viewModel.onPasswordChanged(it) },
            isError = viewModel.passwordError != null,
            label = {
                Text(
                    "Пароль",
                    fontSize = 17.sp,
                    color = ButtonColor,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            },

            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth(),
            // Если passwordVisible = true — показываем символы как есть, иначе заменяем на «*».
            visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(
                    '*'
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ButtonColor,
                unfocusedBorderColor = ButtonColor,
                cursorColor = ButtonColor,
                focusedTextColor = ButtonColor,
            ),
            // Кнопка-«глаз» справа: переключает passwordVisible и меняет иконку и описание
            // (описание читают скринридеры для слабовидящих).
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Скрыть пароль" else "Показать пароль"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )
        // Текст ошибки под паролем. Источник: LoginViewModel.passwordError
        // (локально «Введите пароль» или ответ сервера PASSWORD_EXIST — «Неверный пароль»).
        viewModel.passwordError?.let {
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        // Большой отступ, отодвигающий кнопку вниз экрана. Фиксированные 300.dp: на разных
        // экранах выглядит по-разному (при желании можно заменить на weight или Arrangement).
        Spacer(modifier = Modifier.height(300.dp))

        // Общая ошибка, не привязанная к полю: нет сети, ошибка сервера, пустой ответ и т.п.
        // Источник: LoginViewModel.error.
        viewModel.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = exceptionTypeErrorStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ---------- Кнопка «Войти» ----------
        Button(
            // По нажатию передаём введённые значения в LoginViewModel.login(...).
            // ViewModel сама проверит поля и, если всё в порядке, отправит запрос на сервер;
            // при успехе вызовет onSuccess, а мы пробрасываем результат наружу в onLoginSuccess.
            onClick = { viewModel.login(name,
                password,
                onSuccess = { idUser, login ->  onLoginSuccess (idUser, login)})
            },
            // Пока идёт запрос (isLoading), кнопка отключена — защита от двойной отправки.
            enabled = !viewModel.isLoading,
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(ButtonColor),
        ) {
            // Во время загрузки вместо надписи — индикатор, иначе текст «Войти».
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = BackgroundColor,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text(
                    text = "Войти",
                    style = ButtonTextStyle
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))


        //КНОПКА ДЛЯ ТЕСТИРОВАНИЯ
//        Button(
//            onClick = {
//                viewModel.runExperiments(context)
//            },
//            enabled = !viewModel.isExperimentsRunning && !viewModel.isLoading,
//            modifier = Modifier
//                .padding(start = 30.dp, end = 30.dp)
//                .fillMaxWidth()
//                .height(60.dp),
//            colors = ButtonDefaults.buttonColors(ButtonColor)
//        ) {
//            if (viewModel.isExperimentsRunning) {
//                CircularProgressIndicator(
//                    color = BackgroundColor,
//                    modifier = Modifier.height(24.dp)
//                )
//            } else {
//                Text(
//                    text = "Запустить эксперименты ЛР №4",
//                    style = ButtonTextStyle
//                )
//            }
//        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ссылка-переход на регистрацию. clickable с indication = null отключает
        // «волну» при нажатии, чтобы текст вёл себя как ссылка, а не как кнопка.
        // Вызывает onRedistrationClick (навигация на RegistrationWindow).
        Text(
            text = "Нет аккаунта?\nЗарегистрируйтесь",
            style = LinkTextStyle,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRedistrationClick
                ),
        )


    }
}