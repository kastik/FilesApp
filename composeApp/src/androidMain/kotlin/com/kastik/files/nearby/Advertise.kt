package com.kastik.files.nearby

import android.content.Context
import android.os.Build
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Advertise(
    private val context: Context,
    private val connectionLifecycleCallback: ConnectionLifecycleCallback,
    private val connectionClient: ConnectionsClient,
) {

    private val _advertiseStatus = MutableStateFlow<AdvertiseStatus>(AdvertiseStatus.Initing)
    val advertiseState = _advertiseStatus.asStateFlow()

    fun startAdvertising() {
        val advertisingOptions =
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionClient
            .startAdvertising(
                Build.DEVICE,
                context.packageName,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener {
                _advertiseStatus.value = AdvertiseStatus.Advertising
            }
            .addOnFailureListener { _ ->
                _advertiseStatus.value = AdvertiseStatus.Failure
            }
    }

    fun stopAdvertising() {
        connectionClient.stopAdvertising()
    }

    sealed class AdvertiseStatus {
        data object Initing : AdvertiseStatus()
        data object Advertising : AdvertiseStatus()
        data object Failure : AdvertiseStatus()
    }


}