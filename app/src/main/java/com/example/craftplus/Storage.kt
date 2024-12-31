package com.example.craftplus

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File

class Storage {

    private val storage: FirebaseStorage = FirebaseStorage.getInstance() // Initialize FirebaseStorage instance
    private val storageRef = storage.reference // Get a reference to the root storage
    private val videosRef = storageRef.child("Videos") // Reference to the "Videos" folder

    fun uploadVideo(path: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val file = Uri.fromFile(File(path)) // Convert file path to URI
        val fileName = File(path).name

        // Save video in "Videos" folder with the same name as the cloud Firestore/Media
        val videoRef = videosRef.child(file.lastPathSegment ?: fileName)
        val uploadTask: UploadTask = videoRef.putFile(file) // Start upload task


        uploadTask.addOnSuccessListener {
            // Upload successful
            onSuccess()
        }.addOnFailureListener { exception ->
            // Upload failed
            onFailure(exception)
        }
    }
}
