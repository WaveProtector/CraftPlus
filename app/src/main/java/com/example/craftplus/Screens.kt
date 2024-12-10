package com.example.craftplus

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register: Screens("register")
    object Home: Screens("home")
    object Builds: Screens("builds")
    object Materials: Screens("materials")
    object Profile: Screens("profile")
    object Friends: Screens("friends")
}