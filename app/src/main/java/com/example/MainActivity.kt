package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.MainAppNavigation
import com.example.ui.theme.StorageProTheme
import com.example.viewmodel.StorageViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StorageProTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Removed aggressive lockApp on backgrounding so lock only triggers upon fresh app launch/process restart
    }
}
