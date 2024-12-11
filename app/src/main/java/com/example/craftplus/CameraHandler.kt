package com.example.craftplus

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.guava.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraBuildScreen(navController: NavController, modifier: Modifier = Modifier, onPhotoTaken: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission State
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // ImageCapture use case
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Camera Provider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Remember PreviewView
    val previewView = remember { PreviewView(context) }

    // Request permission when the composable enters the composition
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    // Dispose of the cameraExecutor when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        // Camera Preview
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Verificar se nao esta a cortar a camera
            TopBar(navController, "Recording")

            Box(modifier = Modifier.weight(1f)) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            // Capture Button
            Button(
                onClick = {
                    if (imageCapture != null) {
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture!!,
                            executor = cameraExecutor,
                            onPhotoTaken = onPhotoTaken
                        )
                    } else {
                        Toast.makeText(context, "Camera not ready yet", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text(text = "Capture Photo")
            }
        }

        // Setup Camera in LaunchedEffect
        LaunchedEffect(cameraProviderFuture) {
            try {
                val cameraProvider = cameraProviderFuture.await()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Camera initialization failed", exc)
                Toast.makeText(context, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        // Permission not granted
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera permission is required to use this feature.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

// Helper function to capture photo using MediaStore
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onPhotoTaken: (Uri) -> Unit
) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_$name.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CraftPlus")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                Log.d("CameraX", "Photo saved in: $savedUri")
                savedUri?.let {
                    // Switch to the main thread to update UI
                    Handler(context.mainLooper).post {
                        onPhotoTaken(it)
                        Toast.makeText(context, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                Handler(context.mainLooper).post {
                    Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}
