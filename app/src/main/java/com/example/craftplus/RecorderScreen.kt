package com.example.craftplus

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.HttpResponseCache.install
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.craftplus.network.StepObject
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import kotlin.random.Random

val commonBlocks = listOf(
    "Stone",
    "Dirt",
    "Grass Block",
    "Cobblestone",
    "Sand",
    "Gravel",
    "Oak Log",
    "Spruce Log",
    "Birch Log",
    "Jungle Log",
    "Acacia Log",
    "Dark Oak Log",
    "Oak Planks",
    "Spruce Planks",
    "Birch Planks",
    "Jungle Planks",
    "Acacia Planks",
    "Dark Oak Planks",
    "Coal Ore",
    "Iron Ore",
    "Gold Ore",
    "Diamond Ore",
    "Redstone Ore",
    "Lapis Lazuli Ore",
    "Emerald Ore"
)

@Composable
fun RecorderScreen(
    navController: NavController,
    buildId: String,
    modifier: Modifier = Modifier
) {
    val supabaseClient = remember {
        createSupabaseClient(
            supabaseUrl = "https://utbdioxirmblbdwagasi.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV0YmRpb3hpcm1ibGJkd2FnYXNpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY3Mjg5NTUsImV4cCI6MjA1MjMwNDk1NX0.W2lCPBmDcFUUyql22kK1NtUabHZ6f_EwWzuwBbeIaLU"
        )
        {
            install(Postgrest)
            install(io.github.jan.supabase.realtime.Realtime) // Realtime plugin
            install(io.github.jan.supabase.storage.Storage) // Storage plugin
        }
    }
    val context = LocalContext.current
    var isProcessingRecording by remember { mutableStateOf(false) }
    var currentStepNumber by remember { mutableIntStateOf(1) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    // Verificar permissões
    val permissionsGranted = checkPermissions(context)
    // Solicitar permissões, se necessário
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
                // Permissões concedidas
                Log.i("Permissions", "Camera and Microphone permissions granted")
            } else {
                // Permissões negadas
                Log.i("Permissions", "Permissions denied")
            }
        }
    )
    // Verifique se as permissões foram concedidas
    if (!permissionsGranted) {
        // Solicite permissões se não forem concedidas
        requestPermissionsLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uriString = result.data?.getStringExtra("VIDEO_URI")
            videoUri = uriString?.let { Uri.parse(it) }
            isProcessingRecording = true
            // Processar o URI do vídeo aqui i.e. meter o vídeo no supabase e depois no firebase; Incrementar o step e continuar.
            val byteArray = videoUri?.let { getByteArrayFromUri(context.contentResolver, it) }
            videoUri?.let { byteArray?.let { it1 -> uploadVideoToSupabase(supabaseClient, it, it1, buildId, currentStepNumber, context) } }
            videoUri?.let { updateSteps(buildId, currentStepNumber, it) }
            currentStepNumber++
            Log.d("Recorder", "Step Number incremented: $currentStepNumber")
            isProcessingRecording = false
            Log.d("RecorderScreen", "Video URI: $videoUri")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            startRecording(context, buildId, currentStepNumber, launcher)
        }, enabled = (!isProcessingRecording && permissionsGranted)) {
            Text("Start Recording Video Step $currentStepNumber")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            endRecordingSession(buildId, navController, context)
        }, enabled = (!isProcessingRecording)) {
            Text("End Build Session")
        }
    }
}

fun startRecording(
    context: Context,
    buildId: String,
    stepNumber: Number,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(context, CameraRecordingActivity::class.java).apply {
        putExtra("BUILD_ID", buildId)
        putExtra("STEP_NUMBER", stepNumber)
    }
    launcher.launch(intent)
}

// Função de verificação de permissões
private fun checkPermissions(context: Context): Boolean {
    val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    val audioPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED
}

private fun getByteArrayFromUri(contentResolver: ContentResolver, uri: Uri): ByteArray? {
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        inputStream?.use {
            it.readBytes() // Converte o InputStream para ByteArray
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Funções de gravação e atualização da Firestore
fun uploadVideoToSupabase(
    supabaseClient: SupabaseClient,
    uri: Uri,
    byteArray: ByteArray,
    buildId: String,
    currentStepNumber: Int,
    context: Context
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Enviar o vídeo para o Supabase
                val bucket = supabaseClient.storage.from("build-videos")
                val results = bucket.upload(uri.toString(), byteArray) {
                    upsert = false
                }
                Log.d("RecorderScreen", "Resultados do upload ao Supabase: $results") // DEBUG

                Log.d("com.example.craftplus.RecorderScreen", "Video uploaded successfully with Supabase ID: $uri")
                //saveVideoToFirestore(uri.toString(), buildId, currentStepNumber)
            } else {
                Log.e("com.example.craftplus.RecorderScreen", "Failed to open InputStream for URI: $uri")
            }
            inputStream?.close() // Libertar recurso
        } catch (e: Exception) {
            Log.e("com.example.craftplus.RecorderScreen", "Error uploading video: ${e.message}", e)
        }
    }
}

fun updateSteps(buildId: String, currentStepNumber: Int, videoUri: Uri) {
    // Initialize Firestore
    val db = FirebaseFirestore.getInstance()
    // Reference to the document you want to update
    val documentRef = db.collection("Builds").document(buildId)
    // Create a new StepObject to add
    var numberOfBlocks = Random.nextInt(1, 11)
    var listOfBlocks = mutableListOf<HashMap<String, Any>>()
    repeat(numberOfBlocks){
        listOfBlocks.add(hashMapOf("type" to commonBlocks[Random.nextInt(0, commonBlocks.size)], "quantity" to Random.nextInt(10, 70)))
    }
    val newStep = hashMapOf(
        "numStep" to currentStepNumber,
        "video" to videoUri,
        "blocks" to listOfBlocks
    )

    // Update the 'steps' array field by adding the new StepObject
    documentRef.update("steps", FieldValue.arrayUnion(newStep))
        .addOnSuccessListener {
            // Successfully added the new step
            println("New step added to BuildObject successfully.")
        }
        .addOnFailureListener { e ->
            // Failed to add the new step
            println("Error adding new step: $e")
        }
}


// Função para salvar o vídeo na Firestore com o ID do vídeo e o número do step
fun saveVideoToFirestore(videoUri: String, buildId: String, currentStepNumber: Int) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("Builds").document(buildId)

    // Objeto com o stepNumber e o supabaseVideoId
    val videoMap = mapOf(
        "stepNumber" to currentStepNumber,
        "supabaseVideoId" to videoUri
    )

    // Atualizar o campo 'videos' com um novo map
    buildRef.update("videos", FieldValue.arrayUnion(videoMap))
        .addOnSuccessListener {
            Log.d("com.example.craftplus.RecorderScreen", "Video added to 'videos' array in Firestore")
        }
        .addOnFailureListener { e ->
            Log.e("com.example.craftplus.RecorderScreen", "Error saving video to Firestore: ${e.message}")
        }
}

// Altera status da build para "completed" e vai para o home screen
fun endRecordingSession(buildId: String, navController: NavController, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("Builds").document(buildId)

    buildRef.update("status", "completed")
        .addOnSuccessListener {
            Log.d("com.example.craftplus.RecorderScreen", "Build state updated to 'completed' for buildId: $buildId")
            runBlocking {
                updateUsersStatus(buildId)
            }
            Toast.makeText(context, "Build finished with success!", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.Home.route)
        }
        .addOnFailureListener { e ->
            Log.e("com.example.craftplus.RecorderScreen", "Error updating build state: ${e.message}")
            Toast.makeText(context, "Error finishing build!", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.Home.route)
        }
}

private suspend fun updateUsersStatus(buildId: String) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("Builds").document(buildId)
    val ownerEmail = buildRef.get().await().get("ownerEmail")
    val invitedEmail = buildRef.get().await().get("invitedEmail")
    updateUserStatus(ownerEmail.toString())
    updateUserStatus(invitedEmail.toString())
}

private suspend fun updateUserStatus(userEmail: String) {
    val db = FirebaseFirestore.getInstance()
    val querySnapshot = db.collection("Users").whereEqualTo("email", userEmail).get().await()
    if (!querySnapshot.isEmpty) {
        // Obtém o primeiro documento correspondente
        val document = querySnapshot.documents[0]
        val userId = document.id

        // Atualiza o status do usuário para "busy"
        db.collection("Users").document(userId).update("status", "online").await()

        println("Status atualizado com sucesso para: online")
    }
}