package com.example.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wheretogo.ui.theme.WhereToGoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

        }
    }
}

@Composable
fun Greeting() {
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        // 1. Фоновое изображение на весь экран
        Image(
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Фон главного экрана",
            painter = painterResource(id = R.drawable.main_activity),
            contentScale = ContentScale.Crop
        )
        
        // 2. Колонка для размещения логотипа и текста друг под другом
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(400.dp),
                contentDescription = "Лого WhereToGo",
                painter = painterResource(id = R.drawable.logo),
            )
            Text(
                text = "Экскурсии, достопримечательности и просто интересные места",
                textAlign = TextAlign.Center
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Занимает 70% ширины экрана, делая кнопку шире
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 200.dp),
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF315C45), // Цвет фона кнопки (например, синий)
                    contentColor = Color.White        // Цвет текста кнопки
                )
            ) {
                Text(text = "Войти", fontSize = 20.sp, fontWeight = FontWeight.Bold) // Текст перенесен внутрь тела кнопки
            }
            Text (
                modifier = Modifier.clickable{/*TODO*/},
                text = "Нет аккаунта? Зарегистрироваться"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhereToGoTheme {
        Greeting()
    }
}