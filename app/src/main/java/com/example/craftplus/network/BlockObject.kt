package com.example.craftplus.network

import kotlinx.serialization.SerialName

data class BlockObject(
    //var id: String = "",
    @SerialName(value = "type")
    var type: String = "",
    @SerialName(value = "amount")
    var amount: Int = 0,
)