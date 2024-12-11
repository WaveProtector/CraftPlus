package com.example.craftplus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
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

        composable(route = Screens.Home.route) {
            Home(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.Builds.route) {
            CreateBuildScreen(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.Roles.route) {
            ChooseBuildRoleScreen(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.RolesConfirm.route) {
            ConfirmBuildRoleScreen(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.Camera.route) {
            CameraBuildScreen(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                onPhotoTaken = { uri ->
                    //photoViewModel.setPhotoUri(uri)
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screens.Friends.route) {
            // TODO
        }

    }
}