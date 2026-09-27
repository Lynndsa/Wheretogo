package com.example.wheretogo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wheretogo.Greeting
import com.example.wheretogo.LoginWindow
import com.example.wheretogo.RegistrationWindow

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Main.route,
    ) {
        composable(route = Destination.Registration.route) {
            RegistrationWindow(onLoginClick = { navController.navigate(Destination.Login.route) })
        }
        composable(route = Destination.Login.route) {
            LoginWindow(onRedistrationClick = {
                navController.navigate(Destination.Registration.route) })

        }
        composable(route = Destination.Main.route) {
            Greeting(
                onLoginClick = { navController.navigate(Destination.Login.route) },
                onGuestClick = { navController.navigate(Destination.Registration.route) })
        }
    }
}