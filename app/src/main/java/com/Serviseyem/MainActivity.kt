package com.Serviseyem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.screens.AdminScreen
import com.Serviseyem.screens.MainScreen
import com.Serviseyem.ui.theme.ServisEmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val navController = rememberNavController()

            ServisEmTheme(
                primaryColor = appViewModel.appPrimaryColor,
                secondaryColor = appViewModel.appSecondaryColor,
                backgroundColor = appViewModel.appBackgroundColor,
                textColor = appViewModel.appTextColor,
                fontFamily = appViewModel.appFontFamily
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            viewModel = appViewModel,
                            onNavigateToAdmin = {
                                navController.navigate("admin")
                            }
                        )
                    }
                    composable("admin") {
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
