package com.Serviseyem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.screens.AdminScreen
import com.Serviseyem.screens.MainScreen
import com.Serviseyem.ui.theme.ServisEmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val navController = rememberNavController()

            ServisEmTheme(
                primaryColor = Color(0xFFFFD700), // Stable static default Gold Accent for general theme structure
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main_dashboard"
                ) {
                    composable("main_dashboard") {
                        MainScreen(
                            viewModel = appViewModel,
                            onNavigateToAdmin = {
                                navController.navigate("admin_dashboard")
                            }
                        )
                    }
                    composable("admin_dashboard") {
                        AdminScreen(
                            viewModel = appViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
