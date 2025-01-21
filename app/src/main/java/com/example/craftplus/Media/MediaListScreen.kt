package com.example.craftplus.Media

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craftplus.TopBar
import com.example.craftplus.network.BlockObject
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MediaListScreen(
    navController: NavController,  // Accept navController as a parameter
    modifier: Modifier = Modifier,
    buildViewModel: BuildViewModel
) {

    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    val build: BuildObject? = builds?.random() //BUILD ESPECIFICA
    val steps: List<StepObject>? = build?.steps;
    //val paths: List<String>? = steps?.map { it.video }


    val projectId = "utbdioxirmblbdwagasi"
    val bucketName = "build-videos"
    //val fileName = "file_supabase" + Random.nextInt(0, 100) + ".mp4"
    val (uri, setUri) = remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }


//
//    if (uri == null) {
//        downloadAndSaveVideo(projectId, bucketName, fileName, context = LocalContext.current) { downloadedUri, downloadedByteArray ->
//            setUri(downloadedUri)
//            alo.byteArray = downloadedByteArray  // Atualiza o byteArray com o conteúdo do vídeo
//            Log.d("URI_CHECK2", "$downloadedUri")
//        }
//    }

    val alo: MediaFile = MediaFile(
        uri = uri,  // Mock de URI
        name = "Mock File",
        byteArray = null, // Inicialize como null, pois será preenchido posteriormente
        type = MediaType.VIDEO,
        build = BuildObject(
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

//    // para cada step fazer o donwload do video a partir do supabase
//    if (uri == null) {
//        downloadAndSaveVideo(step.video, context = LocalContext.current) { downloadedUri, downloadedByteArray ->
//            setUri(downloadedUri)
//            alo.byteArray = downloadedByteArray  // Atualiza o byteArray com o conteúdo do vídeo
//            Log.d("Loop", "$downloadedUri")
//        }
//    }

    val supabaseClient = remember {
        createSupabaseClient(
            supabaseUrl = "https://utbdioxirmblbdwagasi.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV0YmRpb3hpcm1ibGJkd2FnYXNpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY3Mjg5NTUsImV4cCI6MjA1MjMwNDk1NX0.W2lCPBmDcFUUyql22kK1NtUabHZ6f_EwWzuwBbeIaLU"
        )
        {
            install(Postgrest)
            install(Realtime) // Realtime plugin
            install(Storage) // Storage plugin
        }
    }

    val bucket = supabaseClient.storage.from("build-videos")

    val mediaReader = MediaReader(
        context = LocalContext.current
    )



    val iterableBuilds: ListIterator<BuildObject>? = builds?.listIterator()
    val buildSteps: List<StepObject>? = iterableBuilds?.next()?.steps;
    val iterableSteps: ListIterator<StepObject>? = buildSteps?.listIterator()
    //val videos: List<String>? = iterableSteps?.next().video

    // ViewModelFactory integrated inside the composable
    val viewModel: MediaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaViewModel(mediaReader) as T
            }
        }
    )


// Realize o download em segundo plano e atualize o campo byteArray depois
//    CoroutineScope(Dispatchers.IO).launch {
//        val videoData = bucket.downloadPublic("/content:/media/external/video/media/1000000075") // Nome real do arquivo
//        withContext(Dispatchers.Main) {
//            alo.byteArray = videoData // Atualize o campo quando o download terminar
//            Log.d("DOWNLOAD", "Download concluído e byteArray atualizado!")
//        }
//    }

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
        TopBar(navController, if (build?.title != null) build.title else "Castelo")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            //Log.d("GETS", builds.toString())
            // Observing the list of files from the viewModel
            items(steps ?: emptyList()) { step ->

                var file by remember { mutableStateOf<MediaFile?>(null) }
                var isVideoDownloaded by remember { mutableStateOf(false) }

                // Baixa o vídeo se não estiver baixado
                if (!isVideoDownloaded) {
                    Log.d("Url", "path para supabase eh: " + step.video)
                    downloadAndSaveVideo(step.video, context = LocalContext.current) { downloadedUri, downloadedByteArray ->
                        setUri(downloadedUri)
                        alo.byteArray = downloadedByteArray
                        Log.d("Loop", "$downloadedUri")
                        file = createMediaFileFromBuild(build, step.video, downloadedUri, downloadedByteArray)
                        isVideoDownloaded = true // Marca como baixado para não fazer novamente
                    }
                }
                file?.let { mediaFile ->
                    MediaListItem(
                        file = mediaFile,
                        navController = navController,
                        modifier = Modifier.fillMaxWidth(),
                        step = step
                    )
                }
            }
        }
    }
}


fun downloadAndSaveVideo(
    fileName: String,
    context: Context,
    onUriReady: (Uri, ByteArray) -> Unit
) {
    // Launch a coroutine on the IO dispatcher
    CoroutineScope(Dispatchers.IO).launch {
        // Construct the public URL
        val fileUrl = "https://utbdioxirmblbdwagasi.supabase.co/storage/v1/object/public/build-videos/" + fileName
       // Log.d("Url", "url eh: " + fileUrl)
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

                val name = "file_supabase" + Random.nextInt(200, 301)
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, "$name.mp4")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES + "/Craft+_Builds_Videos"
                    )
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri == null) {
                    Log.e("DOWNLOAD", "Failed to create Media Store entry")
                    return@use
                }

                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val byteArray = inputStream.readBytes()

                    withContext(Dispatchers.Main) {
                        onUriReady(uri, byteArray)
                    }
                    Log.d("DOWNLOAD", "Video saved successfully at: $uri")
                } catch (e: IOException) {
                    Log.e("DOWNLOAD", "Failed to save video to Media Store: ${e.message}")
                }

                //val uriDownload = uri
                //withContext(Dispatchers.Main) { onUriReady(uriDownload) }
                //Log.d("DOWNLOAD", "Video saved successfully at: ${uri}")
                //withContext(Dispatchers.Main) { onUriReady(uriDownload) }
            }
        } catch (e: IOException) {
            Log.e("DOWNLOAD", "Error during download or save: ${e.message}")
        }
    }
}

fun createMediaFileFromBuild(build: BuildObject?, path:String, uri: Uri, byteArray: ByteArray): MediaFile {
    // Criando a estrutura MediaFile com base no objeto BuildObject
    return MediaFile(
        uri = uri,  // Inicialize o URI, que pode ser preenchido posteriormente
        name = path,  // Usando o título do build para o nome
        byteArray = byteArray,  // Inicialize como null, que será preenchido posteriormente
        type = MediaType.VIDEO,  // Tipo de mídia como vídeo
        build = build!!  // Atribuindo o build diretamente à propriedade buildValues
    )
}
