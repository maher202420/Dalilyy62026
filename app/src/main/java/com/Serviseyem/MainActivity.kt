package com.Serviseyem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.screens.MainScreen
import com.Serviseyem.ui.theme.ServiseyemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge content renders beautifully behind safe areas and gesture bars
        enableEdgeToEdge()

        setContent {
            ServiseyemTheme {
                val viewModel: AppViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
