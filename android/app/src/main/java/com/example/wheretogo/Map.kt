package com.example.wheretogo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

@Composable
fun YandexMapView() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        // Запускаем работу MapKit
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        onDispose {
            // Останавливаем работу MapKit при выходе с экрана
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}