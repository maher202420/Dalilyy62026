package com.Serviseyem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.screens.AdminAppScreens
import com.Serviseyem.screens.MainAppScreens
import com.Serviseyem.ui.theme.ServisEmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = AppViewModel(application)
            var currentScreen by remember { mutableStateOf("main") } // "main" or "admin"

            ServisEmTheme(
                primaryColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.primaryColorHex)),
                backgroundColor = Color(android.graphics.Color.parseColor(viewModel.appSetup.backgroundColorHex))
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "main" -> {
                                MainAppScreens(
                                    viewModel = viewModel,
                                    onNavigateToAdminPortal = { currentScreen = "admin" }
                                )
                            }
                            "admin" -> {
                                AdminAppScreens(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "main" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

