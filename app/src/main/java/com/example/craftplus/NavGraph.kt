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
            // TODO
            StatusToggleButton(auth.currentUser?.uid.toString())
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
    }
}
////TODO TIRAR DAQUI
//@Composable
//fun VideoScreen() {
//    val videoUri = Uri.parse("/storage/emulated/0/Movies/Craft+_Builds_Videos/file_supabase13.mp4")
//    VideoPlayer(videoUri = videoUri, modifier = Modifier.fillMaxSize())
//}
//
//@Composable
//fun VideoPlayer(
//    videoUri: Uri,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//
//    // Initialize ExoPlayer
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build().apply {
//            setMediaItem(MediaItem.fromUri(videoUri))
//            prepare()
//        }
//    }
//
//    // Dispose of the ExoPlayer when the composable is removed
//    AndroidView(
//        modifier = modifier,
//        factory = {
//            PlayerView(context).apply {
//                player = exoPlayer
//                layoutParams = FrameLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//            }
//        }
//    )
//    DisposableEffect(
//        Unit
//    ) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//}