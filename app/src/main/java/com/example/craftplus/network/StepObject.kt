package com.example.craftplus.network

import kotlinx.serialization.SerialName
import java.util.UUID

data class StepObject(
    @SerialName(value = "numStep")
    var numStep: Int = 0,
    @SerialName(value = "video")
    var video: String = "",
    @SerialName(value = "blocks")
    var blocks: List<BlockObject> = emptyList()
)