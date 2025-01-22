package com.example.craftplus

import android.util.Log
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
import com.example.craftplus.Media.StepDetailsScreen
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

        composable(route = Screens.SearchFromHome.route,
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            // Obtenha o título da navegação
            val title = backStackEntry.arguments?.getString("title")
            // Verifique se o título é nulo ou vazio
            if (title.isNullOrEmpty()) {
                Log.d("title nav", "Title is null or empty")
                // Se título for nulo, podemos fazer algum fallback ou exibir uma mensagem
                // Por exemplo, você pode navegar para uma tela de erro ou exibir uma UI alternativa
            } else {
                // Caso contrário, continue com a navegação normal
                MediaListScreen(
                    navController = navController,
                    title = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) }
                )
            }
        }
        composable(route = Screens.Settings.route) {
            StatusToggleButton(navController, auth.currentUser?.uid.toString(), buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) })
            //VideoScreen()

        }
        composable(route = Screens.Search.route) {
            MediaListScreen(
                navController = navController,
                title = "Castelo",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) }
            )
        }

        composable(
            route = Screens.StepDetails.route,
            arguments = listOf(
                navArgument("buildTitle") { type = NavType.StringType },
                navArgument("step") { type = NavType.IntType },
                navArgument("uri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Obtenha os argumentos da navegação
            val buildTitle = backStackEntry.arguments?.getString("buildTitle")
            val step = backStackEntry.arguments?.getInt("step")
            val uri = backStackEntry.arguments?.getString("uri")

            Log.d("StepDetails", "Build ID: $buildTitle, Step Number: $step, Uri: $uri")

            // Verifique se os argumentos não são nulos
            if (buildTitle.isNullOrEmpty() || step == null || uri.isNullOrEmpty()) {
                Log.d("StepDetails", "Invalid arguments: buildTitle or step or uri is null/empty")
            } else {
                Log.d("StepDetails", "Build ID: $buildTitle, Step Number: $step, Uri: $uri")


                // Exiba a tela com os dados fornecidos
                StepDetailsScreen(
                    navController = navController,
                    buildTitle = buildTitle,
                    step = step,
                    uri = uri,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    buildViewModel = viewModel { BuildViewModel(RepositoryProvider.firestoreRepository) }
                )
            }
        }
    }
}