package com.example.craftplus

import android.content.Intent
import android.graphics.Paint.Align
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun Home(navController: NavController, modifier: Modifier = Modifier) {
    val user = FirebaseAuth.getInstance().currentUser
    // TODO -> Get the avatar from DB, instead of using the "template" one
    val pfpSource = R.drawable.steve_pfp

    val db = FirebaseFirestore.getInstance()
    val username = remember { mutableStateOf<String?>(null) }
    // State for the search query
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }
    val inviteViewModel: InviteViewModel = viewModel()
    if (user != null) {
        Log.d("Checking for invites", "User is checking for create build invites...")
        inviteViewModel.checkForInvites(user.email.toString(), navController)
    }

    // Fetch username from Firestore based on email
    LaunchedEffect(user?.email) {
        user?.email?.let { email ->
            db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    val document = result.documents.firstOrNull()
                    username.value = document?.getString("username")
                }
        }
    }

    // TODO -> Get WIP builds from DB, to display on the home menu
    val buildsWIP = null

    Column (modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text("Welcome back, ${username.value}!", fontSize = 28.sp)
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
        // TODO -> Não é muito importante implementar esta parte tho, isto é mais um "tracker de builds"

        Spacer(modifier = Modifier.height(60.dp))

        // Logout button
        Button(onClick = { logout(navController) }) {
            Text(text = "Logout")
        }
    }
}

// Function to handle logout
private fun logout(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    // Verifica se o usuário está autenticado
    currentUser?.let { user ->
        // Atualiza o status de 'online' para 'false' no Firestore
        db.collection("Users").document(user.uid)
            .update("online", false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Logout do FirebaseAuth
                    auth.signOut()
                    // Redireciona para a tela de login após o logout
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true } // Limpa a pilha de navegação
                    }
                } else {
                    // Se falhar na atualização do Firestore
                    Toast.makeText(navController.context, "Erro ao atualizar status de usuário", Toast.LENGTH_SHORT).show()
                }
            }
    } ?: run {
        // Caso não haja usuário logado
        auth.signOut()
        navController.navigate("login") {
            popUpTo("home") { inclusive = true } // Limpa a pilha de navegação
        }
    }
}



