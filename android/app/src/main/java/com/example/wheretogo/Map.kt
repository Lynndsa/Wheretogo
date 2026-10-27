package com.example.wheretogo

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import com.example.wheretogo.model.PlaceItem
import com.example.wheretogo.network.RetrofitClient
import com.example.wheretogo.tools.MapLibreView
import com.example.wheretogo.tools.OfflineMapManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    if (LocalInspectionMode.current) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            styleUrl = "",
            initialCenter = LatLng(59.9386, 30.3141),
            initialZoom = 11.0
        )
        return
    }

    val context = LocalContext.current

    // Проверка текущего статуса разрешения геолокации
    var isLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Инициализация лаунчера для запроса разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationGranted = isGranted
        if (isGranted) {
            Log.d("MAP", "Разрешение на геолокацию получено")
        } else {
            Log.w("MAP", "Пользователь отклонил запрос геолокации")
        }
    }

    // Автоматический запуск запроса при открытии экрана
    LaunchedEffect(Unit) {
        if (!isLocationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapTilerStyleUrl = "https://api.maptiler.com/maps/base-v4/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
    var places by remember { mutableStateOf<List<PlaceItem>>(emptyList()) }
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

    LaunchedEffect(Unit) {
        try {
            val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/")
            val response = api.getAllPlaceDB()
            Log.d("MAP", "Код: ${response.code()}, мест: ${response.body()?.size}")
            if (response.isSuccessful) {
                places = response.body().orEmpty()
            }
        } catch (e: Exception) {
            Log.e("MAP", "Ошибка загрузки мест", e)
        }
    }

    MapLibreView(
        modifier = Modifier.fillMaxSize(),
        styleUrl = mapTilerStyleUrl,
        initialCenter = LatLng(59.9386, 30.3141),
        initialZoom = 11.0,
        places = places,
        isLocationGranted = isLocationGranted,
        onPlaceClick = { place -> /* показать bottom sheet и т.п. */ }
    )
}