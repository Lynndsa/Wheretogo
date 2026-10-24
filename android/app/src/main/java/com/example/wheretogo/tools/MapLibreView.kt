package com.example.wheretogo.tools

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.wheretogo.model.PlaceItem
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val PLACES_SOURCE = "places-source"
private const val PLACES_LAYER = "places-layer"

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(59.9386, 30.3141),
    initialZoom: Double = 11.0,
    onPlaceClick: (PlaceItem) -> Unit = {}
) {
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

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}