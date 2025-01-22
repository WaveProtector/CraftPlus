package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StatusToggleButton(userId: String) {
    Log.d("userid",userId)
    var status by remember { mutableStateOf("online") }
    val db = FirebaseFirestore.getInstance()

    Button(
        onClick = {
            val newStatus = if (status == "online") "busy" else "online"
            db.collection("Users").document(userId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    status = newStatus
                }
                .addOnFailureListener { e ->
                    // Handle the error
                }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Set status to ${if (status == "online") "busy" else "online"}")
    }
}
