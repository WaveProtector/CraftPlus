package com.example.craftplus.Media

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.craftplus.R
import com.example.craftplus.Screens
import com.example.craftplus.TopBar
import com.example.craftplus.network.BlockObject
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun StepDetailsScreen(navController: NavController, buildTitle: String, step: Int, uri: String,
                      modifier: Modifier = Modifier, buildViewModel: BuildViewModel) {

    val decodedUri = Uri.decode(uri)
    Log.d("DetailsArgs", "$buildTitle $step $uri")
    val videoUri = Uri.parse(decodedUri)
    val context = LocalContext.current
    val scrollState = rememberScrollState()


    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    Log.d("viewwwwwww", builds.toString())
    var build: BuildObject? = builds?.find { it.title.equals(buildTitle, ignoreCase = true) }

    //Caso seja clicado na lupa, faz random
    if (buildTitle.equals("Castelo")) {
        build = builds?.firstOrNull()
    }

    val steps: List<StepObject>? = build?.steps;

    Log.d("steps", steps.toString())

    val stepObj = steps?.get(step - 1)
    Log.d("stepObj", "${stepObj}")

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState),
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

        stepObj?.blocks?.forEach { block ->
            BlockItem(block = block)
        }
        stepObj?.blocks?.forEach { block ->
            BlockItem(block = block)
        }
        stepObj?.blocks?.forEach { block ->
            BlockItem(block = block)
        }


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
fun BlockItem(block: BlockObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(11.dp))
        {
            Row {

                val blockImageMap = mapOf(
                    "wood" to R.drawable.emerald,
                    "stone" to R.drawable.steve_pfp
                )

                val imageResource = blockImageMap[block.type] ?: R.drawable.video

                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = null,
                    modifier = Modifier
                        .width(23.dp)
                )
                Text(text = "  X ${block.quantity}",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
            }
        }

    }
}
