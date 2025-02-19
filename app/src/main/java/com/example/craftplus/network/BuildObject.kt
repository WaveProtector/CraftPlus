package com.example.craftplus.network

import kotlinx.serialization.SerialName

data class BuildObject(
    var id: String = "",
    @SerialName(value = "title")
    var title: String = "",
    @SerialName(value = "starter")
    var starter: String = "",
    @SerialName(value = "friend")
    var friend: String = "",
    @SerialName(value = "builder")
    var builder: String = "",
    @SerialName(value = "recorder")
    var recorder: String = "",
    //ALTERAR FUTURAMENTE PARA TER OS TIPOs
    @SerialName(value = "blocks")
    var blocks: Int = 0,
    //ALTERAR FUTURAMENTE PARA SER UMA ESTRUTURA NOVA
    @SerialName(value = "totalSteps")
    var totalSteps: Int = 0,
    @SerialName(value = "steps")
    var steps: List<StepObject> = emptyList()
)
