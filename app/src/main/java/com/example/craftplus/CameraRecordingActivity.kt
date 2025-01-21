package com.example.craftplus

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraRecordingActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var previewView: PreviewView? = null
    private var recording: Recording? = null
    private lateinit var buildId: String
    private var currentStepNumber: Int = 1 // Controla o número do passo (step)
    private lateinit var lastVideoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoRecordingScreen()
        }

        previewView = PreviewView(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Receber o argumento passado
        buildId = intent.getStringExtra("BUILD_ID") ?: ""
        currentStepNumber = intent.getIntExtra("STEP_NUMBER", 0)
        // Solicitar permissões de câmera
        if (allPermissionsGranted()) {
            startCamera(previewView!!)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, videoCapture)

                preview.surfaceProvider = previewView.surfaceProvider
            } catch (e: Exception) {
                Log.e("Camera", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @Composable
    fun VideoRecordingScreen() {
        var isRecording by remember { mutableStateOf(false) } // please don't crash..
        var isVideoTaken by remember { mutableStateOf(false) } // please don't crash..

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(
                modifier = Modifier.weight(1f),
                factory = { previewView!! }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                startRecording()
                isRecording = true
                isVideoTaken = true
                 },
                enabled = !isRecording) {
                Text("Start Recording")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                stopRecording()
                isRecording = false
                 }, enabled = isRecording && isVideoTaken) {
                Text("Stop Recording")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val resultIntent = Intent().apply {
                    putExtra("VIDEO_URI", lastVideoUri.toString())
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Voltar para a RecorderScreen
            }, enabled = !isRecording) {
                Text("Finish step $currentStepNumber")
            }
        }
    }

    private fun startRecording() {
        val fileName = "step_${buildId}_${currentStepNumber}"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "VID_$fileName.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Craft+_Builds_Videos")
        }
        val outputOptions = MediaStoreOutputOptions.Builder(
            this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        recording = videoCapture?.output?.prepareRecording(this, outputOptions)
            ?.withAudioEnabled() // Habilitar áudio se necessário
            ?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // Gravação iniciada
                        //isRecording = true
                        Log.d("VideoRecordEvent.Start", "Gravação iniciada")
                        Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
                    }

                    is VideoRecordEvent.Finalize -> {
                        // Gravação finalizada
                        //isVideoTaken = true
                        //isRecording = false
                        lastVideoUri = recordEvent.outputResults.outputUri
                        Log.d("VideoRecordEvent.Finalize", "Gravação finalizada: $lastVideoUri")
                        Toast.makeText(this, "Recording stopped! Saved on: $lastVideoUri", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}