import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import data.UserPreferencesRepo
import org.jetbrains.skia.Image
import ui.screens.SettingsScreen
import ui.screens.ZipScreen
import java.io.File
import java.net.InetAddress


actual fun getPlatform(): Platform {
    return Platform.Desktop
}

actual fun getDataDir(): String {
    val workingDirectory: String
    val OS = (System.getProperty("os.name")).uppercase()
    if (OS.contains("WIN")) {
        println("ContainsWin")
        workingDirectory = System.getenv("AppData")
    } else {
        println("!ContainsWin")
        workingDirectory = System.getProperty("user.home") //TODO Add linux data dir
    }
    return workingDirectory
}

actual fun filesToCompress(): List<File> {
    val fileList = mutableListOf<File>()
    val paths = listOf(
        File("C:\\Users\\kastik\\Downloads"),
        File("/home/kastik/Downloads"),
        File("C:\\Users\\kastik\\Desktop"),
        File("/home/kastik/Desktop"),
        File("C:\\Users\\kastik\\Pictures"),
        File("/home/kastik/Pictures"),
        File("C:\\Users\\kastik\\Videos"),
        File("/home/kastik/Videos"),
        File("C:\\Users\\kastik\\Pictures\\Screenshots")
    )
    val fileExtensions = listOf("mkv", "mp4", "jpg", "png", "gif")

    paths.forEach { dir ->
        dir.listFiles()?.filter {
            it.isFile && it.extension in fileExtensions
        }?.forEach { fileList.add(it) }
    }
    return fileList
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual enum class AvailableScreens(
    actual val icon: ImageVector,
    actual val screenFunction: @Composable (UserPreferencesRepo, dialogState: MutableState<Boolean>) -> Unit,
) {
    ZipScreen(
        icon = Icons.Default.Home,
        screenFunction = { preferences: UserPreferencesRepo, dialogState: MutableState<Boolean> ->
            ZipScreen(
                dialogState
            )
        }
    ),
    SettingsScreen(
        icon = Icons.Default.Settings,
        screenFunction = { preferences: UserPreferencesRepo, dialogState: MutableState<Boolean> ->
            SettingsScreen(
                preferences
            )
        }
    );

    @Composable
    actual fun render(
        preferences: UserPreferencesRepo,
        dialogState: MutableState<Boolean>,
    ) {
        screenFunction(preferences, dialogState)
    }
}

@Composable
actual fun ImageView(file: File) {
    Image(
        modifier = Modifier.size(128.dp),
        bitmap = Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap(),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        contentDescription = null
    )
}

actual object AppContext


@Composable
actual fun GetPlatformNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AvailableScreens.valueOf(
        backStackEntry?.destination?.route ?: AvailableScreens.entries[0].name
    )
    NavigationRail {
        AvailableScreens.entries.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = { Icon(item.icon, null) },
                label = { Text(item.name) },
                selected = currentScreen == item,
                onClick = { navController.navigate(item.name) }
            )
        }
    }
}

actual fun getDeviceName(): String {
    return try {
        InetAddress.getLocalHost().hostName
    } catch (E: Exception) {
        "Unknown"
    }
}