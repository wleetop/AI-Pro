package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AiVisionScreen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.ContainersScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.StorageViewModel

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppNavigation(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // If app is locked by security PIN, show AppLockScreen
    if (isAppLocked) {
        AppLockScreen(viewModel = viewModel)
        return
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavTabItem("物品库", Icons.Default.Inventory2, "nav_inventory"),
        NavTabItem("AI视觉", Icons.Default.AutoAwesome, "nav_ai_vision"),
        NavTabItem("空间箱", Icons.Default.Widgets, "nav_containers"),
        NavTabItem("扫码透视", Icons.Default.QrCodeScanner, "nav_scanner"),
        NavTabItem("设置", Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.border(BorderStroke(1.dp, Color(0xFFF1F5F9)))
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = {
                            Text(
                                item.title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> InventoryScreen(viewModel = viewModel)
                1 -> AiVisionScreen(
                    viewModel = viewModel,
                    onNavigateToInventory = { selectedTabIndex = 0 }
                )
                2 -> ContainersScreen(
                    viewModel = viewModel,
                    onContainerSelected = { containerId ->
                        // Switch to InventoryScreen with container filtered
                        selectedTabIndex = 0
                    }
                )
                3 -> ScannerScreen(
                    viewModel = viewModel,
                    onNavigateToInventory = { selectedTabIndex = 0 }
                )
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
