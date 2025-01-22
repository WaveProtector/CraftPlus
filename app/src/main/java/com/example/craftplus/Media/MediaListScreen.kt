package com.example.craftplus.Media

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.craftplus.TopBar
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel
import com.example.craftplus.network.StepObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MediaListScreen(
    navController: NavController,  // Accept navController as a parameter
    modifier: Modifier = Modifier,
    title: String,
    buildViewModel: BuildViewModel
) {

    val builds: List<BuildObject>? = buildViewModel.getBuildObjects();
    var build: BuildObject? = builds?.find { it.title.equals(title, ignoreCase = true) }

    //Caso seja clicado na lupa, faz random
    if (title.equals("Castelo")) {
        build = builds?.firstOrNull()
    }

    val steps: List<StepObject>? = build?.steps;


    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(navController, if (build?.title != null) build.title else "Castelo")

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding())
            ) {

                items(steps ?: emptyList()) { step ->

                    var file by remember { mutableStateOf<MediaFile?>(null) }
                    var isVideoDownloaded by remember { mutableStateOf(false) }

                    // Baixa o vídeo se não estiver baixado
                    if (!isVideoDownloaded) {
                        downloadAndSaveVideo(
                            step.video,
                            context = LocalContext.current
                        ) { downloadedUri, downloadedByteArray ->
                            //setUri(downloadedUri)
                            //alo.byteArray = downloadedByteArray
                            //Log.d("Loop", "$downloadedUri")
                            file = createMediaFileFromBuild(
                                build,
                                step.video,
                                downloadedUri,
                                downloadedByteArray
                            )
                            isVideoDownloaded = true
                        }
                    }

                    file?.let { mediaFile ->
                        MediaListItem(
                            file = mediaFile,
                            buildTitle = build!!.title,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth(),
                            step = step
                        )
                    }

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
                    //Log.d("DOWNLOAD", "Video saved successfully at: $uri")
                } catch (e: IOException) {
                    Log.e("DOWNLOAD", "Failed to save video to Media Store: ${e.message}")
                }
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