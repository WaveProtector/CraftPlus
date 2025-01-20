package com.example.craftplus

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class InviteViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    // Verificar convites
    fun checkForInvites(userEmail: String, navController: NavController) {
        listenerRegistration = db.collection("Builds")
            .whereEqualTo("invitedEmail", userEmail)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documents?.forEach { document ->
                    val status = document.getString("status") ?: "inviting"
                    val buildId = document.id
                    val invitedEmail = document.getString("invitedEmail")
                    if (status == "inviting" && invitedEmail == userEmail) {
                        // Redireciona para o ecrã de espera
                        increaseUsersJoined(db, buildId)
                        onCleared()
                        navController.navigate(Screens.WaitForResponse.route.replace("{buildId}", buildId))
                    }
                }
            }
    }

    // Cleanup para evitar leak de listeners
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    /**
     * Updates the usersJoined in the db when a user gets redirected to the wait screen.
     */
    private fun increaseUsersJoined(db: FirebaseFirestore, buildId: String) {
        db.collection("Builds")
            .document(buildId)
            .update("usersJoined", FieldValue.increment(1)) // Incrementa o número de forma atomic, mais seguro!
    }
}

