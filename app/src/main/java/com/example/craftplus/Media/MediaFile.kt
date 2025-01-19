package com.example.craftplus.Media

import android.net.Uri
import com.example.craftplus.network.BuildObject

data class MediaFile(
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val buildValues: BuildObject
)