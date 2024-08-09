package ui

import AvailableScreens
import GetPlatformNavigationBar
import Platform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import data.UserPreferencesRepo
import filesapp.composeapp.generated.resources.Res
import filesapp.composeapp.generated.resources.start
import getPlatform
import org.jetbrains.compose.resources.stringResource
import java.io.File


@Composable
fun MainUILATEST(preferences: UserPreferencesRepo, filesToCompress: List<File>) {
    //TODO USE getDataDir()
    // Get current back stack entry
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AvailableScreens.valueOf(
        backStackEntry?.destination?.route ?: AvailableScreens.entries[0].name
    )
    val dialogState = remember { mutableStateOf(false) }
    val dialogPinValue = remember { mutableStateOf("0000") }

    //val currentScreen = remember { mutableStateOf(AvailableScreens.entries[0]) }

    val showFab = remember { currentScreen == AvailableScreens.entries[0] }
    val startZipping = remember { mutableStateOf(false) }



    Scaffold(
        bottomBar = {
            if (getPlatform() == Platform.Android) {
                GetPlatformNavigationBar(navController)
            }
        },
        floatingActionButton = { FAB(showFab, startZipping) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AvailableScreens.entries[0].name, //todo set to 0
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            AvailableScreens.entries.forEach { screen ->
                composable(route = screen.name) { navBackStackEntry ->
                    Row {
                        if (getPlatform() == Platform.Desktop) {
                            GetPlatformNavigationBar(navController)
                        }
                        screen.render(
                            preferences,
                            dialogState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FAB(showFab: Boolean, startZipping: MutableState<Boolean>) {
    var temp: MutableState<Boolean> = mutableStateOf(true)

    AnimatedVisibility(
        showFab,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FloatingActionButton(
            onClick = { startZipping.value = true }
        ) {
            Icon(Icons.Default.Build, stringResource(Res.string.start))
        }
    }
}


