package com.example.craftplus.Media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.craftplus.R
import com.example.craftplus.TopBar
import com.example.craftplus.network.StepObject


@Composable
fun MediaListItem(
    file: MediaFile,
    navController: NavController,
    modifier: Modifier = Modifier,
    step: StepObject?
) {

    val context = LocalContext.current
    var thumbnail: Bitmap? by remember { mutableStateOf(null) }
    var uriGenerated: Boolean = false

    //val thumbnail = generateVideoThumbnail(file.uri, context)
    var duration: Long? by remember { mutableStateOf(null) }


    LaunchedEffect(file.uri) {
        if (file.uri != null && !uriGenerated) {
            thumbnail = generateVideoThumbnail(file.uri, context)
            duration = getVideoDuration(file.uri, context)
            uriGenerated = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0x990787a2)
        )
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (file.type) {
                MediaType.VIDEO -> {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .width(100.dp)
                                .padding(3.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.video_camera_back_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .width(100.dp)
                                .padding(3.dp)
                        )
                    }
                }
                else -> {
                    // Aqui você pode adicionar o layout para áudio e imagem caso queira
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Step: ${step?.numStep}",
                    color = Color.White, // Cor do texto
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp // Tamanho maior da fonte
                    )
                )
                Text(
                    text = "Blocks: ${step?.blocks?.sumOf { it.quantity } ?: 0}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp // Tamanho maior da fonte
                    )
                )

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Duration: ${duration?.let { it / 1000 }}s",
                    color = Color.Black, // Cor branca para a duração
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp // Tamanho maior da fonte para a duração
                    )
                )
            }
        }
    }
}

fun generateVideoThumbnail(videoUri: Uri?, context: Context): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri)
        retriever.getFrameAtTime(1000000) // Frame em 1 segundo
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}

fun getVideoDuration(videoUri: Uri?, context: Context): Long? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, videoUri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}


