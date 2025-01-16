package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Text

@Composable
fun BuildCatalogScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var builds by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        db.collection("builds")
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener { documents ->
                builds = documents.map { it.data }
            }
            .addOnFailureListener { Log.e("Firestore", "Error fetching builds", it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Build Catalog", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        builds.forEach { build ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    Text("Title: ${build["title"]}")
                    Text("Description: ${build["description"]}")
                }
            }
        }
    }
}
