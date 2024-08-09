package com.kastik.files.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.kastik.files.data.ConnectionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileNotFoundException


class Connection(

    private val connectionClient: ConnectionsClient,
    private val payLoad: PayLoad,
    private val zipToSent: File?,
) : ConnectionLifecycleCallback() {

    private var autoAcceptConnection: Boolean = false
    private lateinit var autoConnectDevices: Set<String>

    constructor(
        connectionClient: ConnectionsClient,
        payLoad: PayLoad,
        zipToSent: File?,
        autoConnectDevices: Set<String>,
    ) : this(connectionClient, payLoad, zipToSent) {
        autoAcceptConnection = true
    }


    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.WaitingForConnection)
    val connectionState = _connectionStatus.asStateFlow()
    private var isReceiving = false

    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        isReceiving = connectionInfo.isIncomingConnection

        if (autoAcceptConnection && autoConnectDevices.contains(connectionInfo.endpointName)) {
            connectionClient.acceptConnection(endpointId, payLoad)
        } else {
            _connectionStatus.value =
                ConnectionStatus.Authenticating(
                    ConnectionResponse(
                        connectionInfo.authenticationDigits,
                        acceptConnection = {
                            connectionClient.acceptConnection(
                                endpointId,
                                payLoad
                            )
                        },
                        rejectConnection = { connectionClient.rejectConnection(endpointId) }
                    ))
        }
    }

    override fun onConnectionResult(
        endpointId: String,
        connectionResolution: ConnectionResolution,
    ) {


        if (!isReceiving) {
            when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    _connectionStatus.value = ConnectionStatus.Accepted
                    try {
                        val filePayload: Payload =
                            Payload.fromFile(zipToSent!!) //TODO find a safer way todo this
                        filePayload.setParentFolder("filesApp")
                        filePayload.setFileName("myZip.zip")
                        filePayload.setSensitive(true)
                        connectionClient.sendPayload(endpointId, filePayload)

                    } catch (e: FileNotFoundException) {
                        Log.e("MyApp", "File not found", e)
                        _connectionStatus.value = ConnectionStatus.Failure
                    }
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _connectionStatus.value = ConnectionStatus.Rejected
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    _connectionStatus.value = ConnectionStatus.Failure
                }

                else -> {
                    _connectionStatus.value = ConnectionStatus.Failure
                }
            }
        } else {
            when (connectionResolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    _connectionStatus.value = ConnectionStatus.Accepted
                }

                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _connectionStatus.value = ConnectionStatus.Rejected
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    _connectionStatus.value = ConnectionStatus.Failure
                }

                else -> {
                    _connectionStatus.value = ConnectionStatus.Failure
                }
            }
        }
    }

    override fun onDisconnected(endpointId: String) {
        //_connectionStatus.value = ConnectionStatus.Disconnected
    }


}

sealed class ConnectionStatus {
    data object Accepted : ConnectionStatus()
    data object Rejected : ConnectionStatus()
    data object Failure : ConnectionStatus()
    data object WaitingForConnection : ConnectionStatus()
    class Authenticating(val connectionResponse: ConnectionResponse) : ConnectionStatus()
    //data object Disconnected : ConnectionStatus()
}

