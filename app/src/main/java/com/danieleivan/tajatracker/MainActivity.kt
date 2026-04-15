package com.danieleivan.tajatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danieleivan.tajatracker.ui.home.MainMenuScreen
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeScreen
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeViewModel
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeViewModelFactory
import com.danieleivan.tajatracker.ui.settings.SettingsScreen
import com.danieleivan.tajatracker.ui.stats.WrappedStatsScreen
import com.danieleivan.tajatracker.ui.stats.WrappedStatsViewModel
import com.danieleivan.tajatracker.ui.stats.WrappedStatsViewModelFactory
import com.danieleivan.tajatracker.ui.theme.DrunkWrappedTheme

private enum class AppScreen {
    MENU,
    RECORD,
    STATS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrunkWrappedTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MENU) }

                val homeViewModel: DrunkWrappedHomeViewModel = viewModel(
                    factory = DrunkWrappedHomeViewModelFactory()
                )

                val statsViewModel: WrappedStatsViewModel = viewModel(
                    factory = WrappedStatsViewModelFactory()
                )

                when (currentScreen) {
                    AppScreen.MENU -> MainMenuScreen(
                        onNewRecord = { currentScreen = AppScreen.RECORD },
                        onOpenStats = { currentScreen = AppScreen.STATS },
                        onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                    )

                    AppScreen.RECORD -> DrunkWrappedHomeScreen(
                        viewModel = homeViewModel,
                        onBackToMenu = { currentScreen = AppScreen.MENU },
                        onOpenStats = { currentScreen = AppScreen.STATS }
                    )

                    AppScreen.STATS -> WrappedStatsScreen(
                        viewModel = statsViewModel,
                        onBack = { currentScreen = AppScreen.MENU }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        onBack = { currentScreen = AppScreen.MENU }
                    )
                }
            }
        }
    }
}

