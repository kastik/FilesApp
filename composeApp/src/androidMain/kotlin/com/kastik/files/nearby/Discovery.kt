package com.kastik.files.nearby

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import com.kastik.files.data.NearbyDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class Discovery(
    private val context: Context,
    private val connectionClient: ConnectionsClient,
    private val connection: Connection,
) : EndpointDiscoveryCallback() {

    private val _discoveryState = MutableStateFlow<DiscoveryStatus>(DiscoveryStatus.Initing)
    val discoveryState = _discoveryState.asStateFlow()

    fun startDiscovery() {
        val discoveryOptions =
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionClient
            .startDiscovery(this.context.packageName, this, discoveryOptions)
            .addOnSuccessListener {
                _discoveryState.value = DiscoveryStatus.Discovering(mutableStateListOf())
            }
            .addOnFailureListener { exception: Exception ->
                _discoveryState.value = DiscoveryStatus.Failure
            }
    }

    fun stopDiscovery() {
        connectionClient.stopDiscovery()
    }

    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
        (_discoveryState.value as DiscoveryStatus.Discovering).devicesNearby.add( //TODO Race condition happening here!!
            NearbyDevice(
                endpointName = info.endpointName,
                endpointId = endpointId,
                connect = { connectionClient.requestConnection("kastik", endpointId, connection) }
            )
        )
    }

    override fun onEndpointLost(endpointId: String) {
        (_discoveryState.value as DiscoveryStatus.Discovering).devicesNearby.removeIf {
            it.endpointId == endpointId
        }
    }

    sealed class DiscoveryStatus {
        data object Initing : DiscoveryStatus()
        data class Discovering(val devicesNearby: SnapshotStateList<NearbyDevice>) :
            DiscoveryStatus()

        data object Failure : DiscoveryStatus()
    }

}

