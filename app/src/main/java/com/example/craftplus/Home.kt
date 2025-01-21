package com.example.craftplus

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        BuildDropdown { selectedBuild ->
            //navController.navigate("search")
            navController.navigate(Screens.Search.route.replace("{title}", selectedBuild))
            Log.d("SelectedBuild", "Usuário selecionou: $selectedBuild")
            // Aqui você pode navegar para outra tela ou carregar detalhes da build
        }

        Spacer(modifier = Modifier.height(50.dp))

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
            .update("status", "offline")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildDropdown(onBuildSelected: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var expanded by remember { mutableStateOf(false) }
    var builds by remember { mutableStateOf(listOf<String>()) }
    var filteredBuilds by remember { mutableStateOf(listOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Carrega builds do Firestore
    LaunchedEffect(Unit) {
        db.collection("Builds")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedBuilds = documents
                    .map { it.getString("title") ?: "" }
                    .filter { it.isNotEmpty() } // Garante que o título não está vazio
                builds = fetchedBuilds
                filteredBuilds = fetchedBuilds
            }
            .addOnFailureListener { Log.e("Firestore", "Error fetching builds", it) }
    }

    // Filtra as builds de acordo com o texto inserido
    LaunchedEffect(searchQuery) {
        filteredBuilds = if (searchQuery.isEmpty()) {
            builds
        } else {
            builds.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                expanded = true // Mostra o dropdown ao começar a digitar
            },
            label = { Text("Search for builds...") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filteredBuilds.forEach { buildTitle ->
                DropdownMenuItem(
                    onClick = {
                        searchQuery = buildTitle // Atualiza o campo com a build selecionada
                        onBuildSelected(buildTitle)
                        Log.d("onBuildSelected", buildTitle)
                        expanded = false
                    },
                    text = { Text(buildTitle) }
                )
            }
        }
    }
}