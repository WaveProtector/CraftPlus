package com.example.craftplus

import android.graphics.Paint.Align
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun Home(navController: NavController, modifier: Modifier = Modifier) {
    // TODO -> Get the avatar from DB, instead of using the "template" one
    val pfpSource = R.drawable.steve_pfp
    // TODO -> Get the username from DB, instead of the "template" one
    val username = "Steve"

    // State for the search query
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }

    // TODO -> Get WIP builds from DB, to display on the home menu
    val buildsWIP = null

    Column (modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
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

        Spacer(modifier = Modifier.height(110.dp))

        Text("Have a build in mind?", fontSize = 26.sp)
        // Search bar
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { newText -> searchQuery.value = newText },
            label = { Text("Search for builds...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Start)
        )

        Spacer(modifier = Modifier.height(100.dp))

        Text("Builds in progress", fontSize = 26.sp)
        // TODO

        Spacer(modifier = Modifier.height(150.dp))

        BottomNavBar(navController, 2)
    }
}

