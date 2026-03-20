package pl.edu.ur.blokur.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Tickets : AppRoute("tickets")
    data object Finances : AppRoute("finances")
    data object Announcements : AppRoute("announcements")
    data object Profile : AppRoute("profile")
}