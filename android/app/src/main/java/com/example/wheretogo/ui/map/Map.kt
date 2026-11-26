
package com.example.wheretogo.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import com.example.wheretogo.BuildConfig
import com.example.wheretogo.data.local.OfflineMapManager
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import retrofit2.HttpException
import java.io.IOException

private const val DEFAULT_LATITUDE = 59.9386
private const val DEFAULT_LONGITUDE = 30.3141
private const val DEFAULT_ZOOM = 11.0

private const val OFFLINE_NORTH_LATITUDE = 60.1500
private const val OFFLINE_EAST_LONGITUDE = 30.7000
private const val OFFLINE_SOUTH_LATITUDE = 59.7000
private const val OFFLINE_WEST_LONGITUDE = 29.8000

private const val OFFLINE_MIN_ZOOM = 9.0
private const val OFFLINE_MAX_ZOOM = 13.0

private const val PLACES_REFRESH_INTERVAL_MILLIS = 5_000L

/**
 * Экран карты.
 *
 * Запрашивает разрешение на геолокацию, загружает офлайн-карту
 * и периодически получает список мест с сервера.
 */
@Suppress("FunctionNaming")
@Composable
fun MapScreen(onPlaceClick: (PlaceItem) -> Unit = {}) {
    if (LocalInspectionMode.current) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            styleUrl = "",
            initialCenter = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
            initialZoom = DEFAULT_ZOOM
        )
        return
    }

    val context = LocalContext.current
    val isLocationGranted = rememberLocationPermission(context)
    val mapStyleUrl = rememberMapStyleUrl()
    val places = rememberPlaces()

    StartOfflineMapDownload(
        context = context,
        styleUrl = mapStyleUrl
    )

    MapScreenContent(
        onPlaceClick = onPlaceClick,
        styleUrl = mapStyleUrl,
        places = places,
        isLocationGranted = isLocationGranted
    )
}

/**
 * Проверяет разрешение на геолокацию и запрашивает его,
 * если пользователь ещё не предоставил доступ.
 */
@Composable
private fun rememberLocationPermission(context: Context): Boolean {
    var isLocationGranted by remember {
        mutableStateOf(hasLocationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        isLocationGranted = isGranted

        if (isGranted) {
            Log.d("MAP", "Разрешение на геолокацию получено")
        } else {
            Log.w("MAP", "Пользователь отклонил запрос геолокации")
        }
    }

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

    return isLocationGranted
}

private fun hasLocationPermission(context: Context): Boolean {
    val hasFinePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarsePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return hasFinePermission || hasCoarsePermission
}

/**
 * Формирует URL стиля MapTiler.
 * Ключ хранится в BuildConfig, а не непосредственно в исходном коде.
 */
@Composable
private fun rememberMapStyleUrl(): String {
    return remember {
        "https://api.maptiler.com/maps/base-v4/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
    }
}

/**
 * Периодически загружает список мест с сервера.
 */
@Composable
private fun rememberPlaces(): List<PlaceItem> {
    var places by remember {
        mutableStateOf<List<PlaceItem>>(emptyList())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                val response = RetrofitClient.apiService.getDataBase()

                if (response.isSuccessful) {
                    val receivedPlaces = response.body().orEmpty()
                    places = receivedPlaces

                    Log.d(
                        "MAP",
                        "Получено с сервера: ${receivedPlaces.size}"
                    )
                } else {
                    Log.e(
                        "MAP",
                        "Ошибка сервера: ${response.code()} ${response.message()}"
                    )
                }
            } catch (exception: IOException) {
                Log.e("MAP", "Ошибка соединения при загрузке мест", exception)
            } catch (exception: HttpException) {
                Log.e("MAP", "Ошибка HTTP при загрузке мест", exception)
            }

            delay(PLACES_REFRESH_INTERVAL_MILLIS)
        }
    }

    return places
}

/**
 * Запускает загрузку офлайн-региона Санкт-Петербурга.
 */
@Composable
private fun StartOfflineMapDownload(
    context: Context,
    styleUrl: String
) {
    val offlineMapManager = remember(context) {
        OfflineMapManager(context)
    }

    LaunchedEffect(offlineMapManager, styleUrl) {
        withContext(Dispatchers.IO) {
            val spbBounds = LatLngBounds.Builder()
                .include(
                    LatLng(
                        OFFLINE_NORTH_LATITUDE,
                        OFFLINE_EAST_LONGITUDE
                    )
                )
                .include(
                    LatLng(
                        OFFLINE_SOUTH_LATITUDE,
                        OFFLINE_WEST_LONGITUDE
                    )
                )
                .build()

            offlineMapManager.downloadRegion(
                styleUrl = styleUrl,
                regionName = "СПб и пригороды",
                bounds = spbBounds,
                minZoom = OFFLINE_MIN_ZOOM,
                maxZoom = OFFLINE_MAX_ZOOM
            )
        }
    }
}

/**
 * Отображает карту и передаёт ей необходимые данные и обработчики.
 */
@Suppress("FunctionNaming")
@Composable
private fun MapScreenContent(
    onPlaceClick: (PlaceItem) -> Unit,
    styleUrl: String,
    places: List<PlaceItem>,
    isLocationGranted: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onPlaceClick = onPlaceClick,
            styleUrl = styleUrl,
            initialCenter = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
            initialZoom = DEFAULT_ZOOM,
            places = places,
            isLocationGranted = isLocationGranted
        )
    }
}

/**
 * Возвращает последнюю известную геопозицию устройства.
 *
 * Если разрешение отсутствует, координаты недоступны или возникает
 * ошибка доступа к геолокации, возвращает координаты Санкт-Петербурга.
 */
suspend fun getDeviceLocation(context: Context): Pair<Double, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(
                Context.LOCATION_SERVICE
            ) as? LocationManager

            if (locationManager != null && hasLocationPermission(context)) {
                val gpsLocation = getLastKnownLocation(
                    locationManager,
                    LocationManager.GPS_PROVIDER
                )

                val networkLocation = getLastKnownLocation(
                    locationManager,
                    LocationManager.NETWORK_PROVIDER
                )

                val location = gpsLocation ?: networkLocation

                if (location != null) {
                    Log.d(
                        "MAP",
                        "Получена геопозиция пользователя: " +
                                "${location.latitude}, ${location.longitude}"
                    )

                    return@withContext Pair(
                        location.latitude,
                        location.longitude
                    )
                }
            }
        } catch (exception: SecurityException) {
            Log.e("MAP", "Нет доступа к геопозиции", exception)
        }

        Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    }
}

/**
 * Получает последнее известное положение указанного провайдера.
 *
 * При отсутствии доступа возвращает null: вызывающий код
 * попробует использовать другой провайдер.
 */
@Suppress("SwallowedException")
private fun getLastKnownLocation(
    locationManager: LocationManager,
    provider: String
) = try {
    if (locationManager.isProviderEnabled(provider)) {
        locationManager.getLastKnownLocation(provider)
    } else {
        null
    }
} catch (exception: SecurityException) {
    null
}
