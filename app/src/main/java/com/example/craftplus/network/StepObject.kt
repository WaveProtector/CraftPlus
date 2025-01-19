package com.example.craftplus.network

import kotlinx.serialization.SerialName

data class StepObject(
    //var id: String = "",
    @SerialName(value = "numStep")
    var numStep: Int = 0,
    //@SerialName(value = "title")
    //var title: String = "",
    @SerialName(value = "video")
    var video: String = "",
    @SerialName(value = "blocks")
    var blocks: List<BlockObject> = emptyList()
)