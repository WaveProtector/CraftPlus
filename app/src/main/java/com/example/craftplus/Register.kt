package com.example.craftplus

import android.content.Context
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

private lateinit var auth: FirebaseAuth

@Composable
fun Register (navController: NavController, modifier: Modifier = Modifier) {
    // Inicializar FirebaseAuth
    auth = FirebaseAuth.getInstance()
    // Obter contexto local
    val context = LocalContext.current

    // Texto dos campos de textEdit
    var emailText by remember { mutableStateOf("") }
    var pwdText by remember { mutableStateOf("") }
    var confirmPwdText by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
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
        OutlinedTextField(
            value = confirmPwdText,
            onValueChange = { confirmPwdText = it },
            label = { Text("confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        // Register button
        Button(
            onClick = { registerUser(emailText, pwdText, confirmPwdText, navController, context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Register")
        }

        Spacer(modifier = Modifier.height(20.dp)) // Espaçamento

        // Already have an account? Go to login button
        Text(text = "Already have an account?")
        Button(
            onClick = {
                navController.navigate("login") // Navigates to the Login screen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }
    }
}

private fun registerUser(email: String, pwd: String, confirmPwd: String, navController: NavController, context: Context) {
    if (email.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
    } else if (pwd != confirmPwd) {
        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
    } else {
        // Firebase registration logic
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") // Navigate to home after successful registration
                } else {
                    Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}