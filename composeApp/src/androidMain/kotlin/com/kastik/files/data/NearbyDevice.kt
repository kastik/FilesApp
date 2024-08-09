package com.kastik.files.data

data class NearbyDevice(
    val endpointName: String,
    val endpointId: String,
    val connect: () -> Unit
)