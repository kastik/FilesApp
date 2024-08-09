package com.kastik.files.nearby.managers

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.nearby.Nearby
import com.kastik.files.data.ConnectionResponse
import com.kastik.files.data.NearbyDevice
import com.kastik.files.nearby.Connection
import com.kastik.files.nearby.ConnectionStatus
import com.kastik.files.nearby.Discovery
import com.kastik.files.nearby.PayLoad
import com.kastik.files.nearby.PayloadStatus
import getDataDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

class UploadManager(context: Context) {
    private val connectionClient = Nearby.getConnectionsClient(context)
    private val payLoad =
        PayLoad(context = context, isDownloader = false, deleteFilesAfterWork = true)
    private val connection =
        Connection(connectionClient, payLoad, File(getDataDir() + "/myZip.zip"))
    private val discovery = Discovery(context, connectionClient, connection)
    private val _uploadManagerState = MutableStateFlow<UploadState>(UploadState.Initing)
    val uploadManagerStateFlow = _uploadManagerState.asStateFlow()

    init {
        discovery.startDiscovery()
        CoroutineScope(Dispatchers.Main).launch {
            combine(
                discovery.discoveryState,
                connection.connectionState,
                payLoad.payloadState
            ) { discovery, connection, payLoad ->
                when (discovery) {
                    Discovery.DiscoveryStatus.Initing -> {
                        UploadState.Initing
                    }

                    Discovery.DiscoveryStatus.Failure -> {
                        UploadState.Failure
                    }

                    is Discovery.DiscoveryStatus.Discovering -> {
                        when (connection) {
                            ConnectionStatus.WaitingForConnection -> {
                                UploadState.DiscoveringDevices(discovery.devicesNearby)
                            }

                            is ConnectionStatus.Authenticating -> {
                                UploadState.Authenticating(connection.connectionResponse)
                            }

                            ConnectionStatus.Rejected -> {
                                UploadState.Rejected
                            }

                            ConnectionStatus.Failure -> {
                                UploadState.Failure
                            }

                            ConnectionStatus.Accepted -> {
                                this@UploadManager.discovery.stopDiscovery()
                                when (payLoad) {
                                    PayloadStatus.Error -> {
                                        UploadState.Failure
                                    }

                                    PayloadStatus.Canceled -> {
                                        UploadState.UploadCanceled

                                    }

                                    PayloadStatus.FinishedDownloading -> {
                                        UploadState.FinishedUpload
                                    }

                                    is PayloadStatus.Sending -> {
                                        UploadState.Downloading(payLoad.progress)
                                    }

                                    is PayloadStatus.Extracting -> TODO("This should never occur?")
                                }
                            }
                        }
                    }
                }
            }.collect { state ->
                _uploadManagerState.value = state
            }
        }
    }
}

sealed class UploadState {
    data object Initing : UploadState()
    data object Failure : UploadState()
    class DiscoveringDevices(val devicesNearby: SnapshotStateList<NearbyDevice>) : UploadState()
    class Authenticating(val connectionResponse: ConnectionResponse) : UploadState()
    data object Rejected : UploadState()
    data object FinishedUpload : UploadState()
    class Downloading(val progress: Float) : UploadState()
    data object UploadCanceled : UploadState()

}