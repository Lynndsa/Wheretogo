package com.example.wheretogo.ui.attractions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import android.util.Log
import androidx.compose.foundation.layout.Box
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.ui.theme.AppDimens
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor
import com.example.wheretogo.ui.theme.ButtonTextStyle
import com.example.wheretogo.ui.theme.InputFieldShape
import com.example.wheretogo.ui.theme.LinkTextStyle
import com.example.wheretogo.ui.theme.TextHeadStyle
import com.example.wheretogo.ui.theme.TextLabelStyle
import com.example.wheretogo.ui.theme.WhereToGoTheme
import com.example.wheretogo.ui.theme.primaryButtonColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import okhttp3.OkHttpClient

@Composable
fun AttractionScreen(
    attraction: PlaceItem,
    onAddToRouteClick: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val imageLoader = remember {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "WhereToGo/1.0 (Android; image loading)"
                    )
                    .build()

                chain.proceed(request)
            }
            .build()

        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 5.dp)
        ) {

            SubcomposeAsyncImage(
                model = attraction.imageUrl,
                imageLoader = imageLoader,
                contentDescription = attraction.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp)),
                loading = {
                    Text("Загрузка фотографии...")
                },
                error = {
                    Text("Изображение недоступно")
                }
            )


            Spacer(Modifier.height(16.dp))

            attraction.title?.let {
                Text(
                    text = it,
                    style = TextLabelStyle.copy(fontSize = 20.sp, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            if (!attraction.description.isNullOrBlank()) {
                Text(
                    text = attraction.description,
                    style = LinkTextStyle.copy(fontSize = 17.sp, textAlign = TextAlign.Start),
                )
            }

            if (!attraction.address.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Находится:",
                    style = TextLabelStyle,
                )
                Text(
                    text = attraction.address,
                    style = LinkTextStyle.copy(fontSize = 17.sp, textAlign = TextAlign.Start),
                )
            }

            // Ссылки показываем только если они есть
            if (!attraction.websiteUrl.isNullOrBlank() || !attraction.wikipediaUrl.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    attraction.websiteUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        Text(
                            text = "Сайт",
                            style = LinkTextStyle.copy(textDecoration = TextDecoration.Underline),
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                    }
                    attraction.wikipediaUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        Text(
                            text = "Википедия",
                            style = LinkTextStyle.copy(textDecoration = TextDecoration.Underline),
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Кнопка закреплена внизу и не прокручивается
        Button(
            onClick = onAddToRouteClick,
            colors = primaryButtonColors(),
            shape = InputFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.ButtonHorizontalPadding)
                .padding(top = 12.dp, bottom = 12.dp)
                .navigationBarsPadding()
                .height(AppDimens.ButtonHeight)
        ) {
            Text(text = "Добавить в маршрут", style = ButtonTextStyle.copy(fontSize = 16.sp))
        }
    }
}

