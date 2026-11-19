package com.example.wheretogo.ui.map

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
import com.example.wheretogo.BuildConfig
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.local.OfflineMapManager
import com.example.wheretogo.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * «Умный» экран карты: здесь вся логика, а сама карта рисуется в [MapLibreView]
 * (файл MapLibreView.kt).
 *
 * ## Что делает этот экран
 * 1. Запрашивает у пользователя разрешение на геолокацию.
 * 2. В фоне скачивает офлайн-регион карты Санкт-Петербурга (OfflineMapManager из
 *    пакета data.local; его кода в загруженных файлах нет; процент загрузки считает
 *    [OfflineProgress] из OfflineProgress.kt).
 * 3. Следит за положением и масштабом карты и, когда пользователь «уезжает» к границе
 *    уже загруженной области, подгружает места с сервера (RetrofitClient.apiService.getNearbyPlaces).
 * 4. Передаёт список мест и колбэки в [MapLibreView].
 *
 * ## Где используется
 * Вызывается из [MapWindow] (MapActivity.kt), который задаёт рамку, фон и отступы.
 *
 * @param onPlaceClick вызывается, когда пользователь тапнул по точке-месту на карте.
 *   Пробрасывается в [MapLibreView]; что делать дальше (например, открыть экран места),
 *   решает вызывающий код.
 */
@Composable
fun MapScreen(  onPlaceClick: (PlaceItem) -> Unit = {}) {
    // В режиме предпросмотра Android Studio (@Preview) сеть, разрешения и MapLibre недоступны.
    // Поэтому рисуем упрощённый вариант (MapLibreView в этом режиме покажет серую заглушку
    // «Map Preview») и сразу выходим.
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

    // Центр и радиус видимой области карты.
    // Обновляются колбэком onCameraCenterChanged из MapLibreView, когда камера остановилась.
    // currentMapCenter == null, пока карта ещё ни разу не сообщила своё положение.
    // Радиус в метрах; 5000 — стартовое значение до первого сообщения от карты.
    var currentMapCenter by remember { mutableStateOf<LatLng?>(null) }
    var currentRadius by remember { mutableIntStateOf(5000) }

    // Выбранное место (для карточки снизу).
    // Сейчас карточка не отрисована: selectedPlace только сбрасывается в null при клике по
    // пустому месту карты и нигде не выставляется. Импорты AnimatedVisibility, Row, Close и
    // т.п. вверху файла относятся к этой ещё не реализованной (или удалённой) карточке.
    var selectedPlace by remember { mutableStateOf<PlaceItem?>(null) }
    // lastShownPlace запоминает последнее непустое значение — типичный приём, чтобы при
    // анимации скрытия карточки в ней не пропадал текст.
    var lastShownPlace by remember { mutableStateOf<PlaceItem?>(null) }
    if (selectedPlace != null) lastShownPlace = selectedPlace

    // Проверка текущего статуса разрешения геолокации:
    // true, если выдано точное (FINE) ИЛИ приблизительное (COARSE) разрешение.
    var isLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Инициализация лаунчера для запроса разрешений.
    // Результат приходит в лямбду ниже: достаточно любого из двух разрешений.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        // Обновление состояния перерисует экран и перезапустит эффекты, зависящие от
        // isLocationGranted (см. загрузку мест ниже и включение геолокации в MapLibreView).
        isLocationGranted = isGranted
        if (isGranted) {
            Log.d("MAP", "Разрешение на геолокацию получено")
        } else {
            Log.w("MAP", "Пользователь отклонил запрос геолокации")
        }
    }

    // Автоматический запуск запроса при открытии экрана.
    // LaunchedEffect(Unit) выполняется один раз при входе на экран.
    // Если разрешение уже есть — диалог не показывается.
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

    // Адрес стиля карты MapTiler. Ключ берётся из BuildConfig (поле MAPTILER_API_KEY
    // задаётся в build.gradle модуля, обычно из local.properties), чтобы не хранить его в коде.
    val mapTilerStyleUrl = "https://api.maptiler.com/maps/base-v4/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
    // Список мест, показанных на карте. Обновляется после успешной загрузки с сервера.
    var places by remember { mutableStateOf<List<PlaceItem>>(emptyList()) }
    val offlineMapManager = remember { OfflineMapManager(context) }

    // Фоновый запуск скачивания без блокировки UI.
    // withContext(Dispatchers.IO) переносит работу в поток для ввода-вывода.
    // Скачивается регион «СПб и пригороды»: прямоугольник между двумя углами
    // (северо-восток 60.15; 30.70 и юго-запад 59.70; 29.80) для масштабов от 9 до 13.
    // Запускается при каждом входе на экран; как менеджер защищается от повторной загрузки
    // уже скачанного региона — смотрите OfflineMapManager.downloadRegion.
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

    // Подгрузка мест, когда видимая область подходит к границе загруженного круга.
    // loadedCenter и loadedRadius — центр и радиус (в метрах) круга, для которого места
    // уже получены с сервера.
    var loadedCenter by remember { mutableStateOf<LatLng?>(null) }
    var loadedRadius by remember { mutableIntStateOf(0) }

    // Эффект перезапускается при смене isLocationGranted (например, после выдачи разрешения).
    @OptIn(FlowPreview::class)
    LaunchedEffect(isLocationGranted) {
        // snapshotFlow превращает чтение Compose-состояний в Flow: он выдаёт новую пару
        // (центр, радиус) при каждом изменении любого из них.
        snapshotFlow { currentMapCenter to currentRadius }
            // debounce: ждём 300 мс «тишины», чтобы не слать запросы, пока пользователь двигает карту.
            .debounce(300.milliseconds)
            // collectLatest: если пришло новое значение, а предыдущая обработка (запрос) ещё идёт,
            // она отменяется — остаётся только самая свежая.
            .collectLatest { (mapCenter, visibleRadius) ->
                // Откуда брать центр для запроса:
                // 1) положение карты, если оно уже известно;
                // 2) иначе позиция устройства (если есть разрешение);
                // 3) иначе центр Санкт-Петербурга по умолчанию.
                val center = mapCenter ?: if (isLocationGranted) {
                    val (lat, lon) = getDeviceLocation(context)
                    LatLng(lat, lon)
                } else {
                    LatLng(59.9386, 30.3141)
                }

                // Расстояние (в метрах) от текущего центра до центра уже загруженной области.
                // Если ничего ещё не загружено — считаем расстояние бесконечным.
                val lc = loadedCenter
                val distanceToLoadedCenter = lc?.let { center.distanceTo(it) } ?: Double.MAX_VALUE

                // Нужна перезагрузка, если ничего не загружено или видимая область (расстояние
                // до центра + радиус экрана) вышла за 80% загруженного круга.
                val needReload = lc == null ||
                        distanceToLoadedCenter + visibleRadius > loadedRadius * 0.8

                if (!needReload) return@collectLatest

                // Радиус запроса: вдвое больше видимого, но не меньше 5 км и не больше 30 км.
                // Запас нужен, чтобы при небольшом сдвиге карты не делать новый запрос.
                val radiusToLoad = maxOf(5000, visibleRadius * 2).coerceAtMost(30000)

                try {
                    val response = RetrofitClient.apiService.getNearbyPlaces(
                        lat = center.latitude,
                        lon = center.longitude,
                        radius = radiusToLoad
                    )
                    if (response.isSuccessful) {
                        val received = response.body().orEmpty()
                        // Убираем дубликаты: сначала по названию (без учёта регистра и лишних
                        // пробелов), затем по паре координат.
                        // Замечание: у мест без названия (title == null) ключ одинаковый (null),
                        // поэтому из всех безымянных мест останется только первое.
                        places = received
                            .distinctBy { it.title?.trim()?.lowercase()?.replace(Regex("\\s+"), " ") }
                            .distinctBy { Pair(it.latitude, it.longitude) }

                        // Запоминаем, для какой области данные получены (используется выше
                        // при следующей проверке needReload).
                        loadedCenter = center
                        loadedRadius = radiusToLoad
                        Log.d("MAP", "Загружено ${places.size} мест, радиус $radiusToLoad м")
                    }
                    // Неуспешный HTTP-ответ молча игнорируется: пользователю ошибка не показывается.
                } catch (e: CancellationException) {
                    // Отмену корутины нельзя «глотать» — пробрасываем дальше.
                    throw e
                } catch (e: Exception) {
                    Log.e("MAP", "Ошибка загрузки мест", e)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Сама карта. Всё, что она «знает», приходит параметрами; всё, что происходит на
        // карте, возвращается колбэками.
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onPlaceClick = onPlaceClick,
            styleUrl = mapTilerStyleUrl,
            initialCenter = LatLng(59.9386, 30.3141),
            initialZoom = 11.0,
            places = places,
            isLocationGranted = isLocationGranted,
            // Карта сообщает новый центр и радиус, когда камера остановилась. Эти значения
            // запускают snapshotFlow в блоке загрузки мест выше.
            onCameraCenterChanged = { newCenter, radius ->
                currentMapCenter = newCenter
                currentRadius = radius
            },
            // Клик по пустому месту карты снимает выбор места.
            onMapClick = { selectedPlace = null }
        )

    }
}




/**
 * Возвращает последнюю известную геопозицию устройства как пару (широта, долгота).
 *
 * Как работает:
 * - Выполняется в потоке ввода-вывода (Dispatchers.IO).
 * - Если выдано хотя бы одно разрешение на геолокацию, спрашивает у LocationManager
 *   последнее известное положение: сначала от GPS, потом от сети. GPS приоритетнее.
 * - Это «последнее известное» положение (getLastKnownLocation), а не свежее определение:
 *   оно может быть устаревшим или отсутствовать.
 * - Если позиции нет, нет разрешения или произошла ошибка — возвращает центр
 *   Санкт-Петербурга (59.9386, 30.3141).
 *
 * Используется в блоке загрузки мест в [MapScreen], когда карта ещё не сообщила свой центр.
 */
suspend fun getDeviceLocation(context: Context): Pair<Double, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if ((hasFine || hasCoarse) && locationManager != null) {
                // Каждый провайдер опрашивается, только если он включён в настройках устройства.
                // SecurityException (разрешение отозвали на лету) превращается в null.
                val gpsLoc = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
                } else null

                val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
                } else null

                // Берём GPS, а если его нет — сетевую позицию.
                val loc = gpsLoc ?: netLoc
                if (loc != null) {
                    Log.d("MAP", "Получена геопозиция пользователя: ${loc.latitude}, ${loc.longitude}")
                    return@withContext Pair(loc.latitude, loc.longitude)
                }
            }
        } catch (e: Exception) {
            Log.e("MAP", "Ошибка получения геопозиции", e)
        }
        // Запасной вариант: центр Санкт-Петербурга.
        Pair(59.9386, 30.3141)
    }
}