package com.example.craftplus.Media

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.craftplus.TopBar

@Composable
fun StepDetailsScreen(navController: NavController, buildId: String, step: Int, uri: String,
                      modifier: Modifier = Modifier) {

    val decodedUri = Uri.decode(uri)
    Log.d("DetailsArgs", "$buildId $step $uri")
    val videoUri = Uri.parse(decodedUri)//"/storage/emulated/0/Movies/Craft+_Builds_Videos/file_supabase13.mp4")
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(navController, "Step: " + step.toString())
        // Dispose of the ExoPlayer when the composable is removed
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
    }
    DisposableEffect(
        Unit
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun StepDetails(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Dispose of the ExoPlayer when the composable is removed
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Eh Scrollable", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))
    }
    DisposableEffect(
        Unit
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
