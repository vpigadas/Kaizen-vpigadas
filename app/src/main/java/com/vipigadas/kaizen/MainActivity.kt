package com.vipigadas.kaizen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vipigadas.kaizen.features.sport.SportViewModel
import com.vipigadas.kaizen.ui.features.SportScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the application with Hilt integration.
 * The @AndroidEntryPoint annotation allows Hilt to inject dependencies into this activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // ViewModel is provided by Hilt
                val viewModel: SportViewModel = viewModel()

                // The UI is completely driven by the ViewModel state
                SportScreen(viewModel = viewModel)
            }
        }
    }
}