package com.example.wheretogo.tools

import android.content.Context
import android.util.Log
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition

class OfflineMapManager(private val context: Context) {

    // Гарантируем инициализацию MapLibre перед вызовом OfflineManager
    private val offlineManager: OfflineManager by lazy {
        MapLibre.getInstance(context)
        OfflineManager.getInstance(context)
    }

    fun downloadRegion(
        styleUrl: String,
        regionName: String,
        bounds: LatLngBounds,
        minZoom: Double = 9.0,
        maxZoom: Double = 13.0,
        onProgress: (Int) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val pixelDensity = context.resources.displayMetrics.density

            val definition = OfflineTilePyramidRegionDefinition(
                styleUrl,
                bounds,
                minZoom,
                maxZoom,
                pixelDensity
            )

            val metadata = regionName.toByteArray(Charsets.UTF_8)

            offlineManager.createOfflineRegion(
                definition,
                metadata,
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: OfflineRegion) {
                        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                            override fun onStatusChanged(status: OfflineRegionStatus) {
                                val percentage = if (status.requiredResourceCount > 0) {
                                    (100.0 * status.completedResourceCount / status.requiredResourceCount).toInt()
                                } else 0

                                onProgress(percentage)

                                if (status.isComplete) {
                                    Log.d("OFFLINE_MAP", "Карта успешно загружена!")
                                    onSuccess()
                                }
                            }

                            override fun onError(error: OfflineRegionError) {
                                Log.e("OFFLINE_MAP", "Ошибка кэширования: ${error.reason}")
                                onError(error.reason)
                            }

                            override fun mapboxTileCountLimitExceeded(limit: Long) {
                                Log.w("OFFLINE_MAP", "Превышен лимит тайлов: $limit")
                            }
                        })

                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                    }

                    override fun onError(error: String) {
                        Log.e("OFFLINE_MAP", "Ошибка создания региона: $error")
                        onError(error)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("OFFLINE_MAP", "Исключение при запуске загрузки: ${e.message}")
            onError(e.message ?: "Unknown error")
        }
    }
}