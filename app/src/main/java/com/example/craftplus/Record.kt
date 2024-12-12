package com.example.craftplus

import android.graphics.Paint.Align
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RecordBuildScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopBar(navController, "Record Build")

        Text(
            // colocar o nome do friend
            text = "You are recording a build with Herobrine!",
            color = Color.Black,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Image(
//                    painter = painterResource(id = R.drawable.steve_pfp),
//                    contentDescription = "Steve Avatar",
//                    modifier = Modifier
//                        .size(150.dp)
//                        .clip(CircleShape)
//                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Steve Minecraft",
//                    style = MaterialTheme.typography.titleMedium,
//                    textAlign = TextAlign.Center
//                )
//            }
//
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Image(
//                    painter = painterResource(id = R.drawable.herobrine),
//                    contentDescription = "Herobrine Avatar",
//                    modifier = Modifier
//                        .size(150.dp)
//                        .clip(CircleShape)
//                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Herobrine",
//                    style = MaterialTheme.typography.titleMedium,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(100.dp))
//
//        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Adicione padding, se necessário
        ) {
            // Role Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalConfiguration.current.screenHeightDp.dp * 0.6f)// Adicione padding, se necessário
                ) {
                    CameraBuildScreen(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        onPhotoTaken = { uri ->
                            //photoViewModel.setPhotoUri(uri)
                            //navController.popBackStack()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { navController.navigate(Screens.Home.route) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = { /*navController.navigate(Screens.Camera.route) *//*E TEM DE SER ACIETE PELO AMIGO*/ },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(text = "Finish")
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}