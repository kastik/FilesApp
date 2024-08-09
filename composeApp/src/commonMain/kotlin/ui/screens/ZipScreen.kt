package ui.screens

import ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.zip
import filesToCompress
import getDataDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File


@Composable
@Preview
fun ZipScreen(
    dialogState: MutableState<Boolean>,
) {
    val filesToCompress =
        filesToCompress() //TODO WE NEED TO COMPRESS THIS FILES WHEN FAB IS CLICKED

    AnimatedVisibility(dialogState.value) {
        ZipWithProgressDialog(
            filesToCompress,
            getDataDir() + "/myZip.zip",
            { dialogState.value = false })
    }
    LazyVerticalGrid(
        columns = GridCells.FixedSize(
            size = 102.dp,
        ),
    ) {
        items(filesToCompress) { photo ->
            ImageView(photo)
        }
    }
}


@Composable
fun ZipWithProgressDialog(
    filesToCompress: List<File>,
    outputZipFilePath: String,
    onDismiss: () -> Unit,
) {
    var filesProcessed by remember { mutableStateOf(0) }
    val totalFiles = filesToCompress.size
    val scope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            zip(filesToCompress, outputZipFilePath) {
                filesProcessed = it
            }
        }
        onDismiss()
    }

    if (filesProcessed < totalFiles) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.surface,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Compressing files...")
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = filesProcessed / totalFiles.toFloat())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$filesProcessed / $totalFiles files processed")
                }
            }
        }
    }
}


