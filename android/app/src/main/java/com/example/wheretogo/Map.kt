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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import com.example.wheretogo.tools.OfflineMapManager // Твой менеджер из пакета tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.maplibre.android.geometry.LatLngBounds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapTilerStyleUrl = "https://api.maptiler.com/maps/base-v4/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

    val offlineMapManager = remember { OfflineMapManager(context) }


    // Фоновый запуск скачивания без блокировки UI
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val spbBounds = LatLngBounds.Builder()
                .include(LatLng(60.1500, 30.7000))
                .include(LatLng(59.7000, 29.8000))
                .build()

            offlineMapManager.downloadRegion(
                styleUrl = mapTilerStyleUrl,
                regionName = "СПб и пригороды",
                bounds = spbBounds,
                minZoom = 9.0,
                maxZoom = 13.0
            )
        }
    }
    MapLibreView(
        modifier = Modifier.fillMaxSize(),
        styleUrl = mapTilerStyleUrl,
        initialCenter = LatLng(59.9386, 30.3141),
        initialZoom = 11.0
    )



}