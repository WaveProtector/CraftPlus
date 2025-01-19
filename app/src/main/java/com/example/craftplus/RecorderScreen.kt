package com.example.craftplus

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.OutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


// Variáveis globais para controlar o estado da gravação e do step
var activeRecording: Recording? = null
var currentStepNumber: Int = 1 // Controla o número do passo (step)

@Composable
fun RecorderScreen(
    navController: NavController,
    buildId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = Recorder.Builder().build()
    val videoCapture = VideoCapture.withOutput(recorder)
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CraftPlus")
    }
    var isRecording by remember { mutableStateOf(false) }
    var isProcessingRecording by remember { mutableStateOf(false) }
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
    // Preview da câmera
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val previewView = remember { PreviewView(context) }

    // Configuração do preview da câmera
    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            //O que tinhamos antes -> val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

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
            Log.w("RecorderScreen", "Recording is already active.")
            return
        }

        // O que tinhamos antes
//        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
//            context.contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        ).setContentValues(contentValues).build()
        val outputOptions = FileOutputOptions.Builder(File(context.filesDir, "video-step-$currentStepNumber.mp4")).build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("com.example.craftplus.RecorderScreen", "Record Audio permission not granted...")
            return
        }
        activeRecording = videoCapture.output.prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        Log.d("com.example.craftplus.RecorderScreen", "Recording started")
                    }

                    is VideoRecordEvent.Finalize -> {
                        val uri = recordEvent.outputResults.outputUri
                        Log.d("com.example.craftplus.RecorderScreen", "Recording finalized, URI: $uri")
                        if (uri !== Uri.EMPTY) {
                            val byteArray = getByteArrayFromUri(context.contentResolver, uri)
                            if (byteArray != null) {
                                uploadVideoToSupabase(supabaseClient, uri, byteArray, buildId, context)
                                isProcessingRecording = false
                            }
                        } else {
                            Log.e("com.example.craftplus.RecorderScreen", "Recording failed to finalize!")
                        }

//                        val uri = recordEvent.outputResults.outputUri
//                        Log.d("com.example.craftplus.RecorderScreen", "Recording finalized, URI: $uri")
//                        val byteArray = getByteArrayFromUri(context.contentResolver, uri)
//                        if (byteArray != null) {
//                            uploadVideoToSupabase(supabaseClient, uri, byteArray, buildId, context)
//                            isProcessingRecording = false
//                        }
                    }
                }
            }
    }


    // Finaliza a gravação atual e começa uma nova gravação
    fun stepDone() {
        isRecording = false
        if (activeRecording == null) {
            Log.w("com.example.craftplus.RecorderScreen", "No active recording to finalize.")
            return
        }
        // Finalizar a gravação atual
        activeRecording?.stop()
        activeRecording = null

        isProcessingRecording = true
        // Aguardar o processamento da gravação antes de iniciar outra
        CoroutineScope(Dispatchers.Main).launch {
            while (isProcessingRecording) {
                kotlinx.coroutines.delay(1500) // Aguardar 5000ms (ajuste conforme necessário)
            }

            startRecording() // Iniciar uma nova gravação
        }
    }


    // Função para parar a gravação e retornar à página inicial
    fun stopRecording() {
        isRecording = false
        if (activeRecording == null) {
            Log.w("RecorderScreen", "No active recording to stop.")
            navigateToHomePage(navController)
            return
        }

        // Finalizar a gravação
        activeRecording?.stop()
        //activeRecording = null

        isProcessingRecording = true
        // Aguardar o processamento da gravação antes de iniciar outra
        CoroutineScope(Dispatchers.Main).launch {
            while (isProcessingRecording) {
                kotlinx.coroutines.delay(1500) // Aguarde 500ms (ajuste conforme necessário)
            }
            updateBuildStateToStopped(buildId)
            navigateToHomePage(navController) // Navegar para a página inicial após finalizar
        }
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

        Button(onClick = { startRecording() }, enabled = (!isRecording && !isProcessingRecording)) {
            Text("Start Recording")
        }
        Button(onClick = { stepDone() }, enabled = (isRecording && !isProcessingRecording)) {
            Text("Step Done")
        }
        Button(onClick = { stopRecording() }, enabled = (isRecording && !isProcessingRecording)) {
            Text("Stop Recording")
        }
    }
}


// Funções de gravação e atualização da Firestore fora do com.example.craftplus.RecorderScreen
fun uploadVideoToSupabase(
    supabaseClient: SupabaseClient,
    uri: Uri,
    byteArray: ByteArray,
    buildId: String,
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

                Log.d("com.example.craftplus.RecorderScreen", "Video uploaded successfully with Supabase ID: $buildId")
                saveVideoToFirestore(uri.toString(), buildId)
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

    buildRef.update("status", "completed")
        .addOnSuccessListener {
            Log.d("com.example.craftplus.RecorderScreen", "Build state updated to 'stopped' for buildId: $buildId")
        }
        .addOnFailureListener { e ->
            Log.e("com.example.craftplus.RecorderScreen", "Error updating build state: ${e.message}")
        }
}


// Função para salvar o vídeo na Firestore com o ID do vídeo e o número do step
fun saveVideoToFirestore(videoUri: String, buildId: String) {
    val db = FirebaseFirestore.getInstance()
    val buildRef = db.collection("Builds").document(buildId ?: return)

    // Objeto com o stepNumber e o supabaseVideoId
    val videoMap = mapOf(
        "stepNumber" to currentStepNumber,
        "supabaseVideoId" to videoUri
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