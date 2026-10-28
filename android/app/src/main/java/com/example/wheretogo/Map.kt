package com.example.wheretogo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.time.Duration.Companion.milliseconds

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

    // Центр экрана карты
    var currentMapCenter by remember { mutableStateOf<LatLng?>(null) }

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

    // Динамическая загрузка мест вокруг центра экрана каждые 5 секунд
    LaunchedEffect(isLocationGranted, currentMapCenter) {
        while (isActive) {
            val center = currentMapCenter ?: if (isLocationGranted) {
                val (lat, lon) = getDeviceLocation(context)
                LatLng(lat, lon)
            } else {
                LatLng(59.9386, 30.3141) // По умолчанию СПб
            }

            try {
                val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/")
                val response = api.getNearbyPlaces(lat = center.latitude, lon = center.longitude, radius = 5000)
                Log.d("MAP", "Код: ${response.code()}, мест в радиусе 5км от (${center.latitude}, ${center.longitude}): ${response.body()?.size}")
                if (response.isSuccessful) {
                    places = response.body().orEmpty()
                }
            } catch (e: Exception) {
                Log.e("MAP", "Ошибка загрузки мест каждые 5 сек", e)
            }

            delay(5000.milliseconds)
        }
    }

    MapLibreView(
        modifier = Modifier.fillMaxSize(),
        styleUrl = mapTilerStyleUrl,
        initialCenter = LatLng(59.9386, 30.3141),
        initialZoom = 11.0,
        places = places,
        isLocationGranted = isLocationGranted,
        onCameraCenterChanged = { newCenter ->
            currentMapCenter = newCenter
        },
        onPlaceClick = { place -> /* показать bottom sheet и т.п. */ }
    )
}

private suspend fun getDeviceLocation(context: Context): Pair<Double, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if ((hasFine || hasCoarse) && locationManager != null) {
                val gpsLoc = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
                } else null

                val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
                } else null

                val loc = gpsLoc ?: netLoc
                if (loc != null) {
                    Log.d("MAP", "Получена геопозиция пользователя: ${loc.latitude}, ${loc.longitude}")
                    return@withContext Pair(loc.latitude, loc.longitude)
                }
            }
        } catch (e: Exception) {
            Log.e("MAP", "Ошибка получения геопозиции", e)
        }
        Pair(59.9386, 30.3141)
    }
}