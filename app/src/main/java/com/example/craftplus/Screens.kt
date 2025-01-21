package com.example.craftplus

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register: Screens("register")
    object Home: Screens("home")
    object Builds: Screens("Build")
    object Search: Screens("search")
    object Settings: Screens("settings")
//    object Materials: Screens("materials")
//    object Profile: Screens("profile")
//    object Friends: Screens("friends")
//    object Camera: Screens("camera")
    object Media: Screens("media")
    object WaitForResponse: Screens("wait_for_response/{buildId}")
    object ChooseRoles: Screens("choose_roles/{buildId}")
    object Builder: Screens("builder/{buildId}")
    object Recorder: Screens("recorder/{buildId}")
    object SearchFromHome: Screens("searchFromHome/{title}")
}