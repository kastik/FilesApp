@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import data.UserPreferencesRepo
import java.io.File

expect fun getPlatform(): Platform

expect fun filesToCompress(): List<File>

expect fun getDeviceName(): String


@Composable
expect fun GetPlatformNavigationBar(navController: NavHostController)

expect object AppContext

expect fun getDataDir(): String

expect enum class AvailableScreens {
    ;

    val icon: ImageVector
    val screenFunction: @Composable (preferences: UserPreferencesRepo, dialogState: MutableState<Boolean>) -> Unit

    @Composable
    fun render(
        preferences: UserPreferencesRepo,
        dialogState: MutableState<Boolean>,
    )
}

@Composable
expect fun ImageView(file: File): Unit

enum class Platform {
    Android, Desktop
}