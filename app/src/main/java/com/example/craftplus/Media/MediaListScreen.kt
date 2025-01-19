package com.example.craftplus.Media

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject

@Composable
fun MediaListScreen(
    navController: NavController,  // Accept navController as a parameter
    modifier: Modifier = Modifier,
    buildViewModel: BuildViewModel
) {

    val mediaReader = MediaReader(
        context = LocalContext.current
    )

    //AINDA E PRECISO ALTERAR AQUI
    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    // DEPOIS E PRECISO TIRAR O RANDOM PARA ESTAR DE ACORDO
    val steps: List<StepObject>? = builds?.random()?.steps;


    val iterableBuilds: ListIterator<BuildObject>? = builds?.listIterator()
    val buildSteps: List<StepObject>? = iterableBuilds?.next()?.steps;
    val iterableSteps: ListIterator<StepObject>? = buildSteps?.listIterator()
    //val videos: List<String>? = iterableSteps?.next().video


    val BuildVideos: List<String> = builds
        ?.flatMap { build -> build.steps } // Mapeia todos os steps em cada build
        ?.mapNotNull { step -> step.video } // Mapeia os vídeos dos steps, ignorando nulls
        ?: emptyList() // Retorna uma lista vazia se builds for null

    // ViewModelFactory integrated inside the composable
    val viewModel: MediaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(mediaReader) as T
            }
        }
    )

    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Request permissions if needed (you can add logic here to handle permission requests)

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //Log.d("GETS", builds.toString())
            // Observing the list of files from the viewModel
            items(steps ?: emptyList()) { step ->
                // You can fetch the corresponding file for the step, e.g., using a random or specific file
                val file = viewModel.files.random() // or any logic to fetch the file associated with the step

                MediaListItem(
                    file = file,
                    modifier = Modifier.fillMaxWidth(),
                    step = step
                )
            }
        }
    }
}



