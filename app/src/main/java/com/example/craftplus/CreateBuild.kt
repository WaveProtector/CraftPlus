package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craftplus.network.BlockObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Math.random
import kotlin.random.Random

// Global vars
val userEmail = FirebaseAuth.getInstance().currentUser?.email // Get email from current user

@Composable
fun CreateBuildScreen(buildViewModel: BuildViewModel, navController: NavController, modifier: Modifier = Modifier) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var invitedEmail by remember { mutableStateOf("") }

    val inviteViewModel: InviteViewModel = viewModel()
    inviteViewModel.checkForInvites(userEmail.toString(), navController)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        //verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Build", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para descrição do build
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        UserDropdown { invitedEmail = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de envio
        Button(
            onClick = {
                if (userEmail != null) {
                    createBuild(title, description, userEmail, invitedEmail, navController)
                }
            },
            enabled = title.isNotEmpty() && description.isNotEmpty() && invitedEmail.isNotEmpty()
        ) {
            Text("Create Build")
        }
    }
}

//TEM DE SER ALTERADO!!
fun createBuild(title: String, description: String, userEmail: String, invitedEmail: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val defaultBlocks = listOf(
        BlockObject(type = "stone", amount = 453),
        BlockObject(type = "wood", amount = 998)
    )
    val defaultBlocks2 = listOf(
        BlockObject(type = "bedrock", amount = 36),
        BlockObject(type = "gravel", amount = 67)
    )
    val defaultBlocks3 = listOf(
        BlockObject(type = "grass", amount = 1452),
        BlockObject(type = "wood", amount = 93)
    )
    val defaultBlocks4 = listOf(
        BlockObject(type = "crystal", amount = 741),
        BlockObject(type = "gravel", amount = 563)
    )
    val defaultBlocks5 = listOf(
        BlockObject(type = "books", amount = 5),
        BlockObject(type = "oil", amount = 123)
    )

    val defaultSteps = listOf(
        StepObject(
            numStep = 1,
            video = "",
            blocks = defaultBlocks
        ),
        StepObject(
            numStep = 2,
            video = "",
            blocks = defaultBlocks2
        ),
        StepObject(
            numStep = 3,
            video = "",
            blocks = defaultBlocks5
        )
    )

    val defaultSteps2 = listOf(
        StepObject(
            numStep = 1,
            video = "",
            blocks = defaultBlocks3
        ),
        StepObject(
            numStep = 2,
            video = "",
            blocks = defaultBlocks4
        ),
        StepObject(
            numStep = 3,
            video = "",
            blocks = defaultBlocks5
        ),
        StepObject(
            numStep = 4,
            video = "",
            blocks = defaultBlocks2
        ),
        StepObject(
            numStep = 5,
            video = "",
            blocks = defaultBlocks
        )
    )
    var defaultStepsToAdd = defaultSteps2
    if (Random.nextInt(0, 2) == 0) {
        defaultStepsToAdd = defaultSteps
    }


    // Dados para o BuildObject
    val buildData = mapOf(
        "title" to title,
        "description" to description,
        "ownerEmail" to userEmail,
        "builder" to "",
        "recorder" to "",
        "starter" to "",
        "friend" to "",
        "invitedEmail" to invitedEmail,
        "blocks" to Random.nextInt(100, 1001), // Inicialmente 0, será atualizado conforme os passos são adicionados
        "totalSteps" to defaultSteps.size,
        "steps" to defaultStepsToAdd.map { step ->
            mapOf(
                "numStep" to step.numStep,
                "video" to step.video,
                "blocks" to step.blocks.map { block ->
                    mapOf(
                        "type" to block.type,
                        "amount" to block.amount
                    )
                }
            )
        }
    )// !! O campo videos é um array de maps que contem o stepNumber e o videoUrl !!

    Log.d("created", buildData.toString())

    db.collection("Builds")
        .add(buildData)
        .addOnSuccessListener { document ->
            Log.d("Firestore", "Build created with ID: ${document.id}")
            // Enviar convite e direcionar para a página de "aguardar resposta"
            navController.navigate(Screens.WaitForResponse.route.replace("{buildId}", document.id))
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error adding document", exception)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDropdown(selectedUser: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var expanded by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf(listOf<String>()) }
    var selectedUserEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Busca os usuários online
        db.collection("Users")
            .whereEqualTo("online", true)
            .get()
            .addOnSuccessListener { documents ->
                val fetchedUsers = documents
                .map { it.getString("email") ?: "" }
                .filter { it.isNotEmpty() && it != userEmail } // Exclui o usuário atual
                users = fetchedUsers
            }
            .addOnFailureListener { Log.e("Firestore", "Error fetching users", it) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUserEmail,
            onValueChange = { selectedUserEmail = it },
            label = { Text("Select User") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            users.forEach { userEmail ->
                DropdownMenuItem(
                    onClick = {
                        selectedUserEmail = userEmail
                        selectedUser(userEmail)
                        expanded = false
                    },
                    text = { Text(userEmail) }
                )
            }
        }
    }
}