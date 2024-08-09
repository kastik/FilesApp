package com.kastik.files.ui

import AvailableScreens
import GetPlatformNavigationBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainUI(preferences: UserPreferencesRepo) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AvailableScreens.valueOf(
        backStackEntry?.destination?.route ?: AvailableScreens.entries[0].name
    )

    var dialogState = remember { mutableStateOf(false) } //CHAT
    val showFab = remember { currentScreen == AvailableScreens.entries[0] }

    Scaffold(
        bottomBar = { GetPlatformNavigationBar(navController) },
        floatingActionButton = { FAB(showFab) { dialogState.value = true } } //TODO myFun()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AvailableScreens.entries[0].name,
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
        )
        {
            AvailableScreens.entries.forEach { screen ->
                composable(route = screen.name) { navBackStackEntry ->
                    screen.render(
                        preferences,
                        dialogState
                    )
                }
            }
        }
    }
}

@Composable
fun FAB(
    showFab: Boolean,
    onClick: () -> Unit,
) { //On click will trigger state change that'll start zipping
    AnimatedVisibility(
        showFab,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FloatingActionButton(
            onClick = { onClick() }
        ) {
            Icon(Icons.Default.FolderZip, stringResource(Res.string.start))
        }
    }
}