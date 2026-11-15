package com.example.wheretogo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    // Центр и радиус видимой области карты
    var currentMapCenter by remember { mutableStateOf<LatLng?>(null) }
    var currentRadius by remember { mutableStateOf(5000) }

    // Выбранное место (для карточки снизу)
    var selectedPlace by remember { mutableStateOf<PlaceItem?>(null) }
    var lastShownPlace by remember { mutableStateOf<PlaceItem?>(null) }
    if (selectedPlace != null) lastShownPlace = selectedPlace

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
    LaunchedEffect(isLocationGranted, currentMapCenter, currentRadius) {
        while (isActive) {
            val center = currentMapCenter ?: if (isLocationGranted) {
                val (lat, lon) = getDeviceLocation(context)
                LatLng(lat, lon)
            } else {
                LatLng(59.9386, 30.3141) // По умолчанию СПб
            }

            try {
                val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/")
                val response = api.getNearbyPlaces(
                    lat = center.latitude,
                    lon = center.longitude,
                    radius = currentRadius
                )
                Log.d("MAP", "Код: ${response.code()}, мест в радиусе ${currentRadius}м от (${center.latitude}, ${center.longitude}): ${response.body()?.size}")
                if (response.isSuccessful) {
                    val receivedPlaces = response.body().orEmpty()

                    Log.d("MAP", "Получено мест: ${receivedPlaces.size}")

                    receivedPlaces
                        .groupBy { it.title?.trim()?.lowercase() ?: "" }
                        .filterValues { it.size > 1 }
                        .forEach { (title, duplicates) ->
                            Log.w("MAP", "Дубль: $title, количество: ${duplicates.size}")
                        }

                    val uniqueByTitle = receivedPlaces.distinctBy {
                        it.title
                            ?.trim()
                            ?.lowercase()
                            ?.replace(Regex("\\s+"), " ")
                    }

                    places = uniqueByTitle.distinctBy {
                        Pair(
                            it.latitude,
                            it.longitude
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MAP", "Ошибка загрузки мест каждые 5 сек", e)
            }

            delay(5000.milliseconds)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            styleUrl = mapTilerStyleUrl,
            initialCenter = LatLng(59.9386, 30.3141),
            initialZoom = 11.0,
            places = places,
            isLocationGranted = isLocationGranted,
            onCameraCenterChanged = { newCenter, radius ->
                currentMapCenter = newCenter
                currentRadius = radius
            },
            onPlaceClick = { place -> selectedPlace = place },
            onMapClick = { selectedPlace = null }
        )

        // Карточка выбранного места снизу
        AnimatedVisibility(
            visible = selectedPlace != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            // Последнее непустое место, чтобы текст не пропадал во время анимации закрытия
            val shown = selectedPlace ?: lastShownPlace
            if (shown != null) {
                PlaceInfoCard(
                    place = shown,
                    onClose = { selectedPlace = null }
                )
            }
        }
    }
}

@Composable
private fun PlaceInfoCard(
    place: PlaceItem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.title?.takeIf { it.isNotBlank() } ?: "Без названия",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.address?.takeIf { it.isNotBlank() } ?: "Адрес не указан",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть")
            }
        }
    }
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