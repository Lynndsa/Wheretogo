package com.example.wheretogo.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.wheretogo.data.model.PlaceItem
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property.TEXT_ANCHOR_TOP
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textAnchor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textOptional
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ---------- Идентификаторы источника данных и слоёв карты ----------
// Источник данных (GeoJSON) один — в нём лежат все места. Поверх него строятся четыре слоя;
// по этим идентификаторам слои находят при кликах (queryRenderedFeatures).
private const val PLACES_SOURCE = "places-source"
private const val PLACES_LAYER = "places-layer"
private const val LABELS_LAYER = "places-labels-layer"
private const val CLUSTER_LAYER = "places-cluster-layer"
private const val CLUSTER_COUNT_LAYER = "places-cluster-count-layer"

// Ограничения радиуса видимой области (в метрах), который карта сообщает наружу.
// Нижняя граница нужна, чтобы при сильном приближении не слать слишком маленький радиус;
// верхняя — чтобы при отдалении не запрашивать слишком много данных.
private const val MIN_RADIUS_METERS = 1000
private const val MAX_RADIUS_METERS = 50000
private const val DUPLICATE_DISTANCE_METERS = 30.0

/**
 * Компонент карты на основе MapLibre (векторная карта с кластеризацией мест).
 * Это «глупый» компонент: сам ничего не загружает с сервера и не запрашивает разрешения.
 * Данные и решения приходят от [MapScreen] (Map.kt), а события уходят ему обратно колбэками.
 *
 * ## Что делает
 * - Создаёт MapView и загружает стиль карты (адрес стиля задан в MapScreen).
 * - Добавляет источник GeoJSON с кластеризацией и четыре слоя: кружки кластеров, число в
 *   кластере, одиночные точки и подписи точек.
 * - Показывает места из [places] и обновляет их при изменении списка.
 * - Обрабатывает тапы: по кластеру — приближает, по точке — сообщает о месте, по пустому
 *   месту — сообщает о клике на карту.
 * - Сообщает центр и радиус видимой области, когда камера остановилась.
 * - При наличии разрешения включает на карте индикатор положения пользователя.
 *
 * ## Параметры
 * @param modifier стандартный модификатор Compose.
 * @param styleUrl адрес стиля карты (JSON); в [MapScreen] это стиль MapTiler.
 * @param places места для отображения. Места без координат пропускаются.
 * @param initialCenter начальный центр камеры (применяется один раз при создании карты).
 * @param initialZoom начальный масштаб (применяется один раз).
 * @param isLocationGranted выдано ли разрешение на геолокацию; решает, включать ли слежение.
 * @param onCameraCenterChanged вызывается, когда камера остановилась: центр карты и радиус
 *   видимой области в метрах (в пределах 1000..50000). [MapScreen] использует его для
 *   решения, пора ли подгружать новые места.
 * @param onPlaceClick вызывается при тапе по одиночной точке с соответствующим [PlaceItem].
 * @param onMapClick вызывается при тапе по пустому месту (не по точке и не по кластеру).
 */
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(59.9386, 30.3141),
    initialZoom: Double = 11.0,
    isLocationGranted: Boolean = false,
    onCameraCenterChanged: (LatLng, Int) -> Unit = { _, _ -> },
    onPlaceClick: (PlaceItem) -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    // В предпросмотре Android Studio настоящая карта не работает — показываем серую заглушку.
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Map Preview")
        }
        return
    }

    val context = LocalContext.current

    // Всегда актуальные значения для слушателей, которые регистрируются один раз.
    // Слушатели карты (ниже) создаются один раз и «запоминают» значения на момент создания.
    // rememberUpdatedState даёт обёртку, чья текущая версия всегда свежая, поэтому слушатель
    // видит актуальный список мест и актуальные колбэки.
    val currentPlaces by rememberUpdatedState(places)
    val currentOnPlaceClick by rememberUpdatedState(onPlaceClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnCameraCenterChanged by rememberUpdatedState(onCameraCenterChanged)

    // Список мест в том же порядке, в котором они отображаются на карте
    var displayedPlaces by remember {
        mutableStateOf(emptyList<PlaceItem>())
    }

    val currentDisplayedPlaces by rememberUpdatedState(displayedPlaces)

    // Однократная инициализация библиотеки MapLibre. setConnected(true) принудительно
    // сообщает ей, что сеть доступна (отключает собственное определение состояния сети).
    remember {
        MapLibre.getInstance(context).also {
            MapLibre.setConnected(true)
        }
    }

    var map by remember {
        mutableStateOf<MapLibreMap?>(null)
    }

    var styleReady by remember {
        mutableStateOf(false)
    }

    // Создание MapView один раз. onCreate(null) вызывается вручную, остальные события
    // жизненного цикла (onStart, onResume и т.д.) пробрасываются ниже в DisposableEffect.
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)

            getMapAsync { m ->
                map = m

                // Начальное положение камеры.
                m.cameraPosition = CameraPosition.Builder()
                    .target(initialCenter)
                    .zoom(initialZoom)
                    .build()

                m.setStyle(styleUrl) { style ->

                    // Источник с кластеризацией. Пока он пуст; точки в него попадают
                    // в блоке «Обновление точек при изменении списка» ниже.
                    style.addSource(
                        GeoJsonSource(
                            PLACES_SOURCE,
                            GeoJsonOptions()
                                .withCluster(true)
                                .withClusterRadius(60)
                                .withClusterMaxZoom(14)
                        )
                    )

                    // Выражение-фильтр: у кластера библиотека сама добавляет свойство point_count.
                    val isCluster = Expression.has("point_count")

                    // Кружки кластеров: цвет и размер зависят от количества мест в кластере.
                    // step работает по порогам: до 10 — синий и радиус 16, от 10 до 49 —
                    // оранжевый и 22, от 50 — красный и 28.
                    style.addLayer(
                        CircleLayer(CLUSTER_LAYER, PLACES_SOURCE)
                            .withFilter(isCluster)
                            .withProperties(
                                circleColor(
                                    Expression.step(
                                        Expression.get("point_count"),
                                        Expression.color("#42A5F5".toColorInt()),
                                        Expression.stop(
                                            10,
                                            Expression.color("#FB8C00".toColorInt())
                                        ),
                                        Expression.stop(
                                            50,
                                            Expression.color("#E53935".toColorInt())
                                        )
                                    )
                                ),
                                circleRadius(
                                    Expression.step(
                                        Expression.get("point_count"),
                                        Expression.literal(16f),
                                        Expression.stop(10, 22f),
                                        Expression.stop(50, 28f)
                                    )
                                ),
                                circleStrokeWidth(2f),
                                circleStrokeColor("#FFFFFF")
                            )
                    )

                    // Число мест внутри кластера (белый текст поверх кружка).
                    // point_count_abbreviated — сокращённая запись числа (например, «1k»).
                    // Перекрытия разрешены, чтобы число всегда было видно.
                    style.addLayer(
                        SymbolLayer(CLUSTER_COUNT_LAYER, PLACES_SOURCE)
                            .withFilter(isCluster)
                            .withProperties(
                                textField(
                                    Expression.toString(
                                        Expression.get("point_count_abbreviated")
                                    )
                                ),
                                textSize(13f),
                                textColor("#FFFFFF"),
                                textAllowOverlap(true),
                                textIgnorePlacement(true)
                            )
                    )

                    // Одиночные точки (не кластеры): красный кружок с белой обводкой.
                    style.addLayer(
                        CircleLayer(PLACES_LAYER, PLACES_SOURCE)
                            .withFilter(Expression.not(isCluster))
                            .withProperties(
                                circleRadius(8f),
                                circleColor("#E53935"),
                                circleStrokeWidth(2f),
                                circleStrokeColor("#FFFFFF")
                            )
                    )

                    // Подписи одиночных точек (не накладываются друг на друга).
                    // Текст берётся из свойства title, ставится под точкой (anchor top со
                    // смещением); textOptional(true) позволяет карте скрывать подпись,
                    // если для неё нет места.
                    style.addLayer(
                        SymbolLayer(LABELS_LAYER, PLACES_SOURCE)
                            .withFilter(Expression.not(isCluster))
                            .withProperties(
                                textField(Expression.get("title")),
                                textSize(12f),
                                textColor("#212121"),
                                textHaloColor("#FFFFFF"),
                                textHaloWidth(1.5f),
                                textAnchor(TEXT_ANCHOR_TOP),
                                textOffset(arrayOf(0f, 1f)),
                                textOptional(true)
                            )
                    )

                    // Сигнал остальным блокам ниже: стиль готов, можно работать со слоями.
                    styleReady = true
                }
            }
        }
    }

    // Жизненный цикл.
    // MapView (старый View) сам не знает о жизненном цикле Compose, поэтому события экрана
    // пробрасываются в него вручную. Наблюдатель снимается при уходе компонента с экрана.
    // Замечание: onDestroy у MapView вызывается только по ON_DESTROY владельца жизненного
    // цикла (экрана целиком); если компонент просто уйдёт из композиции раньше, MapView
    // уничтожен не будет.
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Удаление дублей и отрисовка мест
    LaunchedEffect(places, styleReady) {
        if (!styleReady) return@LaunchedEffect

        val uniquePlaces = mutableListOf<PlaceItem>()

        places.forEach { place ->
            val lat = place.latitude ?: return@forEach
            val lon = place.longitude ?: return@forEach
            val title = place.title
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotEmpty() }
                ?: return@forEach

            val isDuplicate = uniquePlaces.any { existing ->
                val existingTitle = existing.title
                    ?.trim()
                    ?.lowercase()

                if (existingTitle != title) {
                    false
                } else {
                    val existingLat = existing.latitude ?: return@any false
                    val existingLon = existing.longitude ?: return@any false

                    distanceMeters(
                        lat,
                        lon,
                        existingLat,
                        existingLon
                    ) <= DUPLICATE_DISTANCE_METERS
                }
            }

            if (!isDuplicate) {
                uniquePlaces.add(place)
            }
        }

        val features = uniquePlaces.mapIndexed { index, place ->
            Feature.fromGeometry(
                Point.fromLngLat(
                    place.longitude!!,
                    place.latitude!!
                )
            ).apply {
                addNumberProperty("index", index)
                addStringProperty("title", place.title ?: "")
            }
        }

        map?.style
            ?.getSourceAs<GeoJsonSource>(PLACES_SOURCE)
            ?.setGeoJson(
                FeatureCollection.fromFeatures(features)
            )

        displayedPlaces = uniquePlaces
    }

    // Обработка нажатий по карте
    LaunchedEffect(styleReady) {
        val m = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        m.addOnMapClickListener { latLng ->
            val screenPoint = m.projection.toScreenLocation(latLng)

            // 1. Клик по кластеру
            val cluster = m.queryRenderedFeatures(
                screenPoint,
                CLUSTER_LAYER
            ).firstOrNull()

            if (cluster != null) {
                val zoom = m.cameraPosition.zoom + 2.0

                m.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, zoom),
                    400
                )

                return@addOnMapClickListener true
            }

            // 2. Клик по одиночной точке
            val hit = m.queryRenderedFeatures(
                screenPoint,
                PLACES_LAYER
            ).firstOrNull()

            val index = hit
                ?.getNumberProperty("index")
                ?.toInt()

            if (index != null) {
                currentDisplayedPlaces
                    .getOrNull(index)
                    ?.let(currentOnPlaceClick)

                true
            } else {
                currentOnMapClick()
                false
            }
        }
    }

    // Отслеживание центра и радиуса видимой области
    LaunchedEffect(styleReady) {
        val m = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        m.addOnCameraIdleListener {
            val center = m.cameraPosition.target
                ?: return@addOnCameraIdleListener

            val bounds = m.projection.visibleRegion.latLngBounds

            val radiusMeters = center
                .distanceTo(bounds.northEast)
                .toInt()
                .coerceIn(
                    MIN_RADIUS_METERS,
                    MAX_RADIUS_METERS
                )

            currentOnCameraCenterChanged(center, radiusMeters)
        }
    }

    // Включение геолокации MapLibre при наличии разрешения
    LaunchedEffect(isLocationGranted, styleReady) {
        val m = map ?: return@LaunchedEffect
        val style = m.style ?: return@LaunchedEffect

        if (!styleReady || !isLocationGranted) {
            return@LaunchedEffect
        }

        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission || hasCoarsePermission) {
            try {
                val locationComponent = m.locationComponent
                if (!locationComponent.isLocationComponentActivated) {
                    locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(
                            context,
                            style
                        ).build()
                    )
                }

                locationComponent.isLocationComponentEnabled = true
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.renderMode = RenderMode.NORMAL

            } catch (_: Exception) {
                // Геолокация недоступна — карта продолжает работать
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

// Расстояние между двумя координатами в метрах
private fun distanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    return 2 * earthRadius * asin(sqrt(a))
}
