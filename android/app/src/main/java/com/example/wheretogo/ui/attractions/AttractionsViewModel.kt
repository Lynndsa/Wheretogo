
package com.example.wheretogo.ui.attractions

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.RetrofitClient
import com.example.wheretogo.ui.map.getDeviceLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.collections.distinctBy
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class AttractionsViewModel(application: Application) :
    AndroidViewModel(application) {

    var attractions by mutableStateOf<List<PlaceItem>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedAttraction by mutableStateOf<PlaceItem?>(null)
        private set

    var isAttractionLoading by mutableStateOf(false)
        private set

    var attractionError by mutableStateOf<String?>(null)
        private set

    val displayedPlaces: List<PlaceItem>
        get() = attractions

    private var searchJob: Job? = null

    init {
        startLoading()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    private fun startLoading() {
        viewModelScope.launch {
            while (isActive) {

                if (searchQuery.isBlank() && searchJob == null) {
                    loadAttractions()
                }

                delay(5000.milliseconds)
            }
        }
    }

    private suspend fun loadAttractions() {
        try {
            isLoading = attractions.isEmpty()

            val hasPermission = ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        getApplication(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                errorMessage = "Нет разрешения на геолокацию"
                return
            }

            val location = withTimeoutOrNull(10_000.milliseconds) {
                getDeviceLocation(getApplication())
            }

            if (location == null) {
                Log.e("ATTRACTIONS", "Координаты не получены за 10 секунд")
                errorMessage = "Не удалось определить местоположение"
                return
            }

            val (lat, lon) = location
            Log.d("ATTRACTIONS", "Координаты: $lat, $lon")

            val response = RetrofitClient.apiService.getNearbyPlaces(
                lat = lat,
                lon = lon,
                radius = 5000
            )
            if (response.isSuccessful) {

                val receivedPlaces = response.body().orEmpty()

                attractions = receivedPlaces
                    .distinctBy {
                        it.title
                            ?.trim()
                            ?.lowercase()
                            ?.replace(Regex("\\s+"), " ")
                    }
                    .distinctBy {
                        Pair(it.latitude, it.longitude)
                    }

                errorMessage = null

                receivedPlaces.take(5).forEach {
                    Log.d(
                        "ATTRACTIONS",
                        "Название: ${it.title}, Фото: ${it.imageUrl}"
                    )
                }

            } else {
                errorMessage =
                    "Не удалось загрузить места: ${response.code()}"
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e(
                "ATTRACTIONS",
                "Ошибка загрузки мест",
                e
            )

            errorMessage = "Ошибка загрузки достопримечательностей"

        } finally {
            isLoading = false
        }
    }

    fun searchPlaces(query: String) {

        searchQuery = query

        searchJob?.cancel()

        if (query.isBlank()) {
            attractions = emptyList()
            searchJob = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(400.milliseconds)

            try {
                isLoading = true
                errorMessage = null

                val response = RetrofitClient.apiService
                    .searchPlaces(query.trim())

                if (response.isSuccessful) {

                    attractions = response.body().orEmpty()

                    attractions.take(5).forEach {
                        Log.d(
                            "ATTRACTIONS_SEARCH",
                            "Название: ${it.title}, Фото: ${it.imageUrl}"
                        )
                    }

                } else {
                    errorMessage = "Ошибка поиска: ${response.code()}"
                    attractions = emptyList()
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "ATTRACTIONS_SEARCH",
                    "Ошибка поиска достопримечательностей",
                    e
                )

                errorMessage = "Не удалось выполнить поиск"
                attractions = emptyList()

            } finally {
                isLoading = false
            }
        }
    }

    fun loadAttraction(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                isAttractionLoading = true
                attractionError = null
                selectedAttraction = null

                val response = RetrofitClient.apiService
                    .getPlaceDetails(lat, lon)

                if (response.isSuccessful) {
                    selectedAttraction = response.body()

                    if (selectedAttraction == null) {
                        attractionError =
                            "Данные достопримечательности отсутствуют"
                    }
                } else {
                    attractionError =
                        "Не удалось загрузить достопримечательность: ${response.code()}"
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "ATTRACTION_DETAILS",
                    "Ошибка загрузки",
                    e
                )

                attractionError = "Ошибка загрузки достопримечательности"

            } finally {
                isAttractionLoading = false
            }
        }
    }
    // Выбор места (с карты или из списка): данные уже есть, запрос не нужен
    fun selectAttraction(place: PlaceItem) {
        selectedAttraction = place
        attractionError = null
        isAttractionLoading = false
    }

    // Запасной вариант: грузим с сервера, только если нужного места нет в памяти
    fun loadAttractionIfNeeded(lat: Double, lon: Double) {
        val current = selectedAttraction
        val curLat = current?.latitude
        val curLon = current?.longitude
        if (curLat != null && curLon != null &&
            kotlin.math.abs(curLat - lat) < 1e-6 &&
            kotlin.math.abs(curLon - lon) < 1e-6
        ) {
            return
        }
        loadAttraction(lat, lon)
    }
}
