package com.example.wheretogo.tools

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.wheretogo.model.PlaceItem
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    places: List<PlaceItem> = emptyList(),
    initialCenter: LatLng = LatLng(59.9386, 30.3141), // Координаты СПб по умолчанию
    initialZoom: Double = 11.0,
    onPlaceClick: (PlaceItem) -> Unit = {}
) {
    val context = LocalContext.current

    // Инициализация MapLibre singleton
    remember {
        MapLibre.getInstance(context).also {
            MapLibre.setConnected(true)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                Log.d("MAP_TEST", "MapView готов, пробуем загрузить стиль: $styleUrl")

                map.setStyle(styleUrl) { style ->
                    Log.d("MAP_TEST", "УСПЕХ! Стиль успешно загружен!")
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(initialCenter)
                    .zoom(initialZoom)
                    .build()

                val markerToPlaceMap = mutableMapOf<Marker, PlaceItem>()
                places.forEach { place ->
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(place.latLng)
                            .title(place.title)
                            .snippet(place.description)
                    )
                    if (marker != null) {
                        markerToPlaceMap[marker] = place
                    }
                }

                map.setOnMarkerClickListener { marker ->
                    markerToPlaceMap[marker]?.let { place ->
                        onPlaceClick(place)
                        true
                    } ?: false
                }
            }
        }
    }

    // Управление жизненным циклом MapView
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
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}