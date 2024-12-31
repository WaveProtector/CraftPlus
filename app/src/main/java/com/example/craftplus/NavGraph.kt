package com.example.craftplus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.craftplus.Media.MediaListScreen
import com.example.craftplus.network.BuildViewModel

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
            CreateBuildScreen(buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) },navController = navController,
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
            RecordBuildScreen(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        composable(route = Screens.Media.route) {
            MediaListScreen(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        composable(route = Screens.Friends.route) {
            // TODO
        }

    }
}