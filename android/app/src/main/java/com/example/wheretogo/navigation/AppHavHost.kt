
package com.example.wheretogo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wheretogo.Greeting
import com.example.wheretogo.LoginWindow
import com.example.wheretogo.RegistrationWindow
import com.example.wheretogo.model.SessionManager
import kotlinx.coroutines.launch

import com.example.wheretogo.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isLoggedIn by SessionManager
        .isLoggedIn(context)
        .collectAsState(initial = null)

    // Пока DataStore загружает состояние, показываем индикатор
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

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onLoginSuccess = { idUser,login ->
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

        composable(route = Destination.Map.route) {
            ProfileScreen()
        }
    }
}
