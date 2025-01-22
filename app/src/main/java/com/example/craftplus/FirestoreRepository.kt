package com.example.craftplus

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.example.craftplus.network.BuildObject
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException

class FirestoreRepository(private val firestore: FirebaseFirestore) {

    /**
     * Saves a BuildObject to Firestore.
     *
     * @param build The BuildObject to be saved.
     * @throws IOException If there is an issue with the Firestore operation.
     */
    suspend fun saveBuildObject(build: BuildObject) {
        try {
            // Referência à coleção "Builds"
            val collectionRef = firestore.collection("Builds")
            // Adicionar um timestamp ao objeto BuildObject antes de salvar
            //val buildWithTimestamp = build.copy(id = build.id, title = build.title, starter= build.starter)

            collectionRef.add(build).await()
        } catch (e: Exception) {
            throw IOException("Failed to save BuildObject to Firestore", e)
        }
    }

    /**
     * Loads the last BuildObject saved to Firestore.
     *
     * @return The last saved BuildObject or null if none exists.
     * @throws IOException If there is an issue with the Firestore operation.
     */
    suspend fun loadLastBuildObject(): BuildObject? {
        return try {
            val querySnapshot = firestore.collection("build_objects")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val document = querySnapshot.documents.firstOrNull()
            document?.toObject(BuildObject::class.java)
        } catch (e: Exception) {
            Log.e("Firestore", "Error loading BuildObject: ${e.message}")
            null
        }
    }

    /**
     * Gets the current number of builds for a user.
     *
     * @param userId The unique identifier of the user.
     * @return The current build count.
     * @throws IOException If there is an issue with the Firestore operation.
     */
    suspend fun getBuildCount(userId: String): Int {
        try {
            val documentRef = firestore.collection("user_stats").document(userId)
            val documentSnapshot = documentRef.get().await()

            return if (documentSnapshot.exists()) {
                documentSnapshot.getLong("buildCount")?.toInt() ?: 0
            } else {
                // Initialize build count to 0 if the document doesn't exist
                documentRef.set(mapOf("buildCount" to 0)).await()
                0
            }
        } catch (e: Exception) {
            throw IOException("Failed to get build count from Firestore", e)
        }
    }

    /**
     * Increments the build count for a user.
     *
     * @param userId The unique identifier of the user.
     * @throws IOException If there is an issue with the Firestore operation.
     */
    suspend fun incrementBuildCount(userId: String) {
        try {
            val documentRef = firestore.collection("user_stats").document(userId)
            documentRef.set(
                mapOf("buildCount" to FieldValue.increment(1)),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            throw IOException("Failed to increment build count in Firestore", e)
        }
    }

    suspend fun getBuildObjects(): List<BuildObject> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("Builds").get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(BuildObject::class.java)?.apply { id = document.id }
            }
        } catch (e: Exception) {
            Log.e("getBuilds", "Error fetching build objects", e)
            // Log the exception or handle it as needed
            emptyList()
        }
    }

    suspend fun getUserStatus(id: String): String? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val documentSnapshot = db.collection("Users").document(id).get().await()
            //Log.d("getStatus", documentSnapshot.toString())
            if (documentSnapshot.exists()) {
                Log.d("getStatusDentro", documentSnapshot.getString("status").toString())
                return documentSnapshot.getString("status")

            } else {
                Log.e("getUserStatus", "Document with id $id does not exist.")
                "unknown"
            }
        } catch (e: Exception) {
            Log.e("getUserStatus", "Error fetching user status", e)
            "unknown"
        }
    }


}

// Singleton Instance
object RepositoryProvider {
    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository(firestore)
    }
}
