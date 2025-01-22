package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.craftplus.network.BuildViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StatusToggleButton(navController: NavController, userId: String, buildViewModel: BuildViewModel) {

    //Log.d("userid", userId)
    var status by remember { mutableStateOf("online") }
    val db = FirebaseFirestore.getInstance()

    var userStatus by remember { mutableStateOf("unknown") } //buildViewModel.getUserStatus(userId)

    db.collection("Users").document(userId)
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val statusFromFirestore = documentSnapshot.getString("status") ?: "unknown"
                userStatus = statusFromFirestore
                //Log.d("UserStatus", "Status do usuário: $statusFromFirestore")
                // Use o status conforme necessário
            } else {
                Log.e("UserStatus", "Documento com ID $userId não existe.")
            }
        }
        .addOnFailureListener { e ->
            Log.e("UserStatus", "Erro ao buscar o status do usuário", e)
        }

    Scaffold(
        topBar = {
            TopBar(navController, "Settings")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text(
                text = "Your current status is: $userStatus",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    val newStatus = if (status == "online") "busy" else "online"
                    db.collection("Users").document(userId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            status = newStatus
                            userStatus = newStatus
                        }
                        .addOnFailureListener { e ->
                            Log.e("StatusUpdate", "Failed to update status", e)
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Set status to ${if (status == "online") "busy" else "online"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }


        }
    }
}
