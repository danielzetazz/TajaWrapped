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
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeScreen
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeViewModel
import com.danieleivan.tajatracker.ui.home.DrunkWrappedHomeViewModelFactory
import com.danieleivan.tajatracker.ui.stats.WrappedStatsScreen
import com.danieleivan.tajatracker.ui.stats.WrappedStatsViewModel
import com.danieleivan.tajatracker.ui.stats.WrappedStatsViewModelFactory
import com.danieleivan.tajatracker.ui.theme.DrunkWrappedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrunkWrappedTheme {
                var showStats by rememberSaveable { mutableStateOf(false) }

                val homeViewModel: DrunkWrappedHomeViewModel = viewModel(
                    factory = DrunkWrappedHomeViewModelFactory()
                )

                val statsViewModel: WrappedStatsViewModel = viewModel(
                    factory = WrappedStatsViewModelFactory()
                )

                if (showStats) {
                    WrappedStatsScreen(
                        viewModel = statsViewModel,
                        onBack = { showStats = false }
                    )
                } else {
                    DrunkWrappedHomeScreen(
                        viewModel = homeViewModel,
                        onOpenStats = { showStats = true }
                    )
                }
            }
        }
    }
}

