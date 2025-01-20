package com.example.craftplus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.craftplus.Media.MediaListScreen
import com.example.craftplus.network.BuildViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun NavGraph (navController: NavHostController) {
    // Firebase instance variable
    var auth: FirebaseAuth = Firebase.auth
    var startDest = Screens.Login.route

    if (auth.currentUser != null) {
        startDest = Screens.Home.route
    }
    NavHost(
        navController = navController,
        startDestination = startDest
    )
    {
        composable(route = Screens.Login.route) {
            Login(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

        composable(route = Screens.Register.route) {
            Register(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

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

        composable( route = Screens.WaitForResponse.route,
            arguments = listOf(navArgument("buildId") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildId = backStackEntry.arguments?.getString("buildId")
            if (buildId != null) {
                WaitForResponseScreen(
                    buildId = buildId,
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }


        composable( route = Screens.ChooseRoles.route,
            arguments = listOf(navArgument("buildId") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildId = backStackEntry.arguments?.getString("buildId")
            if (buildId != null) {
                ChooseRolesScreen(
                    buildId = buildId,
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }

        composable(route = Screens.Builder.route,
            arguments = listOf(navArgument("buildId") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildId = backStackEntry.arguments?.getString("buildId")
            if (buildId != null) {
                BuilderScreen(
                    navController = navController,
                    buildId = buildId,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }

        composable(route = Screens.Recorder.route,
            arguments = listOf(navArgument("buildId") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildId = backStackEntry.arguments?.getString("buildId")
            if (buildId != null) {
                RecorderScreen(
                    navController = navController,
                    buildId = buildId,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }

        composable(route = Screens.Search.route) {
            MediaListScreen(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) }
            )
        }

        composable(route = Screens.Friends.route) {
            // TODO
        }

    }
}