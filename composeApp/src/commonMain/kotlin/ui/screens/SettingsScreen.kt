package ui.screens

import Platform
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import data.UserPreferences
import data.UserPreferencesRepo
import getDeviceName
import getPlatform
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.UIManager

@Composable
fun SettingsScreen(preferences: UserPreferencesRepo) {
    val scope = rememberCoroutineScope()
    val currentWorkingDirectory = Paths.get("").toAbsolutePath().toString()
    val userPreferences = preferences.userPreferencesFlow.collectAsState(
        UserPreferences(
            deviceName = getDeviceName(),
            deleteAfterZipping = false,
            saveDestination = currentWorkingDirectory,
            runBackgroundService = false,
            backgroundServiceTypeIsUploader = false,
            emptySet()
        )
    )

    Column {
        if (getPlatform() == Platform.Desktop) {
            changeSaveDestinationDesktop(userPreferences.value.saveDestination) { saveDestination ->
                scope.launch {
                    preferences.updateSaveDestination(saveDestination)
                }
            }
        } else {
            RunBackgroundService(
                userPreferences.value.runBackgroundService,
                userPreferences.value.backgroundServiceTypeIsUploader
            ) { run, runAsUploader ->
                scope.launch {
                    preferences.updateRunBackgroundService(run)
                    preferences.updateBackgroundServiceTypeIsUploader(runAsUploader)
                }
            }
            DeviceName(userPreferences.value.deviceName) { deviceName ->
                scope.launch {
                    preferences.updateDeviceName(deviceName)
                }
            }
        }

    }
}


@Composable
fun DeleteAfterZipping(
    deleteAfterZipping: Boolean,
    updateDeleteAfterZipping: (Boolean) -> Unit,
) {
    Row {
        Text("Delete files after zipping?")
        Switch(checked = deleteAfterZipping, onCheckedChange = {
            updateDeleteAfterZipping(it)
        })
    }

}


@Composable
fun RunBackgroundService(
    runBackgroundService: Boolean,
    runBackgroundServiceAsUploader: Boolean,
    updateRunBackgroundService: (run: Boolean, runAsUploader: Boolean) -> Unit,
) {
    Column {
        Row {
            Text("Want to run it automatic every day?")
            Switch(checked = runBackgroundService, onCheckedChange = {
                updateRunBackgroundService(it, runBackgroundServiceAsUploader)
            })
        }
        Row {
            Text("Run as a uploader?")
            Switch(checked = runBackgroundServiceAsUploader, onCheckedChange = {
                updateRunBackgroundService(runBackgroundService, it)
            })
        }

    }
}


@Preview
@Composable
fun DeviceName(
    deviceName: String,
    updateDeviceName: (String) -> Unit,
) {
    Row {
        Text("Device name ")
        TextField(
            value = deviceName,
            onValueChange = { text ->
                updateDeviceName(text)
            },
            label = { Text("Device name") }
        )
    }
}


@Composable
fun changeSaveDestinationDesktop(
    saveDestination: String,
    updateSaveDestination: (String) -> Unit,
) {
    //TODO Find a way for android, This will cause crash if it's not desktop
    Row {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        val fileChooser = JFileChooser("/").apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select a folder"
            approveButtonText = "Select"
            approveButtonToolTipText = "Select current directory as save destination"
        }
        Text("Current Save Destination: $saveDestination")
        Button(onClick = {
            //fileChooser.showOpenDialog(null /* OR null */)
            updateSaveDestination(fileChooser.selectedFile.absolutePath)
        }) {
            Text("Change Save Destination")
        }
    }
}