
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

// Идентификаторы источника данных и слоёв карты.
private const val PLACES_SOURCE = "places-source"
private const val PLACES_LAYER = "places-layer"
private const val LABELS_LAYER = "places-labels-layer"
private const val CLUSTER_LAYER = "places-cluster-layer"
private const val CLUSTER_COUNT_LAYER = "places-cluster-count-layer"

// Начальные координаты карты.
private const val DEFAULT_LATITUDE = 59.9386
private const val DEFAULT_LONGITUDE = 30.3141

// Ограничения радиуса видимой области.
private const val MIN_RADIUS_METERS = 1000
private const val MAX_RADIUS_METERS = 50000
private const val DUPLICATE_DISTANCE_METERS = 30.0

// Настройки кластеризации.
private const val CLUSTER_RADIUS = 60
private const val CLUSTER_MAX_ZOOM = 14
private const val SMALL_CLUSTER_LIMIT = 10
private const val LARGE_CLUSTER_LIMIT = 50

// Размеры и оформление кластеров.
private const val SMALL_CLUSTER_RADIUS = 16f
private const val MEDIUM_CLUSTER_RADIUS = 22f
private const val LARGE_CLUSTER_RADIUS = 28f
private const val CLUSTER_STROKE_WIDTH = 2f
private const val CLUSTER_TEXT_SIZE = 13f

// Оформление одиночных точек.
private const val PLACE_RADIUS = 8f
private const val PLACE_STROKE_WIDTH = 2f
private const val PLACE_LABEL_TEXT_SIZE = 12f
private const val PLACE_LABEL_HALO_WIDTH = 1.5f

// Анимация приближения к кластеру.
private const val CLUSTER_ZOOM_INCREMENT = 2.0
private const val CLUSTER_ANIMATION_DURATION_MS = 400

// Расчёт расстояния.
private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Компонент карты на основе MapLibre.
 *
 * Компонент не загружает данные с сервера и не запрашивает разрешения.
 * Данные и события передаются через параметры и callback-функции.
 */
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(
        DEFAULT_LATITUDE,
        DEFAULT_LONGITUDE
    ),
    initialZoom: Double = 11.0,
    isLocationGranted: Boolean = false,
    onCameraCenterChanged: (LatLng, Int) -> Unit = { _, _ -> },
    onPlaceClick: (PlaceItem) -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    if (LocalInspectionMode.current) {
        MapPreview(modifier)
        return
    }

    val context = LocalContext.current

    val currentOnPlaceClick by rememberUpdatedState(onPlaceClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnCameraCenterChanged by rememberUpdatedState(onCameraCenterChanged)

    var displayedPlaces by remember {
        mutableStateOf(emptyList<PlaceItem>())
    }
    val currentDisplayedPlaces by rememberUpdatedState(displayedPlaces)

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

    val mapView = remember {
        createMapView(
            context = context,
            styleUrl = styleUrl,
            initialCenter = initialCenter,
            initialZoom = initialZoom,
            onMapReady = { readyMap ->
                map = readyMap
            },
            onStyleReady = {
                styleReady = true
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    MapLifecycleEffect(
        lifecycleOwner = lifecycleOwner,
        mapView = mapView
    )

    LaunchedEffect(places, styleReady) {
        if (styleReady) {
            displayedPlaces = updatePlaces(
                map = map,
                places = places
            )
        }
    }

    LaunchedEffect(styleReady, map) {
        val currentMap = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        registerMapClickListener(
            map = currentMap,
            displayedPlaces = { currentDisplayedPlaces },
            onPlaceClick = { currentOnPlaceClick(it) },
            onMapClick = { currentOnMapClick() }
        )
    }

    LaunchedEffect(styleReady, map) {
        val currentMap = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect

        registerCameraIdleListener(
            map = currentMap,
            onCameraCenterChanged = {
                currentOnCameraCenterChanged(it.first, it.second)
            }
        )
    }

    LaunchedEffect(isLocationGranted, styleReady, map) {
        val currentMap = map ?: return@LaunchedEffect
        if (!styleReady || !isLocationGranted) return@LaunchedEffect

        enableLocationComponent(
            map = currentMap,
            context = context
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun MapPreview(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Map Preview")
    }
}

private fun createMapView(
    context: android.content.Context,
    styleUrl: String,
    initialCenter: LatLng,
    initialZoom: Double,
    onMapReady: (MapLibreMap) -> Unit,
    onStyleReady: () -> Unit
): MapView {
    return MapView(context).apply {
        onCreate(null)

        getMapAsync { map ->
            onMapReady(map)

            map.cameraPosition = CameraPosition.Builder()
                .target(initialCenter)
                .zoom(initialZoom)
                .build()

            map.setStyle(styleUrl) { style ->
                configureMapStyle(style)
                onStyleReady()
            }
        }
    }
}

private fun configureMapStyle(
    style: org.maplibre.android.maps.Style
) {
    style.addSource(
        GeoJsonSource(
            PLACES_SOURCE,
            GeoJsonOptions()
                .withCluster(true)
                .withClusterRadius(CLUSTER_RADIUS)
                .withClusterMaxZoom(CLUSTER_MAX_ZOOM)
        )
    )

    addClusterLayers(style)
    addPlaceLayers(style)
}

private fun addClusterLayers(
    style: org.maplibre.android.maps.Style
) {
    val isCluster = Expression.has("point_count")

    style.addLayer(
        CircleLayer(CLUSTER_LAYER, PLACES_SOURCE)
            .withFilter(isCluster)
            .withProperties(
                circleColor(
                    Expression.step(
                        Expression.get("point_count"),
                        Expression.color("#42A5F5".toColorInt()),
                        Expression.stop(
                            SMALL_CLUSTER_LIMIT,
                            Expression.color("#FB8C00".toColorInt())
                        ),
                        Expression.stop(
                            LARGE_CLUSTER_LIMIT,
                            Expression.color("#E53935".toColorInt())
                        )
                    )
                ),
                circleRadius(
                    Expression.step(
                        Expression.get("point_count"),
                        Expression.literal(SMALL_CLUSTER_RADIUS),
                        Expression.stop(
                            SMALL_CLUSTER_LIMIT,
                            MEDIUM_CLUSTER_RADIUS
                        ),
                        Expression.stop(
                            LARGE_CLUSTER_LIMIT,
                            LARGE_CLUSTER_RADIUS
                        )
                    )
                ),
                circleStrokeWidth(CLUSTER_STROKE_WIDTH),
                circleStrokeColor("#FFFFFF")
            )
    )

    style.addLayer(
        SymbolLayer(CLUSTER_COUNT_LAYER, PLACES_SOURCE)
            .withFilter(isCluster)
            .withProperties(
                textField(
                    Expression.toString(
                        Expression.get("point_count_abbreviated")
                    )
                ),
                textSize(CLUSTER_TEXT_SIZE),
                textColor("#FFFFFF"),
                textAllowOverlap(true),
                textIgnorePlacement(true)
            )
    )
}

private fun addPlaceLayers(
    style: org.maplibre.android.maps.Style
) {
    val isNotCluster = Expression.not(
        Expression.has("point_count")
    )

    style.addLayer(
        CircleLayer(PLACES_LAYER, PLACES_SOURCE)
            .withFilter(isNotCluster)
            .withProperties(
                circleRadius(PLACE_RADIUS),
                circleColor("#E53935"),
                circleStrokeWidth(PLACE_STROKE_WIDTH),
                circleStrokeColor("#FFFFFF")
            )
    )

    style.addLayer(
        SymbolLayer(LABELS_LAYER, PLACES_SOURCE)
            .withFilter(isNotCluster)
            .withProperties(
                textField(Expression.get("title")),
                textSize(PLACE_LABEL_TEXT_SIZE),
                textColor("#212121"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(PLACE_LABEL_HALO_WIDTH),
                textAnchor(TEXT_ANCHOR_TOP),
                textOffset(arrayOf(0f, 1f)),
                textOptional(true)
            )
    )
}

@Composable
private fun MapLifecycleEffect(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    mapView: MapView
) {
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private fun updatePlaces(
    map: MapLibreMap?,
    places: List<PlaceItem>
): List<PlaceItem> {
    val uniquePlaces = removeDuplicatePlaces(places)

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

    return uniquePlaces
}

private fun removeDuplicatePlaces(
    places: List<PlaceItem>
): List<PlaceItem> {
    val uniquePlaces = mutableListOf<PlaceItem>()

    places.forEach { place ->
        val latitude = place.latitude ?: return@forEach
        val longitude = place.longitude ?: return@forEach
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
                val existingLatitude = existing.latitude ?: return@any false
                val existingLongitude = existing.longitude ?: return@any false

                distanceMeters(
                    latitude,
                    longitude,
                    existingLatitude,
                    existingLongitude
                ) <= DUPLICATE_DISTANCE_METERS
            }
        }

        if (!isDuplicate) {
            uniquePlaces.add(place)
        }
    }

    return uniquePlaces
}

private fun registerMapClickListener(
    map: MapLibreMap,
    displayedPlaces: () -> List<PlaceItem>,
    onPlaceClick: (PlaceItem) -> Unit,
    onMapClick: () -> Unit
) {
    map.addOnMapClickListener { latLng ->
        val screenPoint = map.projection.toScreenLocation(latLng)

        val cluster = map.queryRenderedFeatures(
            screenPoint,
            CLUSTER_LAYER
        ).firstOrNull()

        if (cluster != null) {
            zoomIntoCluster(map, latLng)
            return@addOnMapClickListener true
        }

        val hit = map.queryRenderedFeatures(
            screenPoint,
            PLACES_LAYER
        ).firstOrNull()

        val index = hit
            ?.getNumberProperty("index")
            ?.toInt()

        if (index != null) {
            displayedPlaces()
                .getOrNull(index)
                ?.let(onPlaceClick)

            true
        } else {
            onMapClick()
            false
        }
    }
}

private fun zoomIntoCluster(
    map: MapLibreMap,
    latLng: LatLng
) {
    val zoom = map.cameraPosition.zoom + CLUSTER_ZOOM_INCREMENT

    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(latLng, zoom),
        CLUSTER_ANIMATION_DURATION_MS
    )
}

private fun registerCameraIdleListener(
    map: MapLibreMap,
    onCameraCenterChanged: (Pair<LatLng, Int>) -> Unit
) {
    map.addOnCameraIdleListener {
        val center = map.cameraPosition.target
            ?: return@addOnCameraIdleListener

        val bounds = map.projection.visibleRegion.latLngBounds

        val radiusMeters = center
            .distanceTo(bounds.northEast)
            .toInt()
            .coerceIn(
                MIN_RADIUS_METERS,
                MAX_RADIUS_METERS
            )

        onCameraCenterChanged(center to radiusMeters)
    }
}

private fun enableLocationComponent(
    map: MapLibreMap,
    context: android.content.Context
) {
    val style = map.style ?: return

    val hasFinePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarsePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasFinePermission && !hasCoarsePermission) {
        return
    }

    try {
        val locationComponent = map.locationComponent

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
        // Геолокация недоступна — карта продолжает работать.
    }
}

// Расстояние между двумя координатами в метрах.
private fun distanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}

fun calculateOrderStatus(
    isPaid: Boolean,
    isDelivered: Boolean,
    isCancelled: Boolean,
    hasItems: Boolean,
    stockAvailable: Boolean,
    isExpress: Boolean,
    isWeekend: Boolean,
    customerVerified: Boolean
): String {
    if (isCancelled) {
        return "Cancelled"
    }

    if (!hasItems) {
        return "Empty order"
    }

    if (!customerVerified) {
        return "Verification required"
    }

    if (!stockAvailable) {
        return "Out of stock"
    }

    if (!isPaid) {
        return "Payment pending"
    }

    if (isDelivered) {
        return "Delivered"
    }

    if (isExpress && isWeekend) {
        return "Express weekend delivery"
    }

    if (isExpress && !isWeekend) {
        return "Express delivery"
    }

    if (!isExpress && isWeekend) {
        return "Weekend delivery"
    }

    var priority = 0

    for (i in 1..5) {
        if (i % 2 == 0) {
            priority++
        } else {
            priority--
        }
    }

    when {
        priority > 0 && isPaid -> return "High priority"
        priority == 0 && stockAvailable -> return "Normal priority"
        priority < 0 && customerVerified -> return "Low priority"
        else -> return "Standard processing"
    }
}
