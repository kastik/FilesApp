package com.kastik.files.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.kastik.files.data.ConnectionResponse
import com.kastik.files.data.NearbyDevice
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun MyCircularProgressIndicatorText(modifier: Modifier = Modifier, text: String) {
    Column(modifier = modifier) {
        CircularProgressIndicator(
            modifier = modifier
                .width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surface,
            strokeCap = StrokeCap.Square,
            strokeWidth = 4.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier,
            maxLines = 1,
            softWrap = false,
            overflow = Ellipsis,
        )
    }
}

@Composable
fun MyCircularProgressIndicatorFloat(modifier: Modifier = Modifier, text: String, progress: Float) {
    Column(modifier = modifier) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier
                .width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surface,
            strokeCap = StrokeCap.Square,
            strokeWidth = 4.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier,
            maxLines = 1,
            softWrap = false,
            overflow = Ellipsis,
        )
    }

}

@Composable
fun ConnectionDialog(modifier: Modifier = Modifier, connectionResponse: ConnectionResponse) {
    val dialogStatus = remember { mutableStateOf(true) }
    AnimatedVisibility(visible = dialogStatus.value) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {
                dialogStatus.value = false
                connectionResponse.rejectConnection()
            }, confirmButton = {
                TextButton(
                    onClick = {
                        connectionResponse.acceptConnection()
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = modifier,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        connectionResponse.rejectConnection()
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = modifier,
                    )
                }
            },
            title = {
                Text(
                    text = "Would you like to accept this connection?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = modifier,
                )
            },
            text = {
                Text(
                    text = " Make sure this pin matches on both devices ${(connectionResponse.pin)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = modifier,
                )
            }
        )
    }
}


@Preview
@Composable
fun DeviceView(modifier: Modifier = Modifier, device: NearbyDevice) {
    Column(modifier = modifier.clickable {
        device.connect()
    }) {
        Icon(
            Icons.Default.Devices,
            contentDescription = null,
            modifier = modifier.size(64.dp),
        )
        Text(text = device.endpointName)
    }
}