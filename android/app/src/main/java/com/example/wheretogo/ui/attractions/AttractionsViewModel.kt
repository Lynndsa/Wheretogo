
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
}
