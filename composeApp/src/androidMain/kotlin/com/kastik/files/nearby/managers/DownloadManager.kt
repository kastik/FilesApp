package com.kastik.files.nearby.managers

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.kastik.files.data.ConnectionResponse
import com.kastik.files.nearby.Advertise
import com.kastik.files.nearby.Connection
import com.kastik.files.nearby.ConnectionStatus
import com.kastik.files.nearby.PayLoad
import com.kastik.files.nearby.PayloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DownloadManager(context: Context) {
    private val connectionClient = Nearby.getConnectionsClient(context)
    private val payLoad =
        PayLoad(context = context, isDownloader = true, deleteFilesAfterWork = true)
    private val connection = Connection(connectionClient, payLoad, null)
    private val advertise = Advertise(context, connection, connectionClient)
    private val _downloadManagerState: MutableStateFlow<DownloadState> =
        MutableStateFlow(DownloadState.Initing)
    val downloadManagerStateFlow = _downloadManagerState.asStateFlow()

    init {
        advertise.startAdvertising()
        CoroutineScope(Dispatchers.Main).launch {
            combine(
                advertise.advertiseState,
                connection.connectionState,
                payLoad.payloadState
            ) { advertise, connection, payLoad ->
                when (advertise) {
                    Advertise.AdvertiseStatus.Initing -> {
                        DownloadState.Initing
                    }

                    Advertise.AdvertiseStatus.Advertising -> {
                        when (connection) {
                            ConnectionStatus.WaitingForConnection -> {
                                DownloadState.Initing
                            }

                            is ConnectionStatus.Authenticating -> {
                                DownloadState.Authenticating(connection.connectionResponse)
                            }

                            ConnectionStatus.Rejected -> {
                                DownloadState.Rejected
                            }

                            ConnectionStatus.Failure -> {
                                DownloadState.Failure
                            }

                            ConnectionStatus.Accepted -> {
                                this@DownloadManager.advertise.stopAdvertising()
                                when (payLoad) {
                                    PayloadStatus.Error -> {
                                        DownloadState.Failure
                                    }

                                    PayloadStatus.Canceled -> {
                                        DownloadState.CanceledDownload
                                    }

                                    PayloadStatus.FinishedDownloading -> {
                                        DownloadState.FinishedDownload
                                    }

                                    is PayloadStatus.Sending -> {
                                        DownloadState.Downloading(payLoad.progress)
                                    }

                                    is PayloadStatus.Extracting -> DownloadState.Extracting(payLoad.progress)
                                }
                            }
                        }
                    }

                    Advertise.AdvertiseStatus.Failure -> {
                        DownloadState.Failure
                    }
                }
            }.collect { state ->
                _downloadManagerState.value = state
            }
        }
    }
}


sealed class DownloadState {
    data object Initing : DownloadState()
    data object Failure : DownloadState()
    class Authenticating(val connectionResponse: ConnectionResponse) : DownloadState()
    data object Rejected : DownloadState()
    data object FinishedDownload : DownloadState()
    class Downloading(val progress: Float) : DownloadState()
    data object CanceledDownload : DownloadState()
    class Extracting(val progress: Float) : DownloadState()
}

fun DownloadState.mapStateToText() {
    //TODO Maybe?
}