package com.example.craftplus.Media

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun StepDetailsScreen(navController: NavController, buildTitle: String, step: Int, uri: String,
                      modifier: Modifier = Modifier, buildViewModel: BuildViewModel) {

    val decodedUri = Uri.decode(uri)
    Log.d("DetailsArgs", "$buildTitle $step $uri")
    val videoUri = Uri.parse(decodedUri)
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var nextStepObj: StepObject? = null
    var uriNextStep: String = ""

    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    //Log.d("viewwwwwww", builds.toString())
    var build: BuildObject? = builds?.find { it.title.equals(buildTitle, ignoreCase = true) }

    //Caso seja clicado na lupa, faz random
    if (buildTitle.equals("Castelo")) {
        build = builds?.firstOrNull()
    }

    val steps: List<StepObject>? = build?.steps;

    //Log.d("steps", steps.toString())

    val stepObj = steps?.get(step - 1)

    if (steps != null && steps.size > step) {
        nextStepObj = steps[step]

        downloadAndSaveVideo(
            fileName = nextStepObj.video,
            context = LocalContext.current
        ) { uriDonwloaded, byteArray ->
            // URI da próxima etapa
           uriNextStep = Uri.encode(uriDonwloaded.toString())

        }
    }

    //Log.d("stepObj", "${stepObj}")

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

        Box(modifier = Modifier.fillMaxSize()) {

            TopBar(navController, "Step: " + step.toString())

            if (nextStepObj != null) {
                Button(
                    onClick = {
                        val stepNumber = (step + 1).toString()
                        val route = Screens.StepDetails.route
                            .replace("{buildTitle}", buildTitle)
                            .replace("{step}", stepNumber)
                            .replace("{uri}", uriNextStep)
                        navController.navigate(route)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Alinha o botão no topo direito
                        .padding(16.dp) // Adiciona um padding para afastar do topo e da direita
                ) {
                    Text(text = "Next")//MAISS
                }
            }
        }

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
//        stepObj?.blocks?.forEach { block ->
//            BlockItem(block = block)
//        }
//        stepObj?.blocks?.forEach { block ->
//            BlockItem(block = block)
//        }


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
                    "stone" to R.drawable.stone,
                    "dirt" to R.drawable.dirt,
                    "grass_block" to R.drawable.grass_block,
                    "cobblestone" to R.drawable.cobblestone,
                    "sand" to R.drawable.sand,
                    "gravel" to R.drawable.gravel,
                    "oak_log" to R.drawable.oak_log,
                    "spruce_log" to R.drawable.spruce_log,
                    "birch_log" to R.drawable.birch_log,
                    "jungle_log" to R.drawable.jungle_log,
                    "acacia_log" to R.drawable.acacia_log,
                    "dark_oak_log" to R.drawable.dark_oak_log,
                    "oak_planks" to R.drawable.oak_planks,
                    "spruce_planks" to R.drawable.spruce_planks,
                    "birch_planks" to R.drawable.birch_planks,
                    "jungle_planks" to R.drawable.jungle_planks,
                    "acacia_planks" to R.drawable.acacia_planks,
                    "dark_oak_planks" to R.drawable.dark_oak_planks,
                    "coal_ore" to R.drawable.coal_ore,
                    "iron_ore" to R.drawable.iron_ore,
                    "gold_ore" to R.drawable.gold_ore,
                    "diamond_ore" to R.drawable.diamond_ore,
                    "redstone_ore" to R.drawable.redstone_ore,
                    "lapis_lazuli_ore" to R.drawable.lapis_lazuli,
                    "emerald_ore" to R.drawable.emerald_ore
                )

                val imageResource = blockImageMap[block.type] ?: R.drawable.steve_pfp

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
