package com.example.wheretogo.navigation

sealed class Destination(val route: String) {
    data object Login : Destination(LOGIN_ROUTE)
    data object Registration : Destination(REGISTRATION_ROUTE)
    data object Main : Destination(MAIN_ROUTE)
    data object Map : Destination(MAP_ROUTE)
    data object Profile : Destination(PROFILE_ROUTE)
    data object Places : Destination(PLACES_ROUTE)
    data object Wishlist : Destination(WISHLIST_ROUTE)
    data object MyRoutes : Destination(MY_ROUTES_ROUTE)
    data object ReadyRoutes : Destination(READY_ROUTES_ROUTE)
    data object Settings : Destination(SETTINGS_ROUTE)
    data object AddRoute : Destination(ADD_ROUTE_ROUTE)

    companion object {
        private const val LOGIN_ROUTE = "route_login"
        private const val REGISTRATION_ROUTE = "route_registration"
        private const val MAIN_ROUTE = "route_main"
        private const val MAP_ROUTE = "route_map"
        private const val PROFILE_ROUTE = "route_profile"
        private const val PLACES_ROUTE = "route_places"
        private const val WISHLIST_ROUTE = "route_wishlist"
        private const val MY_ROUTES_ROUTE = "route_my_routes"
        private const val READY_ROUTES_ROUTE = "route_ready_routes"
        private const val SETTINGS_ROUTE = "route_settings"
        private const val ADD_ROUTE_ROUTE = "route_add_route"
    }
}