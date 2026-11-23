
package com.example.wheretogo.ui.attractions

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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

    init {
        loadAttractions()
        loadFavorites()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    private fun loadAttractions() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val response = RetrofitClient.apiService.getDataBase()

                if (response.isSuccessful) {
                    attractions = response.body().orEmpty()
                    errorMessage = null
                } else {
                    errorMessage = "Не удалось загрузить места: ${response.code()}"
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки достопримечательностей"
            } finally {
                isLoading = false
            }
        }
    }

    // Выбор места из уже загруженного списка
    fun selectAttraction(place: PlaceItem) {
        selectedAttraction = place
        attractionError = null
        isAttractionLoading = false
    }

    // Сброс выбранной достопримечательности
    fun clearSelectedAttraction() {
        selectedAttraction = null
        attractionError = null
        isAttractionLoading = false
    }

    var favoriteIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    // TODO: подставь id реального авторизованного пользователя
    private val userId: Long = 1L

    fun isFavorite(place: PlaceItem) = place.id in favoriteIds

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getFavorites(userId)
                if (response.isSuccessful) {
                    favoriteIds = response.body().orEmpty().toSet()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // не критично: просто не покажем сердечки
            }
        }
    }

    fun toggleFavorite(place: PlaceItem) {
        val id = place.id
        val wasFavorite = id in favoriteIds

        // сразу обновляем UI, не ждём ответа сервера
        favoriteIds = if (wasFavorite) favoriteIds - id else favoriteIds + id

        viewModelScope.launch {
            try {
                val response = if (wasFavorite) {
                    RetrofitClient.apiService.removeFavorite(userId, id)
                } else {
                    RetrofitClient.apiService.addFavorite(userId, id)
                }
                if (!response.isSuccessful) rollback(id, wasFavorite)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                rollback(id, wasFavorite)
            }
        }
    }

    // откат, если сервер не принял изменение
    private fun rollback(id: Long, wasFavorite: Boolean) {
        favoriteIds = if (wasFavorite) favoriteIds + id else favoriteIds - id
    }
}
