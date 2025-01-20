package com.example.craftplus

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.Recording
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

fun File.getUri(context: Context): Uri? {
    return FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".fileprovider",
        this
    )
}


// Variáveis globais para controlar o estado da gravação e do step
private var currentRecording: Recording? = null
private var currentBuildId: String = ""
//private var currentStepNumber: Int = 1 // Controla o número do step
//private var isRecording = false
//private var isProcessingRecording = false
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
                Log.i("Permissions", "Camera and Audio permissions granted")
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
            currentStepNumber++
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
            // TODO -> Vai para home enquanto o builder vai para um ecrã para editar os steps e colocar-se os blocos e descrições -> depois o builder vai para o home
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
fun checkPermissions(context: Context): Boolean {
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

                // TODO -> Ver como se comporta não deve retornar id...
                // Supondo que o Supabase retorna um ID único para o vídeo
                val supabaseVideoId = "supabaseVideoIdHere" // Substitua com o ID retornado
                Log.d("com.example.craftplus.RecorderScreen", "Video uploaded successfully with Supabase ID: $supabaseVideoId")
                saveVideoToFirestore(uri.toString(), buildId, currentStepNumber)
            } else {
                Log.e("com.example.craftplus.RecorderScreen", "Failed to open InputStream for URI: $uri")
            }
            inputStream?.close() // Libertar recurso
        } catch (e: Exception) {
            Log.e("com.example.craftplus.RecorderScreen", "Error uploading video: ${e.message}", e)
        }
    }
}

// Função para salvar o vídeo na Firestore com o ID do vídeo e o número do step
fun saveVideoToFirestore(videoUri: String, buildId: String, currentStepNumber: Int) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("Builds").document(buildId ?: return)
    val videoId = videoUri

    // Objeto com o stepNumber e o supabaseVideoId
    val videoMap = mapOf(
        "stepNumber" to currentStepNumber,
        "supabaseVideoId" to videoId
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

