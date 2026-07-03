package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StockViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppMainContainer()
            }
        }
    }
}

sealed class Screen {
    object Dashboard : Screen()
    object Settings : Screen()
    data class ReportDetail(val symbol: String) : Screen()
}

@Composable
fun AppMainContainer() {
    val viewModel: StockViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Enforce RTL layout direction across the entire app for high fidelity Arabic feel
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                // Only show bottom navigation on main top-level screens (Dashboard & Settings)
                if (currentScreen is Screen.Dashboard || currentScreen is Screen.Settings) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { currentScreen = Screen.Dashboard },
                            icon = { Icon(Icons.Default.ShowChart, contentDescription = "السوق") },
                            label = { Text("السوق والأسهم") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Settings,
                            onClick = { currentScreen = Screen.Settings },
                            icon = { Icon(Icons.Default.Notifications, contentDescription = "التنبيهات") },
                            label = { Text("التنبيهات والاتصال") }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onStockClick = { symbol ->
                                viewModel.selectStock(symbol)
                                currentScreen = Screen.ReportDetail(symbol)
                            }
                        )
                    }
                    is Screen.Settings -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                    is Screen.ReportDetail -> {
                        ReportDetailScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                viewModel.selectStock(null)
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                }
            }
        }
    }
}
