package com.example.wheretogo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.wheretogo.ui.theme.AppDimens
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.InputFieldShape
import com.example.wheretogo.ui.theme.LinkTextStyle
import com.example.wheretogo.ui.theme.TextHeadStyle
import com.example.wheretogo.ui.theme.TextLabelStyle
import com.example.wheretogo.ui.theme.customTextFieldColors
import com.example.wheretogo.ui.theme.primaryButtonColors

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }
}
    @Preview
    @Composable
    fun RegistrationWindow() {
        var name by remember { mutableStateOf("") }
        var number by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(
                    top = AppDimens.ScreenPaddingTop,
                    start = AppDimens.ScreenPaddingHorizontal,
                    end = AppDimens.ScreenPaddingHorizontal
                )
        ) {
            Text(
                text = "РЕГИСТРАЦИЯ",
                style = TextHeadStyle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppDimens.TitleBottomSpacer))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя", style = TextLabelStyle) },
                shape = InputFieldShape,
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Номер телефона", style = TextLabelStyle) },
                shape = InputFieldShape,
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Почта", style = TextLabelStyle) },
                shape = InputFieldShape,
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            val passwordIcon: @Composable () -> Unit = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }

            val passwordTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation('*')

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль", style = TextLabelStyle) },
                shape = InputFieldShape,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = passwordTransformation,
                colors = customTextFieldColors(),
                trailingIcon = passwordIcon
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Повторите пароль", style = TextLabelStyle) },
                shape = InputFieldShape,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = passwordTransformation,
                colors = customTextFieldColors(),
                trailingIcon = passwordIcon
            )

            Spacer(modifier = Modifier.height(AppDimens.ButtonTopSpacer))

            Button(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = AppDimens.ButtonHorizontalPadding)
                    .fillMaxWidth()
                    .height(AppDimens.ButtonHeight),
                colors = primaryButtonColors()
            ) {
                Text(text = "Зарегистрироваться", style = ButtonTextStyle)
            }

            Spacer(modifier = Modifier.height(AppDimens.LinkTopSpacer))

            Text(
                text = "Есть аккаунт",
                style = LinkTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
