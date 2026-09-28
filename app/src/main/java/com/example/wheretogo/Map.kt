package com.example.wheretogo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.wheretogo.tools.MapLibreView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import org.maplibre.android.geometry.LatLng

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

@Composable
fun MapScreen() {
    val mapTilerStyleUrl = "https://api.maptiler.com/maps/base-v4/style.json?key=E0xNLSUQFbDl83g71Xew"

    MapLibreView(
        styleUrl = mapTilerStyleUrl,
        initialCenter = LatLng(59.9386, 30.3141), // Центр на Санкт-Петербург
        initialZoom = 11.0
    )
}