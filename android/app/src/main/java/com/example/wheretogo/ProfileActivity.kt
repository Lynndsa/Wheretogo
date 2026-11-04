package com.example.wheretogo

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wheretogo.ui.theme.AppDimens
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.InputFieldShape
import com.example.wheretogo.ui.theme.LinkTextStyle
import com.example.wheretogo.ui.theme.TextHeadStyle
import com.example.wheretogo.ui.theme.TextLabelStyle
import com.example.wheretogo.ui.theme.WhereToGoTheme
import com.example.wheretogo.ui.theme.customTextFieldColors
import com.example.wheretogo.ui.theme.primaryButtonColors
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.wheretogo.model.ProfileUpdateRequest
import com.example.wheretogo.model.ProfileViewModel

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhereToGoTheme {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onMenuClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    val profile = viewModel.profile
    val isLoading = viewModel.isLoading
    val error = viewModel.error

    var login by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            login = it.login
            firstName = it.name
            lastName = it.lastname ?: ""
            middleName = it.surname ?: ""
            phone = it.phonenumber
            email = it.email
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = AppDimens.ScreenPaddingTop,
                    start = AppDimens.ScreenPaddingHorizontal,
                    end = AppDimens.ScreenPaddingHorizontal
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ПРОФИЛЬ",
                style = TextHeadStyle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            Image(
                painter = painterResource(id = R.drawable.avatar), // твоя картинка
                contentDescription = "Аватар",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(AppDimens.LinkTopSpacer))

            Text(text = login, style = LinkTextStyle)

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = login,
                onValueChange = { login = it },
                label = "Логин"
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "Имя"
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Фамилия"
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = middleName,
                onValueChange = { middleName = it },
                label = "Отчество"
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = phone,
                onValueChange = { phone = it },
                label = "Номер телефона",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))

            ProfileField(
                value = email,
                onValueChange = { email = it },
                label = "Почта",
                keyboardType = KeyboardType.Email
            )

            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text(
                    text = it,
                    color = ButtonColor
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.ButtonTopSpacer))

            Button(
                onClick = {
                    Log.d("PROFILE_DEBUG", "Нажата кнопка сохранения")

                    val request = ProfileUpdateRequest(
                        login = login,
                        name = firstName,
                        lastname = lastName,
                        surname = middleName,
                        phonenumber = phone,
                        email = email
                    )

                    Log.d("PROFILE_DEBUG", "Вызываем updateProfile")

                    viewModel.updateProfile(context, request) {
                        Log.d("PROFILE_DEBUG", "Профиль успешно сохранён")
                        saveMessage = "Изменения сохранены!"
                    }
                },
                modifier = Modifier
                    .padding(horizontal = AppDimens.ButtonHorizontalPadding)
                    .fillMaxWidth()
                    .height(AppDimens.ButtonHeight),
                colors = primaryButtonColors()
            ) {
                Text(
                    text = "Сохранить изменения",
                    style = ButtonTextStyle
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.FieldSpacing))
        }

    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = TextLabelStyle) },
        shape = InputFieldShape,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        colors = customTextFieldColors()
    )
}