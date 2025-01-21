package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Composable
fun WaitForResponseScreen(buildId: String, navController: NavController, modifier: Modifier) {
    val db = FirebaseFirestore.getInstance()
    var status by remember { mutableStateOf("inviting") }
    var listenerStatusCheck by remember { mutableStateOf<ListenerRegistration?>(null) }

    LaunchedEffect(buildId) {
        listenerStatusCheck = db.collection("Builds")
            .document(buildId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val usersJoined = it.getLong("usersJoined") ?: 0
                    val ownerEmail = it.get("ownerEmail")
                    val invitedEmail = it.get("invitedEmail")
                    if (usersJoined.toInt() == 2) {
                        db.collection("Builds").document(buildId).update("status", "ongoing")
                        runBlocking {
                            updateUserStatus(ownerEmail.toString())
                            updateUserStatus(invitedEmail.toString())
                        }
                    }
                    status = it.getString("status") ?: "inviting"
                    if (status == "ongoing") {
                        listenerStatusCheck?.remove() // Remover o listener!
                        navController.navigate(Screens.ChooseRoles.route.replace("{buildId}", buildId))
                    }
                }
            }
    }

    // Cleanup para evitar leak de listeners
    DisposableEffect(Unit) {
        onDispose {
            listenerStatusCheck?.remove()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (status == "inviting") {
            Text("Waiting for the other user to accept...")
        } else {
            Text("Invitation accepted! Redirecting...")
        }
    }
}

private suspend fun updateUserStatus(userEmail: String) {
    val db = FirebaseFirestore.getInstance()
    val querySnapshot = db.collection("Users").whereEqualTo("email", userEmail).get().await()
    if (!querySnapshot.isEmpty) {
        // Obtém o primeiro documento correspondente
        val document = querySnapshot.documents[0]
        val userId = document.id

        // Atualiza o status do usuário para "busy"
        db.collection("Users").document(userId).update("status", "busy").await()

        println("Status atualizado com sucesso para: busy")
    }
}

