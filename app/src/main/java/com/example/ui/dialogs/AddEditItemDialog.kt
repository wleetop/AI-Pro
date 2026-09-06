package com.example.ui.dialogs

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.util.ImageCompressor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.StorageCategory
import com.example.data.StorageContainer
import com.example.data.StorageItem
import com.example.viewmodel.AiOperationState
import com.example.viewmodel.StorageViewModel
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditItemDialog(
    initialItem: StorageItem?,
    categories: List<StorageCategory>,
    containers: List<StorageContainer>,
    viewModel: StorageViewModel,
    prefilledImagePath: String? = null,
    onDismiss: () -> Unit,
    onSaved: (StorageItem) -> Unit
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: categories.firstOrNull()?.name ?: "日常居家") }
    var locationPath by remember { mutableStateOf(initialItem?.locationPath ?: "客厅/收纳柜") }
    var selectedContainerId by remember { mutableStateOf(initialItem?.containerId) }
    var quantity by remember { mutableIntStateOf(initialItem?.quantity ?: 1) }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "件") }
    var priceText by remember { mutableStateOf(initialItem?.price?.toString() ?: "") }
    var barcode by remember { mutableStateOf(initialItem?.barcode ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var tags by remember { mutableStateOf(initialItem?.tags ?: "") }
    var imagePath by remember { mutableStateOf(initialItem?.imageUri ?: prefilledImagePath) }
    var expiryDaysText by remember {
        val days = initialItem?.expiryDate?.let {
            val d = ((it - System.currentTimeMillis()) / (24 * 3600 * 1000)).toInt()
            if (d > 0) d.toString() else "0"
        } ?: ""
        mutableStateOf(days)
    }

    var isCompressing by remember { mutableStateOf(false) }

    // Media Picker for image with automatic Canvas/Bitmap scale & JPEG compression
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isCompressing = true
            viewModel.compressAndSaveImageUri(uri) { savedPath ->
                imagePath = savedPath
                isCompressing = false
            }
        }
    }

    // Barcode scanner picker
    val barcodeImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val bmp = ImageCompressor.decodeUriToBitmap(context, uri)
                    if (bmp != null) {
                        viewModel.compressAndSaveBitmap(bmp) { _ -> }
                        val code = com.example.util.QrCodeUtils.decodeBarcodeFromBitmap(bmp)
                        if (!code.isNullOrBlank()) {
                            barcode = code
                            viewModel.showMessage("扫码解析出条码: $code")
                        } else {
                            viewModel.showMessage("未能在图片中识别到清晰条码/二维码")
                        }
                    } else {
                        viewModel.showMessage("条码图片读取失败")
                    }
                } catch (e: Exception) {
                    viewModel.showMessage("条码解析失败")
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
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem == null) "录入新物品" else "编辑物品详情",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image selector box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompressing) {
                            CircularProgressIndicator()
                        } else if (!imagePath.isNullOrBlank() && File(imagePath!!).exists()) {
                            AsyncImage(
                                model = File(imagePath!!),
                                contentDescription = "物品照片",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "选择照片",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "拍照或选取照片 (自动压缩质量 & 纯本地存储)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name field with AI suggestion button
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("物品名称 *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_name_input"),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        viewModel.suggestItemFromName(name)
                                    }
                                },
                                modifier = Modifier.testTag("ai_suggest_name_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI 联想建议",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector (Chips)
                    Text("物品分类", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat.name,
                                onClick = { selectedCategory = cat.name },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Path & Container binding
                    OutlinedTextField(
                        value = locationPath,
                        onValueChange = { locationPath = it },
                        label = { Text("存放空间层级 (如: 主卧/衣柜/顶层)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("空间快捷选择：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("客厅", "主卧", "次卧", "书房", "厨房", "储物间", "阳台", "玄关", "卫生间", "衣帽间", "儿童房").forEach { space ->
                            AssistChip(
                                onClick = {
                                    locationPath = if (locationPath.contains("/")) {
                                        val sub = locationPath.substringAfter("/")
                                        "$space/$sub"
                                    } else {
                                        space
                                    }
                                },
                                label = { Text(space) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Associated Storage Container
                    if (containers.isNotEmpty()) {
                        Text("关联虚拟收纳箱 (可选)", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedContainerId == null,
                                onClick = { selectedContainerId = null },
                                label = { Text("无收纳箱") }
                            )
                            containers.forEach { box ->
                                FilterChip(
                                    selected = selectedContainerId == box.id,
                                    onClick = {
                                        selectedContainerId = box.id
                                        locationPath = box.locationPath
                                    },
                                    label = { Text(box.name) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Quantity and Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                            label = { Text("数量") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("量词单位") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price & Expiry Days Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("购买金额 (¥)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = expiryDaysText,
                            onValueChange = { expiryDaysText = it },
                            label = { Text("保质天数 (留空为永不过期)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barcode field with Scan Button
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("条形码/商品编码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    barcodeImagePicker.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "扫描条码",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags & Notes
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("自定义标签 (逗号隔开，如: 露营, 贵重)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("备注信息 / 保养说明") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                viewModel.showMessage("物品名称不能为空")
                                return@Button
                            }

                            val days = expiryDaysText.toIntOrNull()
                            val computedExpiry = if (days != null && days > 0) {
                                System.currentTimeMillis() + days.toLong() * 24 * 3600 * 1000
                            } else null

                            val savedItem = (initialItem ?: StorageItem(name = name)).copy(
                                name = name.trim(),
                                category = selectedCategory,
                                locationPath = locationPath.trim(),
                                containerId = selectedContainerId,
                                quantity = if (quantity > 0) quantity else 1,
                                unit = unit.ifBlank { "件" },
                                price = priceText.toDoubleOrNull(),
                                expiryDate = computedExpiry,
                                imageUri = imagePath,
                                barcode = barcode.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() },
                                tags = tags.trim()
                            )
                            onSaved(savedItem)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_item_button")
                    ) {
                        Text("保存存库")
                    }
                }
            }
        }
    }
}
