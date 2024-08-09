package com.kastik.files.ui.screens.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kastik.files.nearby.managers.DownloadState
import com.kastik.files.ui.screens.shared.ConnectionDialog
import com.kastik.files.ui.screens.shared.MyCircularProgressIndicatorFloat
import com.kastik.files.ui.screens.shared.MyCircularProgressIndicatorText

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun DownloadScreen(modifier: Modifier = Modifier) {
    val viewModel: DownloadScreenViewModel = viewModel(factory = DownloadScreenViewModel.Factory)
    val state = viewModel.state.collectAsStateWithLifecycle()
    val text = viewModel.textState.collectAsStateWithLifecycle()

    val nearbyPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN,

            )
    )

    if (!nearbyPermissionState.allPermissionsGranted) {
        Column {
            val textToShow =
                "The app cannot scan for nearby devices without this permissions. Please grant the permission."
            Text(textToShow)
            Button(onClick = { nearbyPermissionState.launchMultiplePermissionRequest() }) {
                Text("Request permissions again")
            }
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            when (state.value) {
                is DownloadState.Downloading -> {
                    MyCircularProgressIndicatorFloat(
                        progress = (state.value as DownloadState.Downloading).progress,
                        text = text.value
                    )
                }

                is DownloadState.Extracting -> {
                    MyCircularProgressIndicatorFloat(
                        progress = (state.value as DownloadState.Extracting).progress,
                        text = text.value
                    )
                }

                is DownloadState.Authenticating -> {
                    ConnectionDialog(connectionResponse = (state.value as DownloadState.Authenticating).connectionResponse)
                }

                DownloadState.Initing -> {
                    MyCircularProgressIndicatorText(
                        text = text.value,
                        modifier = modifier.padding(10.dp)
                    )
                    Text(text = text.value)
                }


                else -> {
                    Text(text = text.value)
                }
            }
        }
    }
}

