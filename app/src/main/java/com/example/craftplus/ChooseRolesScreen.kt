package com.example.craftplus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun ChooseRolesScreen(buildId: String, navController: NavController, modifier: Modifier) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    var rolesListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Função para obter ownerEmail e invitedEmail com base no buildId
    LaunchedEffect(buildId) {
        rolesListener = db.collection("Builds")
            .document(buildId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val builder = it.getString("builder") ?: ""
                    val recorder = it.getString("recorder") ?: ""

                    if (builder != userEmail && builder != "" && recorder == "") {
                        db.collection("Builds")
                            .document(buildId)
                            .update("recorder", userEmail)
                        navController.navigate(Screens.Recorder.route.replace("{buildId}", buildId))
                        rolesListener?.remove()

                    } else if (recorder != userEmail && recorder != "" && builder == "") {
                        db.collection("Builds")
                            .document(buildId)
                            .update("builder", userEmail)
                        navController.navigate(Screens.Builder.route.replace("{buildId}", buildId))
                        rolesListener?.remove()
                    }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose your role")

        Spacer(modifier = Modifier.height(16.dp))


        Button(onClick = {
            db.collection("Builds").document(buildId).update("builder", userEmail)
            navController.navigate(Screens.Builder.route.replace("{buildId}", buildId))
        }) {
            Text("Build")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            db.collection("Builds").document(buildId).update("recorder", userEmail)
            navController.navigate(Screens.Recorder.route.replace("{buildId}", buildId)) // TODO -> FALTA CRIAR ESTA PÁGINA, É A MAIS IMPORTANTE. A CAMERA DEVE OCUPAR A MAIORIA DO ECRÃ (NÃO A TOTALIDADE) E TERMOS UM BOTÃO PARA GRAVAR, PAUSAR E PARAR A GRAVAÇÃO (PARAR A GRAVAÇÃO ACABA A BUILD!) E TAMBÉM TER UM BOTÃO DE "STEP DONE" QUE ACABA O VÍDEO ATUAL E GUARDA-O NUM STEP1,2,ETC. E DEPOIS COMEÇA LOGO A GRAVAR OUTRO VÍDEO PARA O PRÓXIMO STEP. OS VÍDEOS DEVEM SER GUARDADOS NA SUPABASE!
        }) {
            Text("Record")
        }
    } // TODO -> NO FINAL DA INTERAÇÃO DE CREATE BUILD OS USERS DEVEM SER REDIRECIONADOS AO SEU "HOME" RESPETIVO. (Screens.Home.route)
}
