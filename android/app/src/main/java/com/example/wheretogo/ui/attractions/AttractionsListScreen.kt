package com.example.wheretogo.ui.attractions

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.example.wheretogo.data.model.PlaceItem
import com.example.wheretogo.ui.theme.BackgroundColor
import com.example.wheretogo.ui.theme.ButtonColor
import okhttp3.OkHttpClient

@Composable
fun AttractionsListScreen(
    onPlaceClick: (PlaceItem) -> Unit = {},
    attractionsViewModel: AttractionsViewModel = viewModel()
) {
    val context = LocalContext.current

    val imageLoader = remember {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "WhereToGo/1.0 (Android; image loading)"
                    )
                    .build()

                chain.proceed(request)
            }
            .build()

        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .build()
    }

    val searchQuery = attractionsViewModel.searchQuery
    val displayedPlaces = attractionsViewModel.displayedPlaces
    val isLoading = attractionsViewModel.isLoading
    val errorMessage = attractionsViewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                attractionsViewModel.searchPlaces(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Поиск достопримечательностей")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ButtonColor,
                unfocusedBorderColor = ButtonColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {

            isLoading && displayedPlaces.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ButtonColor)
                }
            }

            errorMessage != null && displayedPlaces.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            displayedPlaces.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Ничего не найдено"
                        } else {
                            "Поблизости пока нет мест"
                        },
                        color = ButtonColor,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = displayedPlaces,
                        key = {
                            "${it.title}_${it.latitude}_${it.longitude}"
                        }
                    ) { place ->

                        AttractionCard(
                            place = place,
                            imageLoader = imageLoader,
                            onClick = {
                                onPlaceClick(place)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttractionCard(
    place: PlaceItem,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SubcomposeAsyncImage(
            model = place.imageUrl,
            imageLoader = imageLoader,
            contentDescription = place.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ButtonColor)
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Изображение недоступно",
                        color = ButtonColor
                    )
                }
            },
            onError = { state ->
                Log.e(
                    "IMAGE_ERROR",
                    "Не удалось загрузить: ${place.imageUrl}",
                    state.result.throwable
                )
            }
        )

        Text(
            text = place.title ?: "Без названия",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = ButtonColor,
            textAlign = TextAlign.Center
        )
    }
}