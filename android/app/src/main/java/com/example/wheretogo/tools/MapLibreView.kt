package com.example.wheretogo.tools

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Property
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
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.Property.*

private const val PLACES_SOURCE = "places-source"
private const val PLACES_LAYER = "places-layer"

private const val LABELS_LAYER = "places-labels-layer"
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(59.9386, 30.3141),
    initialZoom: Double = 11.0,
    isLocationGranted: Boolean = false,
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

                    style.addSource(GeoJsonSource(PLACES_SOURCE))
                    style.addLayer(
                        CircleLayer(PLACES_LAYER, PLACES_SOURCE).withProperties(
                            circleRadius(8f),
                            circleColor("#E53935"),
                            circleStrokeWidth(2f),
                            circleStrokeColor("#FFFFFF")
                        )
                    )
                    style.addLayer(
                        SymbolLayer(LABELS_LAYER, PLACES_SOURCE).withProperties(
                            textField(Expression.get("title")),
                            textSize(12f),
                            textColor("#212121"),
                            textHaloColor("#FFFFFF"),
                            textHaloWidth(1.5f),
                            textAnchor(TEXT_ANCHOR_TOP),
                            textOffset(arrayOf(0f, 1f)),
                            textAllowOverlap(true),
                            textIgnorePlacement(true)
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
            }
            Feature.fromGeometry(Point.fromLngLat(lon, lat)).apply {
                addNumberProperty("index", index)
                addStringProperty("title", p.title ?: "")
            }
        }

        map?.style?.getSourceAs<GeoJsonSource>(PLACES_SOURCE)
            ?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    // Клик по точке
    LaunchedEffect(styleReady) {
        val m = map ?: return@LaunchedEffect
        if (!styleReady) return@LaunchedEffect
        m.addOnMapClickListener { latLng ->
            val screenPoint = m.projection.toScreenLocation(latLng)
            val hit = m.queryRenderedFeatures(screenPoint, PLACES_LAYER).firstOrNull()
            val index = hit?.getNumberProperty("index")?.toInt()
            if (index != null) {
                places.getOrNull(index)?.let(onPlaceClick)
                true
            } else false
        }
    }

    // Включение геолокации MapLibre при наличии разрешения
    LaunchedEffect(isLocationGranted, styleReady) {
        val m = map ?: return@LaunchedEffect
        val style = m.style ?: return@LaunchedEffect
        if (!styleReady || !isLocationGranted) return@LaunchedEffect

        val hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

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