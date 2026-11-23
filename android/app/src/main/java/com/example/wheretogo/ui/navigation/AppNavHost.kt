
package com.example.wheretogo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wheretogo.Greeting
import com.example.wheretogo.data.local.SessionManager
import com.example.wheretogo.ui.auth.LoginWindow
import com.example.wheretogo.ui.auth.RegistrationWindow
import com.example.wheretogo.ui.attractions.AttractionScreen
import com.example.wheretogo.ui.attractions.AttractionsListScreen
import com.example.wheretogo.ui.attractions.AttractionsViewModel
import com.example.wheretogo.ui.map.MapWindow
import com.example.wheretogo.ui.profile.ProfileScreen
import kotlinx.coroutines.launch

private fun getScreenTitle(route: String?): String {
    return when (route) {
        Destination.Profile.route -> "ПРОФИЛЬ"
        Destination.Map.route -> "КАРТА"
        Destination.Places.route,
        Destination.AttractionDetails.route -> "ДОСТОПРИМЕЧАТЕЛЬНОСТИ"
        Destination.Wishlist.route -> "ХОЧУ СХОДИТЬ"
        Destination.MyRoutes.route -> "МОИ МАРШРУТЫ"
        Destination.ReadyRoutes.route -> "ГОТОВЫЕ МАРШРУТЫ"
        Destination.Settings.route -> "НАСТРОЙКИ"
        Destination.AddRoute.route -> "ДОБАВИТЬ МАРШРУТ"
        else -> ""
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val attractionsViewModel: AttractionsViewModel = viewModel()

    var menuOpen by remember {
        mutableStateOf(false)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val screenTitle = getScreenTitle(currentRoute)

    val showMenuButton = currentRoute != null && currentRoute !in listOf(
        Destination.Main.route,
        Destination.Login.route,
        Destination.Registration.route
    )

    val isLoggedIn by SessionManager
        .isLoggedIn(context)
        .collectAsState(initial = null)

    if (isLoggedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isLoggedIn == true) {
        Destination.Map.route
    } else {
        Destination.Main.route
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (showMenuButton) {
                AppTopBar(
                    title = screenTitle,
                    onMenuClick = {
                        menuOpen = true
                    }
                )
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f)
            ) {
                composable(route = Destination.Main.route) {
                    Greeting(
                        onLoginClick = {
                            navController.navigate(Destination.Login.route)
                        },
                        onGuestClick = {
                            navController.navigate(Destination.Registration.route)
                        }
                    )
                }

                composable(route = Destination.Registration.route) {
                    RegistrationWindow(
                        onLoginClick = {
                            navController.navigate(Destination.Login.route)
                        },
                        onRegisterSuccess = { idUser, login ->
                            scope.launch {
                                SessionManager.saveSession(context, idUser, login)

                                navController.navigate(Destination.Map.route) {
                                    popUpTo(Destination.Main.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(route = Destination.Login.route) {
                    LoginWindow(
                        onRedistrationClick = {
                            navController.navigate(Destination.Registration.route)
                        },
                        onLoginSuccess = { idUser, login ->
                            scope.launch {
                                SessionManager.saveSession(context, idUser, login)

                                navController.navigate(Destination.Map.route) {
                                    popUpTo(Destination.Main.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable(route = Destination.Profile.route) {
                    ProfileScreen()
                }

                composable(route = Destination.Map.route) {
                    MapWindow(
                        onPlaceClick = { place ->
                            attractionsViewModel.selectAttraction(place)

                            val lat = place.latitude
                            val lon = place.longitude

                            if (lat != null && lon != null) {
                                navController.navigate(
                                    Destination.AttractionDetails.createRoute(lat, lon)
                                )
                            }
                        }
                    )
                }

                composable(route = Destination.Places.route) {
                    AttractionsListScreen(
                        attractionsViewModel = attractionsViewModel,
                        onPlaceClick = { place ->
                            attractionsViewModel.selectAttraction(place)

                            val lat = place.latitude
                            val lon = place.longitude

                            if (lat != null && lon != null) {
                                navController.navigate(
                                    Destination.AttractionDetails.createRoute(lat, lon)
                                )
                            }
                        }
                    )
                }

                composable(route = Destination.AttractionDetails.route) {
                    val attraction = attractionsViewModel.selectedAttraction

                    when {
                        attraction != null -> {
                            AttractionScreen(
                                attraction = attraction,
                                isFavorite = attractionsViewModel.isFavorite(attraction),
                                onFavoriteClick = { attractionsViewModel.toggleFavorite(attraction) }
                            )
                        }

                        attractionsViewModel.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Достопримечательность не найдена")
                            }
                        }
                    }
                }

                composable(route = Destination.Wishlist.route) {
                    Placeholder("Хочу сходить")
                }

                composable(route = Destination.MyRoutes.route) {
                    Placeholder("Мои маршруты")
                }

                composable(route = Destination.ReadyRoutes.route) {
                    Placeholder("Готовые маршруты")
                }

                composable(route = Destination.Settings.route) {
                    Placeholder("Настройки")
                }

                composable(route = Destination.AddRoute.route) {
                    Placeholder("Добавить маршрут")
                }
            }
        }

        SideMenu(
            visible = menuOpen,
            onDismiss = {
                menuOpen = false
            },
            onItemClick = { route ->
                menuOpen = false

                navController.navigate(route) {
                    popUpTo(Destination.Map.route)
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun Placeholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title)
    }
}
