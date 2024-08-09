import android.app.Application
import android.content.Context
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.kastik.files.ui.screens.download.DownloadScreen
import com.kastik.files.ui.screens.upload.UploadScreen
import data.UserPreferencesRepo
import ui.screens.SettingsScreen
import ui.screens.ZipScreen
import java.io.File
import java.util.Locale


actual fun getPlatform(): Platform {
    return Platform.Android
}

actual fun getDataDir(): String {
    return AppContext.get().filesDir.toString()
}

actual object AppContext {
    private lateinit var application: Application

    fun setUp(context: Context) {
        application = context as Application
    }

    fun get(): Context {
        if (::application.isInitialized.not()) throw Exception("Application context isn't initialized")
        return application.applicationContext
    }
}


actual fun filesToCompress(): List<File> {
    val context = AppContext.get()
    val files: MutableList<File> = ArrayList()
    try {
        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        )

        val cursor = MergeCursor(
            arrayOf(
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    columns,
                    null,
                    null,
                    null
                ),
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    columns,
                    null,
                    null,
                    null
                ),
                context.contentResolver.query(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    columns,
                    null,
                    null,
                    null
                ),
                context.contentResolver.query(
                    MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                    columns,
                    null,
                    null,
                    null
                )
            )
        )
        cursor.moveToFirst()
        files.clear()
        while (!cursor.isAfterLast) {
            var path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val lastPoint = path.lastIndexOf(".")
            path = path.substring(0, lastPoint) + path.substring(lastPoint)
                .lowercase(Locale.getDefault())
            files.add(File(path))
            cursor.moveToNext()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return files
}


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual enum class AvailableScreens(
    actual val icon: ImageVector,
    actual val screenFunction: @Composable (UserPreferencesRepo, MutableState<Boolean>) -> Unit,
) {
    ZipScreen(
        icon = Icons.Default.Home,
        screenFunction = { preferences: UserPreferencesRepo, dialogState: MutableState<Boolean> ->
            ZipScreen(
                dialogState
            )
        }
    ),
    UploadScreen(
        icon = Icons.Default.Upload,
        screenFunction = { preferences: UserPreferencesRepo, dialogState: MutableState<Boolean> ->
            UploadScreen()
        }
    ),
    DownloadScreen(
        icon = Icons.Default.Download,
        screenFunction = { preferences: UserPreferencesRepo, dialogState: MutableState<Boolean> ->
            DownloadScreen()
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
    AsyncImage(
        model = file,
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp)),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
actual fun GetPlatformNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AvailableScreens.valueOf(
        backStackEntry?.destination?.route ?: AvailableScreens.entries[0].name
    )
    NavigationBar {
        AvailableScreens.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, null) },
                label = { Text(item.name) },
                selected = currentScreen == item,
                onClick = { navController.navigate(item.name) }
            )
        }
    }
}

actual fun getDeviceName(): String {
    return android.os.Build.DEVICE
}