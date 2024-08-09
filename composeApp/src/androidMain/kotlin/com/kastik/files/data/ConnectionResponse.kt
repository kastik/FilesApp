package com.kastik.files.data

data class ConnectionResponse(
    val pin: String,
    val acceptConnection: () -> Unit,
    val rejectConnection: () -> Unit,
)