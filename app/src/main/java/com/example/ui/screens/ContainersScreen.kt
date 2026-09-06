package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.StorageContainer
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ContainerCard
import com.example.ui.components.NetworkStatusBar
import com.example.ui.components.QrCodeDisplayDialog
import com.example.ui.dialogs.AddEditContainerDialog
import com.example.viewmodel.StorageViewModel
import kotlinx.coroutines.launch

@Composable
fun ContainersScreen(
    viewModel: StorageViewModel,
    onContainerSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val containers by viewModel.allContainers.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddContainerDialog by remember { mutableStateOf(false) }
    var containerToEdit by remember { mutableStateOf<StorageContainer?>(null) }
    var containerToDelete by remember { mutableStateOf<StorageContainer?>(null) }

    var qrCodeContainer by remember { mutableStateOf<StorageContainer?>(null) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NetworkStatusBar(isOnline = isOnline)

            // Header info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "物理空间与容器映射",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "为真实收纳箱生成唯一二维码，贴在箱体外，扫码即可透视查验内含物",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Container list
            if (containers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂未创建任何收纳箱/空间",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击右下方 + 按钮创建第一个收纳空间",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(containers, key = { it.id }) { container ->
                        val itemCount = allItems.count { it.containerId == container.id }
                        ContainerCard(
                            container = container,
                            itemCount = itemCount,
                            onClick = {
                                viewModel.setSelectedContainerFilter(container.id)
                                onContainerSelected(container.id)
                            },
                            onQrClick = {
                                scope.launch {
                                    qrCodeBitmap = viewModel.generateContainerQrCode(container.boxCode)
                                    qrCodeContainer = container
                                }
                            },
                            onEditClick = {
                                containerToEdit = container
                            },
                            onDeleteClick = {
                                containerToDelete = container
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB to add container
        FloatingActionButton(
            onClick = { showAddContainerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_container_fab"),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "创建收纳箱")
        }
    }

    // Add container dialog
    if (showAddContainerDialog) {
        AddEditContainerDialog(
            initialContainer = null,
            onDismiss = { showAddContainerDialog = false },
            onSaved = { c -> viewModel.saveContainer(c) }
        )
    }

    // Edit container dialog
    containerToEdit?.let { c ->
        AddEditContainerDialog(
            initialContainer = c,
            onDismiss = { containerToEdit = null },
            onSaved = { updated -> viewModel.saveContainer(updated) }
        )
    }

    // Confirm delete dialog
    containerToDelete?.let { c ->
        ConfirmDeleteDialog(
            title = "删除收纳箱？",
            message = "删除收纳箱「${c.name}」后，箱内物品仍将保留在物品库中，仅清除箱体归属关联。",
            onConfirm = { viewModel.deleteContainer(c) },
            onDismiss = { containerToDelete = null }
        )
    }

    // QR Code Display Dialog
    qrCodeContainer?.let { c ->
        QrCodeDisplayDialog(
            container = c,
            qrBitmap = qrCodeBitmap,
            onDismiss = {
                qrCodeContainer = null
                qrCodeBitmap = null
            }
        )
    }
}
