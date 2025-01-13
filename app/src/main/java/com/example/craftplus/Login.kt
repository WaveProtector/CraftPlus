package com.example.craftplus

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Private vars **/
private lateinit var auth: FirebaseAuth

@Composable
fun Login (navController: NavController, modifier: Modifier = Modifier) {
    // Inicializar FirebaseAuth
    auth = FirebaseAuth.getInstance()
    // Obter contexto local
    val context = LocalContext.current

    // Texto para email e password
    var emailText by remember { mutableStateOf("") }
    var pwdText by remember { mutableStateOf("") }

    Column (modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        OutlinedTextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = { Text("email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = pwdText,
            onValueChange = { pwdText = it },
            label = { Text("password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(50.dp)) // espaçamento

        Button(onClick = { loginUser(emailText, pwdText, navController, context) }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(10.dp)) // espaçamento

        Button(onClick = { navController.navigate("register") }) {
            Text("Register")
        }
    }
}

private fun loginUser(email: String, password: String, navController: NavController, context: Context) {
    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
    } else {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        // Atualizar o status "online" para true no Firestore
                        val db = FirebaseFirestore.getInstance()
                        db.collection("Users").document(userId).update("online", true)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to update online status: ${updateTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle login failure
                    Toast.makeText(
                        context,
                        "Login Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
