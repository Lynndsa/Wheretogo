package com.example.wheretogo.navigation

sealed class Destination(val route : String) {
    data object Login : Destination(LOGIN_ROUTE)
    data object Registration : Destination(REGISTRATION_ROUTE)
    data object Main : Destination(MAIN_ROUTE)
    companion object{
        private const val LOGIN_ROUTE = "route_login"
        private const val REGISTRATION_ROUTE = "route_registration"
        private const val MAIN_ROUTE = "route_main"
    }
}