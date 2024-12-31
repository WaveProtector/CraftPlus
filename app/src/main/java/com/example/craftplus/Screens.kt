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
    object Camera: Screens("camera")
    object RolesConfirm: Screens("roles_confirm")
    object Media: Screens("media")
    object BlockTracking: Screens("block_tracking")
    object BuildVideoTracking: Screens("build_video_tracking")
}