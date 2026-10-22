package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.StorageCategory
import com.example.data.StorageContainer
import com.example.data.StorageItem
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ExpiryAlertBanner
import com.example.ui.components.FullscreenImageViewerDialog
import com.example.ui.components.ItemCard
import com.example.ui.components.NetworkStatusBar
import com.example.ui.dialogs.AddEditItemDialog
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.MetricBlueBg
import com.example.ui.theme.MetricBlueLabel
import com.example.ui.theme.MetricBlueText
import com.example.ui.theme.MetricPurpleBg
import com.example.ui.theme.MetricPurpleLabel
import com.example.ui.theme.MetricPurpleText
import com.example.ui.theme.MetricRedBg
import com.example.ui.theme.MetricRedLabel
import com.example.ui.theme.MetricRedText
import com.example.util.ImageCompressor
import com.example.viewmodel.AiOperationState
import com.example.viewmodel.ItemSortOrder
import com.example.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.filteredItems.collectAsState()
    val allItemsList by viewModel.allItems.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val containers by viewModel.allContainers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedContainerFilter by viewModel.selectedContainerFilter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val onlyExpiringSoon by viewModel.onlyExpiringSoon.collectAsState()
    val semanticMatchedIds by viewModel.semanticMatchedIds.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiState by viewModel.aiState.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var aiPrefilledItem by remember { mutableStateOf<StorageItem?>(null) }
    var aiPrefilledImagePath by remember { mutableStateOf<String?>(null) }
    var itemToEdit by remember { mutableStateOf<StorageItem?>(null) }
    var itemToDelete by remember { mutableStateOf<StorageItem?>(null) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Hero AI Photo Picker (One-Tap Entry Point)
    val heroPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = ImageCompressor.decodeUriToBitmap(context, uri)
                if (bmp != null) {
                    viewModel.recognizeSinglePhoto(bmp)
                } else {
                    viewModel.showMessage("图片读取失败，请重新选择")
                }
            }
        }
    }

    // Auto-open Add Item dialog when AI recognition or local fallback finishes
    LaunchedEffect(aiState) {
        val state = aiState
        if (state is AiOperationState.SingleSuccess) {
            val res = state.result
            aiPrefilledItem = StorageItem(
                name = res.name,
                category = res.category,
                locationPath = res.suggestedLocation,
                quantity = res.quantity,
                unit = res.unit,
                price = res.estimatedPrice,
                tags = res.tags.joinToString(","),
                notes = res.storageTips,
                imageUri = state.compressedImagePath,
                expiryDate = res.expiryDays?.let { System.currentTimeMillis() + it.toLong() * 86400000L }
            )
            aiPrefilledImagePath = state.compressedImagePath
            showAddItemDialog = true
            viewModel.resetAiState()
        }
    }

    // Batch Management state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var showBatchMoveDialog by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }

    // Expiring calculation
    val now = System.currentTimeMillis()
    val expiringItems = remember(allItemsList) {
        val thirtyDays = 30L * 24 * 60 * 60 * 1000
        allItemsList.filter { it.expiryDate != null && it.expiryDate <= (now + thirtyDays) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Network & Offline Status Banner
            NetworkStatusBar(isOnline = isOnline)

            // Streamlined Clean Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "万物收纳",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AI 增强",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) AlertGreen else AlertAmber)
                    )
                }

                // Batch Mode Toggle
                OutlinedButton(
                    onClick = {
                        isSelectionMode = !isSelectionMode
                        if (!isSelectionMode) {
                            selectedItemIds = emptySet()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isSelectionMode) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                    modifier = Modifier.testTag("batch_mode_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.Checklist,
                        contentDescription = "批量管理",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSelectionMode) "退出批量" else "批量管理", style = MaterialTheme.typography.labelMedium)
                }
            }

            // 🌟 醒目的 AI 拍照识图 Hero 入口区域 (一键拍照识别入库 + 5秒容错降级)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("home_ai_vision_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI 拍照秒级识图",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "自动识别品名分类与收纳位置 (5秒智能降级)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "双引擎",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                heroPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp)
                                .testTag("home_ai_camera_hero_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📸 拍照识图入库", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                aiPrefilledItem = null
                                aiPrefilledImagePath = null
                                showAddItemDialog = true
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp)
                                .testTag("home_manual_add_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("手动录入", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // AI 实时识别中状态卡片 (具备 5 秒倒计时感知与交互提示)
            AnimatedVisibility(
                visible = aiState is AiOperationState.Loading,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val loadingMsg = (aiState as? AiOperationState.Loading)?.message ?: "AI 正在识别中..."
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "正在智能识图入库",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = loadingMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.resetAiState() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "取消", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 精简高效的 3 项水平胶囊统计条 (大幅降低纵向空间占用)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.clearAllFilters() },
                    shape = RoundedCornerShape(8.dp),
                    color = MetricPurpleBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("全部", style = MaterialTheme.typography.labelSmall, color = MetricPurpleLabel, fontWeight = FontWeight.Bold)
                        Text("${allItemsList.size}", style = MaterialTheme.typography.labelLarge, color = MetricPurpleText, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable { viewModel.toggleOnlyExpiringSoon() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (onlyExpiringSoon) MetricRedLabel.copy(alpha = 0.2f) else MetricRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("临期预警", style = MaterialTheme.typography.labelSmall, color = MetricRedLabel, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.getDefault(), "%02d", expiringItems.size), style = MaterialTheme.typography.labelLarge, color = MetricRedText, fontWeight = FontWeight.Black)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setSelectedCategory(null) },
                    shape = RoundedCornerShape(8.dp),
                    color = MetricBlueBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("空间箱", style = MaterialTheme.typography.labelSmall, color = MetricBlueLabel, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.getDefault(), "%02d", containers.size), style = MaterialTheme.typography.labelLarge, color = MetricBlueText, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Search Bar & Filter Controls
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("语义搜索: 去海边玩需要带什么?...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank() || semanticMatchedIds != null) {
                            IconButton(onClick = { viewModel.clearAllFilters() }) {
                                Icon(Icons.Default.Close, contentDescription = "清除搜索")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Semantic match notice chip if active
                if (semanticMatchedIds != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✨ 已启用 AI 语义寻物筛选 (匹配到 ${semanticMatchedIds!!.size} 件)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(onClick = { viewModel.clearAllFilters() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Active Container Filter Notice
                if (selectedContainerFilter != null) {
                    val cName = containers.firstOrNull { it.id == selectedContainerFilter }?.name ?: "当前收纳箱"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📦 正在查看箱内物品: $cName",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            IconButton(onClick = { viewModel.setSelectedContainerFilter(null) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "清除筛选", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Category Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedFilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setSelectedCategory(null) },
                        label = { Text("全部 (${allItemsList.size})") }
                    )
                    categories.forEach { cat ->
                        val count = allItemsList.count { it.category == cat.name }
                        ElevatedFilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = {
                                if (selectedCategory == cat.name) {
                                    viewModel.setSelectedCategory(null)
                                } else {
                                    viewModel.setSelectedCategory(cat.name)
                                }
                            },
                            label = { Text("${cat.name} ($count)") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sort & Expiry Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expiry pill
                    ElevatedFilterChip(
                        selected = onlyExpiringSoon,
                        onClick = { viewModel.toggleOnlyExpiringSoon() },
                        label = {
                            Text(
                                text = "⚠️ 临期筛选 (${expiringItems.size})",
                                color = if (expiringItems.isNotEmpty()) AlertAmber else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )

                    // Sort order dropdown button
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "排序", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOrder.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ItemSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Expiry Alert Banner
            if (!onlyExpiringSoon && expiringItems.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    ExpiryAlertBanner(
                        expiringItems = expiringItems,
                        onViewAllClick = { viewModel.toggleOnlyExpiringSoon() }
                    )
                }
            }

            // Section Header: 最近入库 + 批量选择提示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "已选 ${selectedItemIds.size} / ${items.size} 件",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (selectedItemIds.size == items.size) {
                                    selectedItemIds = emptySet()
                                } else {
                                    selectedItemIds = items.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(if (selectedItemIds.size == items.size) "取消全选" else "全选")
                        }
                    }
                } else {
                    Text(
                        text = if (selectedCategory != null) "分类物品 · $selectedCategory (${items.size})" else "最近物品 (${items.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "本地资产: ${allItemsList.size} 件",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无匹配物品",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击右下方 + 按钮或切换到「AI视觉」一键拍照入库",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(2.dp)) }
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onItemClick = { itemToEdit = item },
                            onImageClick = { previewImagePath = it },
                            onEditClick = { itemToEdit = item },
                            onDeleteClick = { itemToDelete = item },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItemIds.contains(item.id),
                            onToggleSelect = {
                                selectedItemIds = if (selectedItemIds.contains(item.id)) {
                                    selectedItemIds - item.id
                                } else {
                                    selectedItemIds + item.id
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(if (isSelectionMode) 100.dp else 80.dp)) }
                }
            }
        }

        // Floating Action Button for manual addition (hidden during selection mode)
        if (!isSelectionMode) {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_item_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "录入新物品")
            }
        }

        // Batch Action Bottom Bar
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Batch Move Container Action
                    Button(
                        onClick = { showBatchMoveDialog = true },
                        enabled = selectedItemIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("放入空间箱", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Batch Update Category Action
                    OutlinedButton(
                        onClick = { showBatchCategoryDialog = true },
                        enabled = selectedItemIds.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("修改分类", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Batch Delete Action
                    Button(
                        onClick = { showBatchDeleteConfirm = true },
                        enabled = selectedItemIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MetricRedText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("批量删除", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        }
    }

    // Batch Move To Container Dialog
    if (showBatchMoveDialog) {
        AlertDialog(
            onDismissRequest = { showBatchMoveDialog = false },
            title = { Text("批量放入收纳空间箱") },
            text = {
                Column {
                    Text("选择目标收纳箱，将选中的 ${selectedItemIds.size} 件物品一键移入：")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.batchMoveItemsToContainer(selectedItemIds, null)
                                        showBatchMoveDialog = false
                                        selectedItemIds = emptySet()
                                        isSelectionMode = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("移出当前收纳箱 (未分配空间)")
                                }
                            }
                        }
                        items(containers) { container ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.batchMoveItemsToContainer(selectedItemIds, container)
                                        showBatchMoveDialog = false
                                        selectedItemIds = emptySet()
                                        isSelectionMode = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(container.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(container.locationPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBatchMoveDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Batch Update Category Dialog
    if (showBatchCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showBatchCategoryDialog = false },
            title = { Text("批量修改物品分类") },
            text = {
                Column {
                    Text("选择目标分类，将选中的 ${selectedItemIds.size} 件物品统一归类：")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(categories) { cat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.batchUpdateCategory(selectedItemIds, cat.name)
                                        showBatchCategoryDialog = false
                                        selectedItemIds = emptySet()
                                        isSelectionMode = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBatchCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Batch Delete Confirm Dialog
    if (showBatchDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "批量删除物品确认",
            message = "确定要彻底删除已勾选的 ${selectedItemIds.size} 件物品吗？此操作将清除这些物品的所有本地记录，无法撤销。",
            onConfirm = {
                viewModel.batchDeleteItems(selectedItemIds)
                selectedItemIds = emptySet()
                isSelectionMode = false
                showBatchDeleteConfirm = false
            },
            onDismiss = { showBatchDeleteConfirm = false }
        )
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddEditItemDialog(
            initialItem = aiPrefilledItem,
            prefilledImagePath = aiPrefilledImagePath,
            categories = categories,
            containers = containers,
            viewModel = viewModel,
            onDismiss = {
                showAddItemDialog = false
                aiPrefilledItem = null
                aiPrefilledImagePath = null
            },
            onSaved = { item ->
                viewModel.saveItem(item)
                showAddItemDialog = false
                aiPrefilledItem = null
                aiPrefilledImagePath = null
            }
        )
    }

    // Edit Item Dialog
    itemToEdit?.let { item ->
        AddEditItemDialog(
            initialItem = item,
            categories = categories,
            containers = containers,
            viewModel = viewModel,
            onDismiss = { itemToEdit = null },
            onSaved = { updated -> viewModel.saveItem(updated) }
        )
    }

    // Confirm Delete Dialog
    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "确认删除物品？",
            message = "确定要将「${item.name}」从本地库中彻底删除吗？此操作无法撤销。",
            onConfirm = { viewModel.deleteItem(item) },
            onDismiss = { itemToDelete = null }
        )
    }

    // Fullscreen Image Viewer
    previewImagePath?.let { path ->
        FullscreenImageViewerDialog(
            imagePath = path,
            onDismiss = { previewImagePath = null }
        )
    }
}
