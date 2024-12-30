package com.example.craftplus

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import android.graphics.Bitmap
import androidx.lifecycle.viewmodel.compose.viewModel

private var recording: Recording? = null

// Executor
var cameraExecutor = Executors.newSingleThreadExecutor()

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraBuildScreen(navController: NavController, modifier: Modifier = Modifier, onPhotoTaken: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }

    controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // Permission State
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Trocar entre mic on e off onClick
    var micVar by remember { mutableStateOf<Boolean?>(false) }

    val viewModel = viewModel<MainViewModel>()

    val bitmaps by viewModel.bitmaps.collectAsState()

    // Request permission when the composable enters the composition
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
        audioPermissionState.launchPermissionRequest()
    }

    // Dispose of the cameraExecutor when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (cameraPermissionState.status.isGranted && audioPermissionState.status.isGranted) {
        // Camera Preview
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(modifier = Modifier.size(450.dp)) {

                CameraPreview(controller = controller, modifier.align(Alignment.Center))

                // Mic Button
                IconButton(
                    onClick = {
                        // Toggle micVar between "mic_on" and "mic_off"
                        micVar = if (micVar == false) {
                            true // or whatever the off image resource ID is
                        } else {
                            false  // or the on image resource ID
                        }
                    },
                    modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Image(
                        painter = painterResource(id = when (micVar) {
                            true -> R.drawable.mic_24px // On mic image
                            else -> R.drawable.mic_off_24px // Off mic image
                        }),
                        contentDescription = "Microphone"
                    )
                }

                IconButton(
                    onClick = {
                        controller.cameraSelector =
                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else CameraSelector.DEFAULT_BACK_CAMERA
                    },
                    modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.flip_camera_android_24px),
                        contentDescription = "Swap cameras"
                    )
                }

                // Capture Button
                IconButton(
                    onClick = {
                            capturePhoto(
                                controller = controller,
                                context = context,
                                onPhotoSaved = { uri ->
                                    // Handle the saved URI, e.g., store it, show it, etc.
                                    Log.d("Camera", "Photo saved at: $uri")
                                },
                                onPhotoTaken = viewModel::onTakePhoto
                            )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.take_a_picture),
                        contentDescription = "Capture a Photo"
                    )
                }


                IconButton(
                    onClick = {
                        recordVideo(controller, context)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.video_camera_back_24px),
                        contentDescription = "Start recording"
                    )
                }
            }
        }
    }
}

// Helper function to capture photo using MediaStore
private fun capturePhoto(controller: LifecycleCameraController,
                         context: Context,
                         onPhotoSaved: (Uri) -> Unit,
                         onPhotoTaken: (Bitmap) -> Unit

) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_$name.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Craft+_Pictures")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()


    controller.takePicture(
        outputOptions,  // Pass the outputOptions directly here
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Get the URI of the saved file
                val savedUri: Uri = outputFileResults.savedUri ?: Uri.EMPTY
                onPhotoSaved(savedUri)
                Toast.makeText(
                    context,
                    "Picture saved: ${savedUri.path}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Couldn't save photo: ", exception)
                Toast.makeText(
                    context,
                    "Couldn't save picture",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
}

@SuppressLint("MissingPermission")
fun recordVideo(controller: LifecycleCameraController, context: Context) {

    if(recording != null) {
        recording?.stop()
        recording = null
        return
    }

    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "$name.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Craft+_Builds")
    }

    val outputOptions = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues).build()


    //Toast must be called on the main thread not on the camera one
    val mainExecutor = ContextCompat.getMainExecutor(context)

    recording = controller.startRecording(
        outputOptions,
        //FileOutputOptions.Builder(outputFile).build(),
        AudioConfig.create(true),
        mainExecutor
    ) { event ->
        when (event) {
            is VideoRecordEvent.Finalize -> {
                mainExecutor.execute {
                    if (event.hasError()) {
                        //Log.d("Finaliza", event.cause.toString())
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            context,
                            "Video capture failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val uri = event.outputResults.outputUri
                        Toast.makeText(
                            context,
                            "Video saved: ${uri.path}",
                            Toast.LENGTH_LONG
                        ).show()

                        //notify media store
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(uri.toString()),
                            arrayOf("video/mp4"),
                            null
                        )
                    }
                }
            }
        }
    }
}
