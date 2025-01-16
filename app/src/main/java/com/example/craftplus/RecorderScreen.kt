package com.example.craftplus

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Variáveis globais para controlar o estado da gravação e do step
var activeRecording: Recording? = null
var currentBuildId: String? = null
var currentStepNumber: Int = 1 // Controla o número do passo (step)

@Composable
fun RecorderScreen(
    navController: NavController,
    buildId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = Recorder.Builder()
        .build()
    val videoCapture = VideoCapture.withOutput(recorder)
    val contentValues by remember { mutableStateOf(ContentValues()) }
    var isRecording by remember { mutableStateOf(false) }

    // Exibindo o preview da câmera
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val previewView = remember { PreviewView(context) }

    // Configuração do preview da câmera
    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Log.e("RecorderScreen", "Error starting camera preview: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Inicia a gravação quando o botão "Start Recording" é pressionado
    fun startRecording() {
        if (activeRecording != null) {
            Log.w("com.example.craftplus.RecorderScreen", "Recording is already active.")
            return
        }

        // Armazenar o buildId para atualizar o estado depois
        currentBuildId = buildId

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("com.example.craftplus.RecorderScreen", "Record Audio permission not granted...")
            return
        }
        activeRecording = videoCapture.output.prepareRecording(context, mediaStoreOutput)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d("com.example.craftplus.RecorderScreen", "Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        val uri = recordEvent.outputResults.outputUri
                        Log.d("com.example.craftplus.RecorderScreen", "Recording finalized, URI: $uri")
                        uploadVideoToSupabase(uri, context)
                    }
                }
            }
        isRecording = true
    }

    // Finaliza a gravação atual e começa uma nova gravação
    fun stepDone() {
        if (activeRecording == null) {
            Log.w("com.example.craftplus.RecorderScreen", "No active recording to finalize.")
            return
        }

        // Finalizar a gravação atual
        activeRecording?.stop()
        activeRecording = null

        // Atualizar o estado da build no Firebase para "stopped"
        currentBuildId?.let { updateBuildStateToStopped(it) }

        // Salvar o vídeo na Firestore
        currentBuildId?.let { saveVideoToFirestore() }

        // Iniciar uma nova gravação
        startRecording()
    }

    // Função para parar a gravação e retornar à página inicial
    fun stopRecording() {
        if (activeRecording == null) {
            Log.w("com.example.craftplus.RecorderScreen", "No active recording to stop.")
            navigateToHomePage(navController)
            return
        }

        // Finalizar a gravação
        activeRecording?.stop()
        activeRecording = null

        // Considera o "Stop Recording" como um passo
        currentBuildId?.let { saveVideoToFirestore() } // Salva o vídeo correspondente ao "Stop Recording"

        // Atualizar o estado da build no Firebase para "stopped"
        currentBuildId?.let { updateBuildStateToStopped(it) }

        // Navegar para a página inicial após finalizar
        navigateToHomePage(navController)
    }

    // UI com os botões
    Column(modifier = modifier) {
        // Exibição do preview da câmera
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .weight(1f) // Ocupa metade do espaço vertical
                .padding(16.dp)
        )

        Button(onClick = { startRecording() }, enabled = !isRecording) {
            Text("Start Recording")
        }
        Button(onClick = { stepDone() }, enabled = isRecording) {
            Text("Step Done")
        }
        Button(onClick = { stopRecording() }, enabled = isRecording) {
            Text("Stop Recording")
        }
    }
}

// Funções de gravação e atualização da Firestore fora do com.example.craftplus.RecorderScreen

fun uploadVideoToSupabase(uri: android.net.Uri, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Lógica para enviar o vídeo para o Supabase
                // Supondo que o Supabase retorna um ID único para o vídeo
                val supabaseVideoId = "supabaseVideoIdHere" // Substitua com o ID retornado
                Log.d("com.example.craftplus.RecorderScreen", "Video uploaded successfully with Supabase ID: $supabaseVideoId")
                saveVideoToFirestore(supabaseVideoId)
            } else {
                Log.e("com.example.craftplus.RecorderScreen", "Failed to open InputStream for URI: $uri")
            }
            inputStream?.close() // Libertar recurso
        } catch (e: Exception) {
            Log.e("com.example.craftplus.RecorderScreen", "Error uploading video: ${e.message}", e)
        }
    }
}

// Função para atualizar o estado da build para "stopped" no Firebase
fun updateBuildStateToStopped(buildId: String) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("builds").document(buildId)

    buildRef.update("status", "stopped")
        .addOnSuccessListener {
            Log.d("com.example.craftplus.RecorderScreen", "Build state updated to 'stopped' for buildId: $buildId")
        }
        .addOnFailureListener { e ->
            Log.e("com.example.craftplus.RecorderScreen", "Error updating build state: ${e.message}")
        }
}

// Função para salvar o vídeo na Firestore com o ID do vídeo e o número do step
fun saveVideoToFirestore(supabaseVideoId: String? = null) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("builds").document(currentBuildId ?: return)

    val videoId = supabaseVideoId ?: "supabaseVideoIdHere" // Se o Supabase ID não for fornecido, use um valor padrão

    // Objeto com o stepNumber e o supabaseVideoId
    val videoMap = mapOf(
        "stepNumber" to currentStepNumber,
        "supabaseVideoId" to videoId
    )

    // Atualizar o campo 'videos' com um novo map
    buildRef.update("videos", FieldValue.arrayUnion(videoMap))
        .addOnSuccessListener {
            Log.d("com.example.craftplus.RecorderScreen", "Video added to 'videos' array in Firestore")
            // Incrementar o número do step para o próximo, mesmo para o stop
            currentStepNumber++
        }
        .addOnFailureListener { e ->
            Log.e("com.example.craftplus.RecorderScreen", "Error saving video to Firestore: ${e.message}")
        }
}

// Função de navegação para a página inicial
fun navigateToHomePage(navController: NavController) {
    navController.navigate(Screens.Home.route)
}
