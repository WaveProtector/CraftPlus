package com.example.craftplus

import android.media.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Home(navController: NavController, modifier: Modifier = Modifier) {
    // TODO -> Get the avatar from DB, instead of using the "template" one
    val pfpSource = R.drawable.steve_pfp
    // TODO -> Get the username from DB, instead of the "template" one
    val username = "Steve"
    Column (Modifier)
    {
        Text("Welcome back, $username!", fontSize = 28.sp)
        Image(
            painter = painterResource(id = pfpSource),
            contentDescription = pfpSource.toString(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(5.dp))


    }
}