package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.util.ImageCompressor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import java.io.File
import com.example.ai.BatchRecognizedItem
import com.example.ai.DeclutterAdvice
import com.example.ai.ProviderType
import com.example.ai.ReceiptParseResult
import com.example.ai.ReceiptParsedItem
import com.example.ai.SingleItemRecognitionResult
import com.example.data.StorageContainer
import com.example.data.StorageItem
import com.example.ui.components.NetworkStatusBar
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.viewmodel.AiOperationState
import com.example.viewmodel.StorageViewModel

@Composable
fun AiVisionScreen(
    viewModel: StorageViewModel,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiState by viewModel.aiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()
    val containers by viewModel.allContainers.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    val allSpaces = remember(locations, containers, allItems) {
        val defaultSpaces = listOf("客厅", "主卧", "次卧", "书房", "厨房", "储物间", "阳台", "玄关", "卫生间", "衣帽间", "儿童房", "车库")
        val locNames = locations.map { it.name }
        val itemSpaces = allItems.mapNotNull { it.locationPath.split("/").firstOrNull()?.trim() }.filter { it.isNotBlank() }
        val containerSpaces = containers.mapNotNull { it.locationPath.split("/").firstOrNull()?.trim() }.filter { it.isNotBlank() }
        (defaultSpaces + locNames + itemSpaces + containerSpaces).distinct().filter { it.isNotBlank() }
    }

    var activePickerMode by remember { mutableStateOf<String?>(null) } // "single", "batch", "receipt"
    var semanticQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Dialog state for reviewing ListPlus batch items
    var batchReviewItems by remember { mutableStateOf<List<BatchRecognizedItem>?>(null) }
    var receiptReviewResult by remember { mutableStateOf<ReceiptParseResult?>(null) }
    var singleRecognitionResult by remember { mutableStateOf<Pair<SingleItemRecognitionResult, String?>?>(null) }

    // Pick visual media launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mode = activePickerMode
            coroutineScope.launch(Dispatchers.IO) {
                val bmp = ImageCompressor.decodeUriToBitmap(context, uri)
                if (bmp != null) {
                    when (mode) {
                        "single" -> viewModel.recognizeSinglePhoto(bmp)
                        "batch" -> viewModel.recognizeBatchListPlusPhoto(bmp)
                        "receipt" -> viewModel.parseReceiptPhoto(bmp)
                    }
                } else {
                    viewModel.showMessage("图片读取失败，请重新选择图片")
                }
            }
        }
    }

    // React to AI states
    when (val state = aiState) {
        is AiOperationState.SingleSuccess -> {
            singleRecognitionResult = Pair(state.result, state.compressedImagePath)
            viewModel.resetAiState()
        }
        is AiOperationState.BatchSuccess -> {
            batchReviewItems = state.items
            viewModel.resetAiState()
        }
        is AiOperationState.ReceiptSuccess -> {
            receiptReviewResult = state.result
            viewModel.resetAiState()
        }
        else -> {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Network state indicator
        NetworkStatusBar(isOnline = isOnline)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF0D9488))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI 视觉引擎",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "开放式 AI 视觉引擎",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "支持 Gemini 官方与主流 OpenAI 兼容视觉大模型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Engine Status Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (apiConfig.useLocalOfflineEngine) AlertGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (apiConfig.useLocalOfflineEngine) Icons.Default.Check else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (apiConfig.useLocalOfflineEngine) AlertGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (apiConfig.useLocalOfflineEngine) "本地离线智能引擎已激活" else "云端视觉大模型引擎",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (apiConfig.useLocalOfflineEngine) AlertGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (apiConfig.useLocalOfflineEngine) "免配置 API Key · 纯本地推断 · 100% 隐私安全 · 断网可用"
                            else if (apiConfig.apiKey.isNotBlank()) "已连接: ${if (apiConfig.provider == ProviderType.GEMINI) "Gemini (${apiConfig.geminiModel})" else "OpenAI (${apiConfig.customModel})"}"
                            else "⚠️ 未配置 API Key，点击右侧可一键开启本地智能引擎",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!apiConfig.useLocalOfflineEngine) {
                        OutlinedButton(
                            onClick = { viewModel.toggleLocalOfflineEngine(true) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("开启本地引擎", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.toggleLocalOfflineEngine(false) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("切换云端", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading / Streaming indicator banner if active
            AnimatedVisibility(visible = aiState is AiOperationState.Loading || aiState is AiOperationState.BatchStreaming) {
                val message = when (val s = aiState) {
                    is AiOperationState.Loading -> s.message
                    is AiOperationState.BatchStreaming -> s.statusText.ifBlank { "正在流式解析拆解物品..." }
                    else -> "AI 正在分析中..."
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Error banner if any
            AnimatedVisibility(visible = aiState is AiOperationState.Error) {
                val errorMsg = (aiState as? AiOperationState.Error)?.errorMsg ?: "操作遇到问题"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 服务提示", fontWeight = FontWeight.Bold, color = AlertRed, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.resetAiState() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "关闭", tint = AlertRed, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.toggleLocalOfflineEngine(true)
                                    if (activePickerMode == "batch") {
                                        viewModel.retryLastBatchRecognition()
                                    } else {
                                        viewModel.retryLastRecognition()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AlertGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("立即切换为本地引擎并重新识别 (免 Key · 秒级完成)", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 1. Core Feature 1: Single item fast photo recognition
            AiFeatureCard(
                title = "单件智能识别 (拍图即填表)",
                description = "拍摄单件物品，AI 自动解析名称、分类、推荐存放空间及保质保养建议，省去繁琐手工输入。",
                icon = Icons.Default.CameraAlt,
                badgeText = "极速入库",
                badgeColor = Color(0xFF3B82F6),
                buttonText = "拍照 / 选图识别",
                onClick = {
                    activePickerMode = "single"
                    imagePickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Core Feature 2: ListPlus 批量全家福识图
            AiFeatureCard(
                title = "ListPlus 批量识图入库 (全家福)",
                description = "拍摄整桌或货架多件物品“全家福”，AI 视觉自动拆解画面生成结构化清单，勾选后一键批量存入！",
                icon = Icons.Default.DynamicFeed,
                badgeText = "Pro 级核心",
                badgeColor = Color(0xFF0D9488),
                buttonText = "拍摄物品全家福",
                onClick = {
                    activePickerMode = "batch"
                    imagePickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Scenario Extension: Receipt & Express Parcel Parser
            AiFeatureCard(
                title = "购物小票 / 快递面单解析",
                description = "拍照识别超市小票、网购清单或快递单，AI 自动提取商家、商品明细、购买价格与分类，快捷归类入库。",
                icon = Icons.Default.ReceiptLong,
                badgeText = "记账与溯源",
                badgeColor = Color(0xFFF59E0B),
                buttonText = "解析购物小票",
                onClick = {
                    activePickerMode = "receipt"
                    imagePickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Scenario Extension: Semantic Natural Language Search
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "语义寻物",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "语义化模糊寻物助手",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "基于自然语言找东西。例如输入「去海边玩需要带什么？」或「修理电器的工具有哪些？」，自动筛选匹配库内物品。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = semanticQuery,
                        onValueChange = { semanticQuery = it },
                        placeholder = { Text("例如: 去海边玩需要带什么？") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (semanticQuery.isNotBlank()) {
                                        viewModel.executeSemanticSearch(semanticQuery)
                                        onNavigateToInventory()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "语义检索", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("去海边玩需要带什么？", "药箱里有哪些感冒药？", "出差数码装备").forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    semanticQuery = prompt
                                    viewModel.executeSemanticSearch(prompt)
                                    onNavigateToInventory()
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Scenario Extension: Declutter Advisor (断舍离建议)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "断舍离建议",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "极简生活 · 断舍离建议生成",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI 根据物品更新时间、闲置周期与临期状态，智能生成「本月建议丢弃/闲鱼转让」清单。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.generateDeclutterRecommendations() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("一键生成本月断舍离建议")
                    }

                    // Display Declutter advice list if present
                    if (aiState is AiOperationState.DeclutterSuccess) {
                        val advices = (aiState as AiOperationState.DeclutterSuccess).adviceList
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("💡 断舍离建议列表 (${advices.size}项):", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        advices.forEach { advice ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(advice.itemName, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (advice.actionType.contains("丢弃")) AlertRed.copy(alpha = 0.2f) else AlertAmber.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = advice.actionType,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (advice.actionType.contains("丢弃")) AlertRed else AlertAmber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "理由: ${advice.reason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Review Dialog for Single Item Recognition
    singleRecognitionResult?.let { (recResult, savedImgPath) ->
        SingleItemReviewDialog(
            result = recResult,
            imagePath = savedImgPath,
            containers = containers,
            allSpaces = allSpaces,
            onConfirm = { item ->
                viewModel.saveItem(item)
                singleRecognitionResult = null
                onNavigateToInventory()
            },
            onDismiss = { singleRecognitionResult = null }
        )
    }

    // Review Dialog for ListPlus Batch Items
    batchReviewItems?.let { items ->
        ListPlusBatchReviewDialog(
            initialItems = items,
            fullSourceBitmap = viewModel.lastBatchBitmap,
            allSpaces = allSpaces,
            allContainers = containers,
            onConfirm = { selectedItems ->
                val itemsToSave = selectedItems.map { b ->
                    StorageItem(
                        name = b.name,
                        category = b.category,
                        locationPath = b.selectedSpace.ifBlank { b.suggestedLocation },
                        containerId = b.containerId,
                        quantity = b.quantity,
                        unit = b.unit,
                        imageUri = b.imagePath,
                        tags = b.tags.joinToString(",")
                    )
                }
                viewModel.batchSaveItems(itemsToSave)
                batchReviewItems = null
                onNavigateToInventory()
            },
            onDismiss = { batchReviewItems = null }
        )
    }

    // Review Dialog for Receipt Parse
    receiptReviewResult?.let { receipt ->
        ReceiptReviewDialog(
            receipt = receipt,
            allSpaces = allSpaces,
            onConfirm = { items, targetSpace ->
                val itemsToSave = items.map { r ->
                    StorageItem(
                        name = r.name,
                        category = r.category,
                        locationPath = targetSpace,
                        price = r.price,
                        quantity = r.quantity,
                        unit = r.unit,
                        notes = "购自 ${receipt.merchantName} (${receipt.purchaseDate})",
                        tags = "小票入库,${receipt.merchantName}"
                    )
                }
                viewModel.batchSaveItems(itemsToSave)
                receiptReviewResult = null
                onNavigateToInventory()
            },
            onDismiss = { receiptReviewResult = null }
        )
    }
}

@Composable
fun AiFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = badgeColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = badgeColor)
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

@Composable
fun SingleItemReviewDialog(
    result: SingleItemRecognitionResult,
    imagePath: String?,
    containers: List<StorageContainer> = emptyList(),
    allSpaces: List<String> = emptyList(),
    onConfirm: (StorageItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(result.name) }
    var category by remember { mutableStateOf(result.category) }
    var quantity by remember { mutableStateOf(result.quantity.toString()) }
    var unit by remember { mutableStateOf(result.unit) }
    var tips by remember { mutableStateOf(result.storageTips) }
    var selectedContainerId by remember { mutableStateOf<Long?>(null) }

    // 解析 AI 推荐的存放位置，拆分为 主空间 (如: 书房) 与 细分位置 (如: 书桌抽屉)
    val parsedAiLocation = remember(result.suggestedLocation) {
        val parts = result.suggestedLocation.split("/").map { it.trim() }.filter { it.isNotBlank() }
        val root = parts.firstOrNull() ?: "客厅"
        val sub = if (parts.size > 1) parts.drop(1).joinToString("/") else ""
        Pair(root, sub)
    }

    var selectedSpace by remember { mutableStateOf(parsedAiLocation.first) }
    var subLocation by remember { mutableStateOf(parsedAiLocation.second) }
    var fullLocationPath by remember { mutableStateOf(result.suggestedLocation.ifBlank { "客厅" }) }

    fun updateLocation(space: String, sub: String) {
        selectedSpace = space
        subLocation = sub
        fullLocationPath = if (sub.isNotBlank()) {
            if (space.isNotBlank()) "$space/$sub" else sub
        } else {
            space
        }
    }

    val finalSpaces = remember(allSpaces) {
        val base = if (allSpaces.isEmpty()) {
            listOf("客厅", "主卧", "次卧", "书房", "厨房", "储物间", "阳台", "玄关", "卫生间", "衣帽间", "儿童房", "车库")
        } else {
            allSpaces
        }
        base.distinct()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "AI 识别结果确认",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "支持手动切换所属空间与细分存放位置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("物品名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("分类") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量") },
                        modifier = Modifier.weight(0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // 核心区域：所属空间手动选择与微调
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "物品所属空间 (手动选择)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (result.suggestedLocation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        updateLocation(parsedAiLocation.first, parsedAiLocation.second)
                                        fullLocationPath = result.suggestedLocation
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ AI 推荐位置: ${result.suggestedLocation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "点此采用",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "选择所属主空间：",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            finalSpaces.forEach { space ->
                                FilterChip(
                                    selected = selectedSpace == space,
                                    onClick = {
                                        updateLocation(space, subLocation)
                                    },
                                    label = { Text(space) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = subLocation,
                            onValueChange = {
                                updateLocation(selectedSpace, it)
                            },
                            label = { Text("细分存放位置 (选填)") },
                            placeholder = { Text("如: 衣柜第2层 / 电视柜抽屉 / 收纳盒") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fullLocationPath,
                            onValueChange = {
                                fullLocationPath = it
                                val p = it.split("/").map { s -> s.trim() }
                                if (p.isNotEmpty()) selectedSpace = p.first()
                                if (p.size > 1) subLocation = p.drop(1).joinToString("/")
                            },
                            label = { Text("最终收纳路径预览/手动微调") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (containers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "或直接绑定实体收纳箱 (可选)：",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedContainerId == null,
                                    onClick = { selectedContainerId = null },
                                    label = { Text("不绑定收纳箱") }
                                )
                                containers.forEach { box ->
                                    FilterChip(
                                        selected = selectedContainerId == box.id,
                                        onClick = {
                                            selectedContainerId = box.id
                                            fullLocationPath = box.locationPath
                                            val p = box.locationPath.split("/").map { s -> s.trim() }
                                            if (p.isNotEmpty()) selectedSpace = p.first()
                                            if (p.size > 1) subLocation = p.drop(1).joinToString("/")
                                        },
                                        label = { Text("📦 ${box.name}") }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (tips.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💡 收纳建议: $tips",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("放弃") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val days = result.expiryDays
                            val expiry = if (days != null && days > 0) System.currentTimeMillis() + days * 24L * 3600 * 1000 else null
                            val item = StorageItem(
                                name = name,
                                category = category,
                                locationPath = fullLocationPath.ifBlank { selectedSpace },
                                containerId = selectedContainerId,
                                quantity = quantity.toIntOrNull() ?: 1,
                                unit = unit,
                                expiryDate = expiry,
                                price = result.estimatedPrice,
                                imageUri = imagePath,
                                tags = result.tags.joinToString(","),
                                notes = tips
                            )
                            onConfirm(item)
                        }
                    ) {
                        Text("确认识别并存库")
                    }
                }
            }
        }
    }
}

@Composable
fun ListPlusBatchReviewDialog(
    initialItems: List<BatchRecognizedItem>,
    fullSourceBitmap: Bitmap? = null,
    allSpaces: List<String> = emptyList(),
    allContainers: List<StorageContainer> = emptyList(),
    onConfirm: (List<BatchRecognizedItem>) -> Unit,
    onDismiss: () -> Unit
) {
    val itemsState = remember { mutableStateListOf(*initialItems.toTypedArray()) }
    val spacesList = remember {
        mutableStateListOf(
            *(if (allSpaces.isNotEmpty()) allSpaces else listOf("客厅", "主卧", "次卧", "书房", "厨房", "储物间", "阳台", "玄关", "卫生间", "衣帽间")).toTypedArray()
        )
    }

    var showAddSpaceDialog by remember { mutableStateOf(false) }
    var newSpaceName by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<BatchRecognizedItem?>(null) }
    var selectedBatchContainer by remember { mutableStateOf<StorageContainer?>(null) }
    var batchContainerDropdownExpanded by remember { mutableStateOf(false) }
    var locatingItemIndex by remember { mutableStateOf<Int?>(null) }

    val dialogContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var photoReplacingItemId by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && photoReplacingItemId != null) {
            val targetId = photoReplacingItemId!!
            coroutineScope.launch {
                val savedPath = ImageCompressor.compressAndSaveImage(dialogContext, uri)
                val idx = itemsState.indexOfFirst { it.id == targetId }
                if (idx >= 0) {
                    itemsState[idx] = itemsState[idx].copy(imagePath = savedPath)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ListPlus 批量识图确认",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "画面共拆解 ${itemsState.size} 件物品，支持批量或单独存入空间箱与空间",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 全景全家福交互画布 (支持点击直接一秒纠偏截图)
                if (fullSourceBitmap != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (locatingItemIndex != null)
                                    "👉 请在下方原图点击「${itemsState[locatingItemIndex!!].name}」的准确中心点"
                                else "📷 原图全景视角 (图片截偏了？点击下方 [🎯定位] 即可在图上点按重裁):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (locatingItemIndex != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (locatingItemIndex != null) {
                                TextButton(
                                    onClick = { locatingItemIndex = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("取消定位", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(
                                    width = if (locatingItemIndex != null) 2.dp else 1.dp,
                                    color = if (locatingItemIndex != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            val containerWidth = constraints.maxWidth.toFloat()
                            val containerHeight = constraints.maxHeight.toFloat()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(locatingItemIndex) {
                                        detectTapGestures { offset ->
                                            val idx = locatingItemIndex
                                            if (idx != null && idx in itemsState.indices && fullSourceBitmap != null) {
                                                val normX = (offset.x / containerWidth).coerceIn(0.05f, 0.95f)
                                                val normY = (offset.y / containerHeight).coerceIn(0.05f, 0.95f)

                                                coroutineScope.launch {
                                                    val newPath = ImageCompressor.cropByCenterPoint(
                                                        context = dialogContext,
                                                        sourceBitmap = fullSourceBitmap,
                                                        normalizedX = normX,
                                                        normalizedY = normY
                                                    )
                                                    val boxYmin = ((normY - 0.12f) * 1000).toInt().coerceIn(0, 1000)
                                                    val boxXmin = ((normX - 0.12f) * 1000).toInt().coerceIn(0, 1000)
                                                    val boxYmax = ((normY + 0.12f) * 1000).toInt().coerceIn(boxYmin + 1, 1000)
                                                    val boxXmax = ((normX + 0.12f) * 1000).toInt().coerceIn(boxXmin + 1, 1000)

                                                    itemsState[idx] = itemsState[idx].copy(
                                                        imagePath = newPath,
                                                        boxNorm = listOf(boxYmin, boxXmin, boxYmax, boxXmax)
                                                    )
                                                    locatingItemIndex = null
                                                }
                                            }
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = fullSourceBitmap,
                                    contentDescription = "全景参照图",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                                // 绘制各物品在全景图上的数字标定点 Pin
                                itemsState.forEachIndexed { i, item ->
                                    val box = item.boxNorm
                                    if (box != null && box.size == 4) {
                                        val cxNorm = (box[1] + box[3]) / 2000f
                                        val cyNorm = (box[0] + box[2]) / 2000f
                                        val isTarget = locatingItemIndex == i

                                        val density = LocalDensity.current
                                        val pxX = (containerWidth * cxNorm)
                                        val pxY = (containerHeight * cyNorm)
                                        val dpX = with(density) { pxX.toDp() } - 12.dp
                                        val dpY = with(density) { pxY.toDp() } - 12.dp

                                        Surface(
                                            shape = CircleShape,
                                            color = if (isTarget) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.75f),
                                            border = BorderStroke(1.5.dp, Color.White),
                                            modifier = Modifier
                                                .offset(x = dpX, y = dpY)
                                                .size(24.dp)
                                                .clickable {
                                                    locatingItemIndex = i
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${i + 1}",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 空间快速管理与空间箱指派
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Batch Container Assignment Row
                        if (allContainers.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "批量放入空间箱:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedBatchContainer != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { batchContainerDropdownExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedBatchContainer?.let { "📦 ${it.name}" } ?: "选择目标空间箱",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedBatchContainer != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = batchContainerDropdownExpanded,
                                        onDismissRequest = { batchContainerDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("不放入特定箱子 (仅按空间存放)") },
                                            onClick = {
                                                selectedBatchContainer = null
                                                for (i in itemsState.indices) {
                                                    if (itemsState[i].isSelected) {
                                                        itemsState[i] = itemsState[i].copy(
                                                            containerId = null,
                                                            containerName = null
                                                        )
                                                    }
                                                }
                                                batchContainerDropdownExpanded = false
                                            }
                                        )
                                        allContainers.forEach { box ->
                                            DropdownMenuItem(
                                                text = { Text("📦 ${box.name} (${box.locationPath})") },
                                                onClick = {
                                                    selectedBatchContainer = box
                                                    for (i in itemsState.indices) {
                                                        if (itemsState[i].isSelected) {
                                                            itemsState[i] = itemsState[i].copy(
                                                                containerId = box.id,
                                                                containerName = box.name,
                                                                selectedSpace = box.locationPath,
                                                                suggestedLocation = box.locationPath
                                                            )
                                                        }
                                                    }
                                                    batchContainerDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "批量指定存放空间:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { showAddSpaceDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("新增空间", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            spacesList.forEach { space ->
                                AssistChip(
                                    onClick = {
                                        for (i in itemsState.indices) {
                                            if (itemsState[i].isSelected) {
                                                itemsState[i] = itemsState[i].copy(
                                                    suggestedLocation = space,
                                                    selectedSpace = space
                                                )
                                            }
                                        }
                                    },
                                    label = { Text(space) },
                                    trailingIcon = if (spacesList.size > 1) {
                                        {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "移除空间 $space",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        spacesList.remove(space)
                                                    }
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item List with independent cropped thumbnail, inline edit & space/container picker
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(itemsState) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { checked ->
                                        val idx = itemsState.indexOfFirst { it.id == item.id }
                                        if (idx >= 0) {
                                            itemsState[idx] = itemsState[idx].copy(isSelected = checked)
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Item Cropped Thumbnail Display with Tap-to-Replace
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            photoReplacingItemId = item.id
                                            photoPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.imagePath != null && File(item.imagePath!!).exists()) {
                                        AsyncImage(
                                            model = File(item.imagePath!!),
                                            contentDescription = item.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    RoundedCornerShape(topStart = 6.dp)
                                                )
                                                .padding(2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "更换照片",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "更换照片",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Item Info & Space/Container Dropdown
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${item.name}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "x${item.quantity}${item.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = "分类: ${item.category}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (item.containerName != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = "📦 ${item.containerName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (fullSourceBitmap != null) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (locatingItemIndex == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.clickable {
                                                    locatingItemIndex = if (locatingItemIndex == index) null else index
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (locatingItemIndex == index) "🎯重裁中" else "🎯定位",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (locatingItemIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Per-Item Space Selector Dropdown
                                        var spaceDropdownExpanded by remember { mutableStateOf(false) }
                                        Box {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                modifier = Modifier.clickable { spaceDropdownExpanded = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = item.selectedSpace.ifBlank { item.suggestedLocation },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Icon(
                                                        Icons.Default.ArrowDropDown,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = spaceDropdownExpanded,
                                                onDismissRequest = { spaceDropdownExpanded = false }
                                            ) {
                                                spacesList.forEach { sp ->
                                                    DropdownMenuItem(
                                                        text = { Text(sp) },
                                                        onClick = {
                                                            val idx = itemsState.indexOfFirst { it.id == item.id }
                                                            if (idx >= 0) {
                                                                itemsState[idx] = itemsState[idx].copy(
                                                                    suggestedLocation = sp,
                                                                    selectedSpace = sp
                                                                )
                                                            }
                                                            spaceDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Per-Item Container Selector Dropdown
                                        if (allContainers.isNotEmpty()) {
                                            var containerDropdownExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (item.containerId != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.clickable { containerDropdownExpanded = true }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Inventory2,
                                                            contentDescription = null,
                                                            tint = if (item.containerId != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = item.containerName ?: "选择箱子",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (item.containerId != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Icon(
                                                            Icons.Default.ArrowDropDown,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = containerDropdownExpanded,
                                                    onDismissRequest = { containerDropdownExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("无空间箱") },
                                                        onClick = {
                                                            val idx = itemsState.indexOfFirst { it.id == item.id }
                                                            if (idx >= 0) {
                                                                itemsState[idx] = itemsState[idx].copy(
                                                                    containerId = null,
                                                                    containerName = null
                                                                )
                                                            }
                                                            containerDropdownExpanded = false
                                                        }
                                                    )
                                                    allContainers.forEach { box ->
                                                        DropdownMenuItem(
                                                            text = { Text("📦 ${box.name}") },
                                                            onClick = {
                                                                val idx = itemsState.indexOfFirst { it.id == item.id }
                                                                if (idx >= 0) {
                                                                    itemsState[idx] = itemsState[idx].copy(
                                                                        containerId = box.id,
                                                                        containerName = box.name,
                                                                        selectedSpace = box.locationPath,
                                                                        suggestedLocation = box.locationPath
                                                                    )
                                                                }
                                                                containerDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Edit button to fine-tune item details
                                IconButton(
                                    onClick = { editingItem = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "编辑",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedCount = itemsState.count { it.isSelected }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                val allSelected = itemsState.all { it.isSelected }
                                for (i in itemsState.indices) {
                                    itemsState[i] = itemsState[i].copy(isSelected = !allSelected)
                                }
                            }
                        ) {
                            Text(if (itemsState.all { it.isSelected }) "全不选" else "全选")
                        }
                        Text("已选: $selectedCount 件", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                    }

                    Row {
                        OutlinedButton(onClick = onDismiss) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onConfirm(itemsState.filter { it.isSelected })
                            },
                            enabled = selectedCount > 0
                        ) {
                            Text("批量入库 ($selectedCount)")
                        }
                    }
                }
            }
        }
    }

    // Modal to add custom space
    if (showAddSpaceDialog) {
        Dialog(onDismissRequest = { showAddSpaceDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("新增存放空间", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newSpaceName,
                        onValueChange = { newSpaceName = it },
                        label = { Text("空间名称 (例如: 书房/顶层储物柜)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showAddSpaceDialog = false }) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newSpaceName.isNotBlank() && !spacesList.contains(newSpaceName.trim())) {
                                    spacesList.add(0, newSpaceName.trim())
                                    newSpaceName = ""
                                }
                                showAddSpaceDialog = false
                            },
                            enabled = newSpaceName.isNotBlank()
                        ) {
                            Text("添加")
                        }
                    }
                }
            }
        }
    }

    // Modal to edit single item in batch
    editingItem?.let { item ->
        var editName by remember { mutableStateOf(item.name) }
        var editCategory by remember { mutableStateOf(item.category) }
        var editQuantity by remember { mutableStateOf(item.quantity.toString()) }
        var editUnit by remember { mutableStateOf(item.unit) }
        var editSpace by remember { mutableStateOf(item.selectedSpace.ifBlank { item.suggestedLocation }) }
        var editContainerId by remember { mutableStateOf(item.containerId) }

        Dialog(onDismissRequest = { editingItem = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("修改物品信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("物品名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = { editCategory = it },
                            label = { Text("分类") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editQuantity,
                            onValueChange = { editQuantity = it },
                            label = { Text("数量") },
                            modifier = Modifier.weight(0.5f)
                        )
                        OutlinedTextField(
                            value = editUnit,
                            onValueChange = { editUnit = it },
                            label = { Text("单位") },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editSpace,
                        onValueChange = { editSpace = it },
                        label = { Text("存放空间") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (allContainers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        var editContainerDropdownExpanded by remember { mutableStateOf(false) }
                        val currentSelectedBox = allContainers.find { it.id == editContainerId }
                        Box {
                            OutlinedButton(
                                onClick = { editContainerDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentSelectedBox?.let { "归属空间箱: ${it.name}" } ?: "未指定空间箱 (独立存放)")
                            }
                            DropdownMenu(
                                expanded = editContainerDropdownExpanded,
                                onDismissRequest = { editContainerDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("无空间箱") },
                                    onClick = {
                                        editContainerId = null
                                        editContainerDropdownExpanded = false
                                    }
                                )
                                allContainers.forEach { box ->
                                    DropdownMenuItem(
                                        text = { Text("📦 ${box.name} (${box.locationPath})") },
                                        onClick = {
                                            editContainerId = box.id
                                            editSpace = box.locationPath
                                            editContainerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { editingItem = null }) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val idx = itemsState.indexOfFirst { it.id == item.id }
                                if (idx >= 0) {
                                    val boxName = allContainers.find { it.id == editContainerId }?.name
                                    itemsState[idx] = itemsState[idx].copy(
                                        name = editName.ifBlank { item.name },
                                        category = editCategory.ifBlank { item.category },
                                        quantity = editQuantity.toIntOrNull() ?: item.quantity,
                                        unit = editUnit.ifBlank { item.unit },
                                        suggestedLocation = editSpace,
                                        selectedSpace = editSpace,
                                        containerId = editContainerId,
                                        containerName = boxName
                                    )
                                }
                                editingItem = null
                            }
                        ) {
                            Text("保存修改")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptReviewDialog(
    receipt: ReceiptParseResult,
    allSpaces: List<String> = emptyList(),
    onConfirm: (List<ReceiptParsedItem>, String) -> Unit,
    onDismiss: () -> Unit
) {
    val itemsState = remember { mutableStateListOf(*receipt.items.toTypedArray()) }
    val spaces = remember(allSpaces) {
        if (allSpaces.isNotEmpty()) allSpaces else listOf("厨房/冰箱", "储物间", "日常居家/储物柜", "客厅", "主卧", "卫生间")
    }
    var targetSpace by remember { mutableStateOf(spaces.firstOrNull() ?: "厨房/冰箱") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "小票/面单商品明细 (${receipt.merchantName})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (receipt.totalAmount != null) {
                    Text(
                        text = "小票总额: ¥${receipt.totalAmount} · 日期: ${receipt.purchaseDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 选择存放空间
                Text("统一存放空间：", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    spaces.take(8).forEach { space ->
                        FilterChip(
                            selected = targetSpace == space,
                            onClick = { targetSpace = space },
                            label = { Text(space) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(itemsState) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected,
                                onCheckedChange = { checked ->
                                    val idx = itemsState.indexOf(item)
                                    if (idx >= 0) {
                                        itemsState[idx] = item.copy(isSelected = checked)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${item.category} · 存放: $targetSpace · x${item.quantity}${item.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.price != null) {
                                Text("¥${item.price}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(itemsState.filter { it.isSelected }, targetSpace)
                        }
                    ) {
                        Text("存入本地物品库")
                    }
                }
            }
        }
    }
}
