package com.example.craftplus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun BuilderScreen(
    buildId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()
    var listenerStatusCheck by remember { mutableStateOf<ListenerRegistration?>(null) }
    var status by remember { mutableStateOf("ongoing") }

    LaunchedEffect(buildId) {
        listenerStatusCheck = db.collection("Builds")
            .document(buildId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    status = it.getString("status") ?: "ongoing"
                    if (status == "completed") {
                        listenerStatusCheck?.remove()
                        navController.navigate(Screens.Home.route)
                    }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Start Building!")

        Spacer(modifier = Modifier.height(16.dp))

        // Exibe uma mensagem enquanto espera o "Recorder" parar a gravação
        Text("Waiting for the Recorder to stop the recording...")
    }
}
