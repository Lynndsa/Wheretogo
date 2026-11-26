package com.example.wheretogo.ui.map


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.ui.theme.AppDimens
import com.example.wheretogo.ui.theme.BackgroundColor



/**
 * Экран-«обёртка» вкладки с картой (несмотря на имя файла «MapActivity», это не Activity,
 * а @Composable-функция, которую подключает навигация приложения; код навигации
 * в загруженных файлах отсутствует).
 *
 * ## Роль
 * Отвечает только за внешнюю рамку: фон приложения, отступы от краёв экрана и скруглённые
 * углы у карты. Вся логика (разрешения, офлайн-карта, загрузка мест) находится в [MapScreen]
 * (Map.kt), а сама карта рисуется в [MapLibreView] (MapLibreView.kt).
 *
 * Цепочка вызовов: MapWindow -> MapScreen -> MapLibreView.
 *
 * ## Оформление
 * Отступы и цвета берутся из темы (пакет ui.theme): AppDimens.ScreenPaddingTop,
 * AppDimens.ScreenPaddingHorizontal, BackgroundColor.
 *
 * ## @Preview
 * Аннотация позволяет смотреть экран в предпросмотре Android Studio. Там реальная карта
 * не работает, поэтому [MapScreen] и [MapLibreView] определяют режим предпросмотра
 * (LocalInspectionMode) и показывают заглушку.
 *
 * @param onPlaceClick вызывается при тапе по месту на карте; пробрасывается без изменений
 *   в [MapScreen]. По умолчанию — пустая функция.
 */
@Preview
@Composable
fun MapWindow(
    onPlaceClick: (PlaceItem) -> Unit = {}
) {

    // Внешний контейнер на весь экран с фоном темы и внешними отступами.
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

        // Дополнительный отступ сверху. Такое же значение (ScreenPaddingTop) уже задано в
        // padding выше, поэтому итоговый верхний отступ получается двойным.
        Spacer(modifier = Modifier.height(AppDimens.ScreenPaddingTop))

        // Контейнер карты: небольшой отступ и скругление углов на 20 dp.
        // clip обрезает содержимое (карту) по скруглённой форме.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            MapScreen(onPlaceClick = onPlaceClick)
        }
    }
}