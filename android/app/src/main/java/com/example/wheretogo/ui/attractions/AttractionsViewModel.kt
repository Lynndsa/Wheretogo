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

    val displayedPlaces: List<PlaceItem>
        get() = attractions

    private val api = RetrofitClient.createApi(
        "http://192.168.31.46:8080/"
    )
    //    private val api = RetrofitClient.createApi(
//        "https://wheretogo-00tp.onrender.com/"
//    )

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

                delay(5000)
            }
        }
    }

    private suspend fun loadAttractions() {
        try {
            isLoading = attractions.isEmpty()

            val (lat, lon) = getDeviceLocation(
                getApplication()
            )

            val response = api.getNearbyPlaces(
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

            delay(400)

            try {
                isLoading = true
                errorMessage = null

                val response = api.searchPlaces(query.trim())

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
}