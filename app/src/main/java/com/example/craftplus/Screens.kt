package com.example.craftplus

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register: Screens("register")
    object Home: Screens("home")
    object Builds: Screens("Build")
    object Materials: Screens("materials")
    object Profile: Screens("profile")
    object Friends: Screens("friends")
    object Roles: Screens("roles")
    object RolesAccept: Screens("roles_accept")
    object BlockTracking: Screens("block_tracking")
    object BuildVideoTracking: Screens("build_video_tracking")
}