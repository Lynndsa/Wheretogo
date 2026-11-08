package com.example.wheretogo.tools

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.wheretogo.model.PlaceItem
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
import androidx.core.graphics.toColorInt

private const val PLACES_SOURCE = "places-source"
private const val PLACES_LAYER = "places-layer"                       // одиночные точки
private const val LABELS_LAYER = "places-labels-layer"                // подписи одиночных точек
private const val CLUSTER_LAYER = "places-cluster-layer"              // кружки кластеров
private const val CLUSTER_COUNT_LAYER = "places-cluster-count-layer"  // число в кластере

private const val MIN_RADIUS_METERS = 1000
private const val MAX_RADIUS_METERS = 50000

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(59.9386, 30.3141),
    initialZoom: Double = 11.0,
    isLocationGranted: Boolean = false,
    /** Центр карты и радиус видимой области в метрах */
    onCameraCenterChanged: (LatLng, Int) -> Unit = { _, _ -> },
    onPlaceClick: (PlaceItem) -> Unit = {}
) {
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

    // Всегда актуальные значения для слушателей, которые регистрируются один раз
    val currentPlaces by rememberUpdatedState(places)
    val currentOnPlaceClick by rememberUpdatedState(onPlaceClick)
    val currentOnCameraCenterChanged by rememberUpdatedState(onCameraCenterChanged)

    remember {
        MapLibre.getInstance(context).also { MapLibre.setConnected(true) }
    }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { m ->
                map = m
                m.cameraPosition = CameraPosition.Builder()
                    .target(initialCenter)
                    .zoom(initialZoom)
                    .build()

                m.setStyle(styleUrl) { style ->
                    Log.d("MAP_TEST", "Стиль загружен")

                    // Источник с кластеризацией
                    style.addSource(
                        GeoJsonSource(
                            PLACES_SOURCE,
                            GeoJsonOptions()
                                .withCluster(true)
                                .withClusterRadius(60)   // радиус объединения в пикселях
                                .withClusterMaxZoom(14)  // с зума 15 кластеров уже нет
                        )
                    )

                    val isCluster = Expression.has("point_count")

                    // Кружки кластеров: цвет и размер зависят от количества
                    style.addLayer(
                        CircleLayer(CLUSTER_LAYER, PLACES_SOURCE)
                            .withFilter(isCluster)
                            .withProperties(
                                circleColor(
                                    Expression.step(
                                        Expression.get("point_count"),
                                        Expression.color("#42A5F5".toColorInt()),
                                        Expression.stop(10, Expression.color("#FB8C00".toColorInt())),
                                        Expression.stop(50, Expression.color("#E53935".toColorInt()))
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

                    // Число объектов внутри кластера
                    // Если цифры не видны, добавьте textFont(arrayOf("Noto Sans Regular"))
                    // (название должно совпадать со шрифтом из стиля)
                    style.addLayer(
                        SymbolLayer(CLUSTER_COUNT_LAYER, PLACES_SOURCE)
                            .withFilter(isCluster)
                            .withProperties(
                                textField(Expression.toString(Expression.get("point_count_abbreviated"))),
                                textSize(13f),
                                textColor("#FFFFFF"),
                                textAllowOverlap(true),
                                textIgnorePlacement(true)
                            )
                    )

                    // Одиночные точки (не кластеры)
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

                    // Подписи одиночных точек (не накладываются друг на друга)
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

                    styleReady = true
                }
            }
        }
    }

    // Жизненный цикл
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Обновление точек при изменении списка
    LaunchedEffect(places, styleReady) {
        if (!styleReady) return@LaunchedEffect

        val features = places.mapIndexedNotNull { index, p ->
            val lat = p.latitude ?: return@mapIndexedNotNull null
            val lon = p.longitude ?: return@mapIndexedNotNull null

            Feature.fromGeometry(Point.fromLngLat(lon, lat)).apply {
                addNumberProperty("index", index)
                addStringProperty("title", p.title ?: "")
            }
        }

        map?.style?.getSourceAs<GeoJsonSource>(PLACES_SOURCE)
            ?.setGeoJson(FeatureCollection.fromFeatures(features))

        Log.d("MAP_TEST", "Отрисовано точек: ${features.size}")
    }

    // Клик: по кластеру приближаем, по точке открываем место
    LaunchedEffect(styleReady) {
        val m = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        m.addOnMapClickListener { latLng ->
            val screenPoint = m.projection.toScreenLocation(latLng)

            // 1. Клик по кластеру
            val cluster = m.queryRenderedFeatures(screenPoint, CLUSTER_LAYER).firstOrNull()
            if (cluster != null) {
                val zoom = m.cameraPosition.zoom + 2.0
                m.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), 400)
                return@addOnMapClickListener true
            }

            // 2. Клик по одиночной точке
            val hit = m.queryRenderedFeatures(screenPoint, PLACES_LAYER).firstOrNull()
            val index = hit?.getNumberProperty("index")?.toInt()
            if (index != null) {
                currentPlaces.getOrNull(index)?.let(currentOnPlaceClick)
                true
            } else {
                false
            }
        }
    }

    // Отслеживание центра и радиуса видимой области
    LaunchedEffect(styleReady) {
        val m = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        m.addOnCameraIdleListener {
            val center = m.cameraPosition.target ?: return@addOnCameraIdleListener
            val bounds = m.projection.visibleRegion.latLngBounds
            val radiusMeters = center.distanceTo(bounds.northEast).toInt()
                .coerceIn(MIN_RADIUS_METERS, MAX_RADIUS_METERS)
            currentOnCameraCenterChanged(center, radiusMeters)
        }
    }

    // Включение геолокации MapLibre при наличии разрешения
    LaunchedEffect(isLocationGranted, styleReady) {
        val m = map ?: return@LaunchedEffect
        val style = m.style ?: return@LaunchedEffect
        if (!styleReady || !isLocationGranted) return@LaunchedEffect

        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission || hasCoarsePermission) {
            try {
                val locationComponent = m.locationComponent
                if (!locationComponent.isLocationComponentActivated) {
                    locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(context, style).build()
                    )
                }
                locationComponent.isLocationComponentEnabled = true
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.renderMode = RenderMode.NORMAL
            } catch (e: Exception) {
                Log.e("MAP_TEST", "Ошибка включения геолокации: ${e.message}")
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}