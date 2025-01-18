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
    val builds = buildViewModel.getBuildObjects();
    val build: BuildObject? = builds?.random()
    val buildstepsvideo = build?.video;

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
            items(viewModel.files) { file ->
                MediaListItem(
                    file = file,
                    modifier = Modifier.fillMaxWidth(),
                    build = build
                )
            }
        }
    }
}



