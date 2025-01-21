package com.example.craftplus.Media

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craftplus.network.BlockObject
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@Composable
fun MediaListScreen(
    navController: NavController,  // Accept navController as a parameter
    modifier: Modifier = Modifier,
    buildViewModel: BuildViewModel
) {

    val projectId = "utbdioxirmblbdwagasi"
    val bucketName = "build-videos"
    val fileName = "file_supabase" + Random.nextInt(0, 100) + ".mp4"

    downloadAndSaveVideo(projectId, bucketName, fileName)


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

    val bucket = supabaseClient.storage.from("build-videos")

    val mediaReader = MediaReader(
        context = LocalContext.current
    )

    // Define the onResult lambda to handle the downloaded ByteArray
//    val onResult: (ByteArray?) -> Unit = { byteArray ->
//        if (byteArray != null) {
//            // Define the directory to save the video
//            val directory = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
//                "CraftPlus_Builds_Videos"
//            )
//
//            // Ensure the directory exists
//            if (!directory.exists()) {
//                directory.mkdirs()
//            }
//
//            // Create the file in the specified directory
//            val file = File(directory, fileName)
//
//            try {
//                // Write the ByteArray to the file
//                FileOutputStream(file).use { outputStream ->
//                    outputStream.write(byteArray)
//                }
//                Log.d("DOWNLOAD", "Download completed: ${file.absolutePath}")
//            } catch (e: Exception) {
//                Log.e("DOWNLOAD", "Error saving the video: ${e.message}")
//            }
//        } else {
//            Log.e("DOWNLOAD", "Download failed: ByteArray is null")
//        }
//    }

    // Define the onResult callback
//    val onResult: (ByteArray?) -> Unit = { byteArray ->
//        if (byteArray != null) {
//            // Save the video to external storage
//            val savedPath = saveVideoToExternalStorage(byteArray, "test_file_1000000075")
//            if (savedPath != null) {
//                Log.d("DOWNLOAD", "Video saved successfully at: $savedPath")
//            } else {
//                Log.e("DOWNLOAD", "Failed to save video.")
//            }
//        } else {
//            Log.e("DOWNLOAD", "Download failed: ByteArray is null")
//        }
//    }
    // Define the onResult callback
//    val onResult: (ByteArray?) -> Unit = { byteArray ->
//        if (byteArray != null) {
//            // Save the video to external storage
//            val savedPath = saveVideoToExternalStorage(byteArray, "test_file_1000000075.mp4")
//            if (savedPath != null) {
//                Log.d("DOWNLOAD", "Video saved successfully at: $savedPath")
//            } else {
//                Log.e("DOWNLOAD", "Failed to save video.")
//            }
//        } else {
//            Log.e("DOWNLOAD", "Download failed: ByteArray is null")
//        }
//    }


//    saveVideoToExternalStorage(downloadVideoNet(fileName, onResult, bucket),
//        "test_file_1000000075" )

    //AINDA E PRECISO ALTERAR AQUI
    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    // DEPOIS E PRECISO TIRAR O RANDOM PARA ESTAR DE ACORDO
    val steps: List<StepObject>? = builds?.random()?.steps;


    val iterableBuilds: ListIterator<BuildObject>? = builds?.listIterator()
    val buildSteps: List<StepObject>? = iterableBuilds?.next()?.steps;
    val iterableSteps: ListIterator<StepObject>? = buildSteps?.listIterator()
    //val videos: List<String>? = iterableSteps?.next().video


    val BuildVideos: List<String> = builds
        ?.flatMap { build -> build.steps } // Mapeia todos os steps em cada build
        ?.mapNotNull { step -> step.video } // Mapeia os vídeos dos steps, ignorando nulls
        ?: emptyList() // Retorna uma lista vazia se builds for null

    // ViewModelFactory integrated inside the composable
    val viewModel: MediaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(mediaReader) as T
            }
        }
    )

    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Request permissions if needed (you can add logic here to handle permission requests)

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //Log.d("GETS", builds.toString())
            // Observing the list of files from the viewModel
            items(steps ?: emptyList()) { step ->
                // You can fetch the corresponding file for the step, e.g., using a random or specific file
                val file = if (viewModel.files.isNotEmpty()) {
                    viewModel.files.random() // Pega um arquivo aleatório se a lista não estiver vazia
                } else {
                    alo
                }
                MediaListItem(
                    file = file,
                    modifier = Modifier.fillMaxWidth(),
                    step = step
                )
            }
        }
    }
}

fun downloadVideoNet(fileName: String, onResult: (ByteArray?) -> Unit, bucket: BucketApi) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val byteArray = bucket.downloadPublic(fileName)
            //Log.d("Dentro do donwload", byteArray.toString())// Nome do arquivo no bucket
            withContext(Dispatchers.Main) {
                onResult(byteArray) // Retorna o conteúdo
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.printStackTrace()
                onResult(null) // Em caso de erro
            }
        }
    }
}

fun downloadAndSaveVideo(
    projectId: String,
    bucketName: String,
    fileName: String
) {
    // Launch a coroutine on the IO dispatcher
    CoroutineScope(Dispatchers.IO).launch {
        // Construct the public URL
        val fileUrl = "https://utbdioxirmblbdwagasi.supabase.co/storage/v1/object/public/build-videos/content:/media/external/video/media/1000000075"

        // Initialize OkHttpClient
        val client = OkHttpClient()

        // Create a request to download the file
        val request = Request.Builder()
            .url(fileUrl)
            .build()

        try {
            // Execute the request
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("DOWNLOAD", "Failed to download file: ${response.message}")
                    return@use
                }

                // Get the byte stream of the response
                val inputStream = response.body?.byteStream() ?: run {
                    Log.e("DOWNLOAD", "Failed to get input stream from response")
                    return@use
                }

                // Define the directory and file where the video will be saved
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val videoDir = File(moviesDir, "Craft+_Builds_Videos")

                // Ensure the directory exists
                if (!videoDir.exists() && !videoDir.mkdirs()) {
                    Log.e("DOWNLOAD", "Failed to create directory: ${videoDir.absolutePath}")
                    return@use
                }

                // Create the video file
                val videoFile = File(videoDir, fileName)

                // Write the input stream to the file
                FileOutputStream(videoFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Log.d("DOWNLOAD", "Video saved successfully at: ${videoFile.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("DOWNLOAD", "Error during download or save: ${e.message}")
        }
    }
}

//fun downloadAndSaveVideo(
//    projectId: String,
//    bucketName: String,
//    fileName: String
//) {
//    // Construct the public URL
//    val fileUrl = "https://utbdioxirmblbdwagasi.supabase.co/storage/v1/object/public/build-videos/content:/media/external/video/media/1000000075"
//
//    // Initialize OkHttpClient
//    val client = OkHttpClient()
//
//    // Create a request to download the file
//    val request = Request.Builder()
//        .url(fileUrl)
//        .build()
//
//    // Execute the request
//    client.newCall(request).execute().use { response ->
//        if (!response.isSuccessful) {
//            Log.d("DOWNLOAD", "Failed to download file: ${response.message}")
//            return
//        }
//
//        // Get the byte stream of the response
//        val inputStream = response.body?.byteStream() ?: run {
//            Log.d("DOWNLOAD", "Failed to get input stream from response")
//            return
//        }
//
//        // Define the directory and file where the video will be saved
//        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//        val videoDir = File(moviesDir, "CraftPlus_Builds_Videos")
//
//        // Ensure the directory exists
//        if (!videoDir.exists() && !videoDir.mkdirs()) {
//            Log.d("DOWNLOAD", "Failed to create directory: ${videoDir.absolutePath}")
//            return
//        }
//
//        // Create the video file
//        val videoFile = File(videoDir, fileName)
//
//        try {
//            // Write the input stream to the file
//            FileOutputStream(videoFile).use { outputStream ->
//                inputStream.copyTo(outputStream)
//            }
//            Log.d("DOWNLOAD", "Video saved successfully at: ${videoFile.absolutePath}")
//        } catch (e: IOException) {
//            Log.e("DOWNLOAD", "Error saving video: ${e.message}")
//        }
//    }
//}


//fun saveVideoToExternalStorage(videoData: Unit, fileName: String): String? {
//    // Get the public Movies directory
//    val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//    // Create a subdirectory named "CraftPlus_Builds_Videos"
//    val videoDir = File(moviesDir, "CraftPlus_Builds_Videos")
//
//    // Ensure the directory exists
//    if (!videoDir.exists()) {
//        if (!videoDir.mkdirs()) {
//            // Failed to create directory
//            return null
//        }
//    }
//
//    // Create the video file
//    val videoFile = File(videoDir, fileName)
//    return try {
//        // Write the byte array to the file
//        FileOutputStream(videoFile).use { fos ->
//            fos.write(videoData)
//        }
//        // Return the absolute path of the saved video
//        videoFile.absolutePath
//    } catch (e: IOException) {
//        e.printStackTrace()
//        null
//    }
//}

fun saveVideoToExternalStorage(videoData: ByteArray?, fileName: String): String? {
    // Get the public Movies directory
    val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    // Create a subdirectory named "CraftPlus_Builds_Videos"
    val videoDir = File(moviesDir, "CraftPlus_Builds_Videos")

    // Ensure the directory exists
    if (!videoDir.exists()) {
        if (!videoDir.mkdirs()) {
            // Failed to create directory
            return null
        }
    }

    // Create the video file
    val videoFile = File(videoDir, fileName)
    return try {
        // Write the byte array to the file
        FileOutputStream(videoFile).use { fos ->
            fos.write(videoData)
        }
        // Return the absolute path of the saved video
        videoFile.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


val alo = MediaFile(
    uri = Uri.parse("mock://uri"),  // Mock de URI
    name = "Mock File",  // Nome mock
    type = MediaType.VIDEO,  // Tipo mock
    buildValues = BuildObject(  // Mock completo de BuildObject
        id = "mock_id",
        title = "Mock Build",
        starter = "Mock Starter",
        friend = "Mock Friend",
        builder = "Mock Builder",
        recorder = "Mock Recorder",
        blocks = 0,
        totalSteps = 1,
        steps = listOf(
            StepObject(
                numStep = 1,
                video = "",
                blocks = listOf(
                    BlockObject(type = "stone", amount = 5)
                )
            )
        )
    )
)



