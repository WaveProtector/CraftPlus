package com.example.craftplus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph (navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route // TODO Deve ser depois alterado para login! Só está assim para efeitos de teste
    )
    {
        composable(route = Screens.Login.route) {
            // TODO
        }

        composable(route = Screens.Register.route) {
            // TODO
        }

        composable(route = Screens.Home.route) {
            Home(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.Builds.route) {
            // TODO
        }

        composable(route = Screens.Materials.route) {
            // TODO
        }

        composable(route = Screens.Profile.route) {
            // TODO
        }

        composable(route = Screens.Friends.route) {
            // TODO
        }

    }
}