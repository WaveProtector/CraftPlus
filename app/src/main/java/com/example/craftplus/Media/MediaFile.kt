package com.example.craftplus.Media

import android.net.Uri
import com.example.craftplus.network.BuildObject

data class MediaFile(
    var uri: Uri?,
    var byteArray: ByteArray?,
    val name: String,
    val type: MediaType,
    val build: BuildObject
)