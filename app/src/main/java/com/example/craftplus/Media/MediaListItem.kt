package com.example.craftplus.Media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.craftplus.R

@Composable
fun MediaListItem(
    file: MediaFile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (file.type) {
            MediaType.IMAGE -> {
                AsyncImage(
                    model = file.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .width(100.dp)
                )
            }

            MediaType.VIDEO -> {
                Image(
                    painter = painterResource(id = R.drawable.video_camera_back_24px),
                    contentDescription = null,
                    modifier = Modifier.width(100.dp)
                )
            }

            MediaType.AUDIO -> {
                Image(
                    painter = painterResource(id = R.drawable.mic_24px),
                    contentDescription = null,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
        Text(
            text = "${file.name} - ${file.type}",
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )
    }
}