package com.example.craftplus

sealed class Screens(val route: String) {
    object Login : Screens("login_screen")
    object Register: Screens("register_screen")
    object Home: Screens("home_screen")
    object Builds: Screens("builds_screen")
}