
package com.example.wheretogo.ui.attractions

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheretogo.data.local.SessionManager
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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

    fun isFavorite(place: PlaceItem) = place.id in favoriteIds
    private val appContext = application.applicationContext

    // id текущего пользователя, обновляется автоматически при входе/выходе
    private var userId: Long? = null

    init {
        loadAttractions()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            SessionManager.getUserId(appContext)
                .distinctUntilChanged()
                .collectLatest { id ->
                    userId = id
                    favoriteIds = emptySet()      // сбрасываем чужое избранное
                    if (id != null) fetchFavorites(id)
                }
        }
    }

    private suspend fun fetchFavorites(uid: Long) {
        try {
            val response = RetrofitClient.apiService.getFavorites(uid)
            if (response.isSuccessful) {
                favoriteIds = response.body().orEmpty().toSet()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // не критично: просто не покажем сердечки
        }
    }
    fun toggleFavorite(place: PlaceItem) {
        val uid = userId ?: run {
            Log.e("FAV_ERROR", "userId равен null! Пользователь не авторизован.")
            return
        }
        val id = place.id
        val wasFavorite = id in favoriteIds

        // Оптимистично меняем UI
        favoriteIds = if (wasFavorite) favoriteIds - id else favoriteIds + id

        viewModelScope.launch {
            try {
                val response = if (wasFavorite) {
                    RetrofitClient.apiService.removeFavorite(uid, id)
                } else {
                    RetrofitClient.apiService.addFavorite(uid, id)
                }

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FAV_ERROR", "Ошибка сервера: HTTP ${response.code()} | Body: $errorBody")
                    rollback(id, wasFavorite)
                } else {
                    Log.d("FAV_SUCCESS", "Избранное успешно обновлено на сервере")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("FAV_ERROR", "Исключение при запросе избранного", e)
                rollback(id, wasFavorite)
            }
        }
    }

    private fun rollback(id: Long, wasFavorite: Boolean) {
        favoriteIds = if (wasFavorite) favoriteIds + id else favoriteIds - id
    }
}
