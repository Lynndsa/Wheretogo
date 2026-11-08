package com.example.wheretogo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

// Модель на клиенте: поля те же, что в Attraction на бэкенде.
// Потом лучше вынести в пакет model.
data class Attraction(
    val id: Long,
    val title: String,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val websiteUrl: String? = null,
    val wikipediaUrl: String? = null,
)

@Composable
fun AttractionScreen(
    attraction: Attraction,
    onAddToRouteClick: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val imagePlaceholder = ColorPainter(ButtonColor.copy(alpha = 0.15f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Шапка. Высота 64.dp подобрана под иконку меню из AppNavHost
        // (она стоит с отступом 8.dp и имеет размер 48.dp), чтобы они были на одной линии.
        // Боковые отступы 52.dp оставляют место под иконку.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 52.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ДОСТОПРИМЕЧАТЕЛЬНОСТИ",
                style = TextHeadStyle.copy(fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Прокручиваемая часть: картинка, название, описание, адрес, ссылки
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AsyncImage(
                model = attraction.imageUrl,
                contentDescription = attraction.title,
                placeholder = imagePlaceholder,
                error = imagePlaceholder,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = attraction.title,
                style = TextLabelStyle.copy(fontSize = 20.sp, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )

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
                .padding(top = 12.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .height(AppDimens.ButtonHeight)
        ) {
            Text(text = "Добавить в маршрут", style = ButtonTextStyle.copy(fontSize = 16.sp))
        }
    }
}

// Тестовые данные, пока нет запроса к бэкенду
val sampleAttraction = Attraction(
    id = 1,
    title = "Государственный Эрмитаж",
    description = "Государственный Эрмитаж — это один из крупнейших и самых известных " +
            "художественных и культурно-исторических музеев мира, расположенный в Санкт-Петербурге.",
    address = "Главный комплекс музея расположен в самом центре Санкт-Петербурга " +
            "по адресу: Дворцовая площадь, дом 2",
    latitude = 59.9398,
    longitude = 30.3146,
    category = "Музей",
    imageUrl = null, // сюда подставится ссылка на картинку
    websiteUrl = "https://www.hermitagemuseum.org",
    wikipediaUrl = "https://ru.wikipedia.org/wiki/Эрмитаж",
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AttractionScreenPreview() {
    WhereToGoTheme {
        AttractionScreen(attraction = sampleAttraction)
    }
}