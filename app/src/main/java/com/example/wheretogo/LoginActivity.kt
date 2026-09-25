package com.example.wheretogo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }
}
@Preview
@Composable
fun LoginWindow() {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(top = 50.dp, start = 5.dp, end = 5.dp)

    ) {

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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
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

        Spacer(modifier = Modifier.height(16.dp))



        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Скрыть пароль" else "Показать пароль"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )
        Spacer(modifier = Modifier.height(300.dp))
        Button(
            onClick = {  },
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(ButtonColor),

            ) {
            Text(text = "Войти",
                color = BackgroundColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Нет аккаунта?\nЗарегистрируйтесь",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = ButtonColor,
            fontSize = 15.sp,
            textDecoration = TextDecoration.Underline
        )


    }
}

