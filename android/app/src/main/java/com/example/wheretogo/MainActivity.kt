package com.example.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wheretogo.ui.theme.AppDimens
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.LinkTextStyle
import com.example.wheretogo.ui.theme.TextLabelStyle
import com.example.wheretogo.ui.theme.primaryButtonColors
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.setApiKey("3c99d87c-a288-458d-a597-041985b98f3f")
        MapKitFactory.initialize(this)
        setContent {
            YandexMapView()
        }
    }
}

@Preview
@Composable
fun Greeting() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Фоновое изображение на весь экран
        Image(
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Фон главного экрана",
            painter = painterResource(id = R.drawable.main_activity),
            contentScale = ContentScale.Crop
        )

        // 2. Основной контент
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = AppDimens.ScreenPaddingTop,
                    start = AppDimens.ScreenPaddingHorizontal,
                    end = AppDimens.ScreenPaddingHorizontal
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(350.dp),
                contentDescription = "Лого WhereToGo",
                painter = painterResource(id = R.drawable.logo)
            )

            Text(
                text = "Экскурсии, достопримечательности и просто интересные места",
                color = ButtonColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(230.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = AppDimens.ButtonHorizontalPadding)
                    .fillMaxWidth()
                    .height(AppDimens.ButtonHeight),
                colors = primaryButtonColors()
            ) {
                Text(
                    text = "Войти",
                    style = ButtonTextStyle
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.LinkTopSpacer))

            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                style = LinkTextStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Перейти на экран регистрации */ }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}