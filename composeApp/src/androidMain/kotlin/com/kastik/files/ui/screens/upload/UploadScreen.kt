package com.kastik.files.ui.screens.upload

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kastik.files.nearby.managers.UploadState
import com.kastik.files.ui.screens.shared.ConnectionDialog
import com.kastik.files.ui.screens.shared.DeviceView
import com.kastik.files.ui.screens.shared.MyCircularProgressIndicatorFloat
import com.kastik.files.ui.screens.shared.MyCircularProgressIndicatorText

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UploadScreen(modifier: Modifier = Modifier) {
    val viewModel: UploadScreenViewModel = viewModel(factory = UploadScreenViewModel.Factory)
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
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            when (state.value) {
                is UploadState.Downloading -> {
                    MyCircularProgressIndicatorFloat(
                        progress = (state.value as UploadState.Downloading).progress,
                        text = text.value
                    )
                }

                is UploadState.Authenticating -> {
                    ConnectionDialog(connectionResponse = (state.value as UploadState.Authenticating).connectionResponse)
                }

                is UploadState.DiscoveringDevices -> {
                    (state.value as UploadState.DiscoveringDevices).devicesNearby.forEach {
                        DeviceView(device = it)
                    }
                }

                UploadState.Initing -> {
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
