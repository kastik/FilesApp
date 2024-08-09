import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.UserPreferencesRepo
import ui.MainUILATEST

fun main() {
    val preferences = UserPreferencesRepo()
    val filesToCompress = filesToCompress()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FilesApp",
        ) {
            MaterialTheme {
                MainUILATEST(preferences, filesToCompress)
            }
        }
    }
}
