package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ai.ApiConfig
import com.example.ai.ProviderType
import com.example.ui.components.NetworkStatusBar
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.util.SecuritySettings
import com.example.util.WebDavConfig
import com.example.viewmodel.StorageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiConfig by viewModel.apiConfig.collectAsState()
    val webDavConfig by viewModel.webDavConfig.collectAsState()
    val securitySettings by viewModel.securitySettings.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val allContainers by viewModel.allContainers.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    // Local form state for API
    var selectedProvider by remember(apiConfig) { mutableStateOf(apiConfig.provider) }
    var apiKeyInput by remember(apiConfig) { mutableStateOf(apiConfig.apiKey) }
    var customBaseUrl by remember(apiConfig) { mutableStateOf(apiConfig.customBaseUrl) }
    var customModel by remember(apiConfig) { mutableStateOf(apiConfig.customModel) }
    var geminiModel by remember(apiConfig) { mutableStateOf(apiConfig.geminiModel) }
    var useLocalOffline by remember(apiConfig) { mutableStateOf(apiConfig.useLocalOfflineEngine) }
    var autoFallbackLocal by remember(apiConfig) { mutableStateOf(apiConfig.autoFallbackToLocal) }
    var imageMaxDim by remember(apiConfig) { mutableStateOf(apiConfig.imageMaxDimension) }
    var imageQuality by remember(apiConfig) { mutableStateOf(apiConfig.imageQuality) }
    var openAiLowDetail by remember(apiConfig) { mutableStateOf(apiConfig.openAiLowDetail) }
    var fastThinkingMode by remember(apiConfig) { mutableStateOf(apiConfig.fastThinkingMode) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(true) }

    // WebDav form state
    var webDavUrl by remember(webDavConfig) { mutableStateOf(webDavConfig.serverUrl) }
    var webDavUser by remember(webDavConfig) { mutableStateOf(webDavConfig.username) }
    var webDavPass by remember(webDavConfig) { mutableStateOf(webDavConfig.password) }

    // Security form state
    var isLockEnabled by remember(securitySettings) { mutableStateOf(securitySettings.isLockEnabled) }
    var pinCodeInput by remember(securitySettings) { mutableStateOf(securitySettings.pinCode) }

    // Backup view dialog
    var backupJsonDialogContent by remember { mutableStateOf<String?>(null) }
    var restoreJsonInput by remember { mutableStateOf<String?>(null) }

    val totalAssetValue = remember(allItems) {
        allItems.mapNotNull { it.price }.sum()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NetworkStatusBar(isOnline = isOnline)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "系统与数据管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "纯本地存储安全保障、万能 AI 视觉接口及私有 WebDAV 同步",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Asset & Local Storage Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "本地资产库统计",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "在库物品", value = "${allItems.size} 件")
                        StatItem(label = "收纳空间", value = "${allContainers.size} 个")
                        StatItem(label = "资产总额", value = "¥${String.format(Locale.getDefault(), "%.1f", totalAssetValue)}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 万能 API 配置中心
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "万能 API 配置中心 (AI视觉引擎)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支持 Google Gemini 原生官方接口或自建/第三方 OpenAI 兼容视觉格式 (如通义千问 Qwen-VL、智谱 GLM-4V、GPT-4o 等)。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Local Offline Engine Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (useLocalOffline) AlertGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "本地离线智能引擎 (免 API Key)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (useLocalOffline) AlertGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (useLocalOffline) "已开启：纯本地规则与启发式推断，断网即时响应，零隐私泄露风险。" else "关闭后将优先使用下方配置的云端视觉大模型 (需有效 API Key 及网络)。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = useLocalOffline,
                                onCheckedChange = {
                                     useLocalOffline = it
                                    val updatedConfig = ApiConfig(
                                        provider = selectedProvider,
                                        apiKey = apiKeyInput.trim(),
                                        customBaseUrl = customBaseUrl.trim(),
                                        customModel = customModel.trim(),
                                        geminiModel = geminiModel.trim(),
                                        useLocalOfflineEngine = it,
                                        autoFallbackToLocal = autoFallbackLocal
                                    )
                                    viewModel.updateApiConfig(updatedConfig)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto Fallback to Local Engine Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "云端异常时自动切换本地引擎",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (autoFallbackLocal) "已启用：当云端接口超时(10s)、断网或报错时，自动无缝降级为本地离线智能引擎，保证识别永不中断。" else "已停用：云端失败时将显示错误提示。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = autoFallbackLocal,
                                onCheckedChange = {
                                    autoFallbackLocal = it
                                    val updatedConfig = ApiConfig(
                                        provider = selectedProvider,
                                        apiKey = apiKeyInput.trim(),
                                        customBaseUrl = customBaseUrl.trim(),
                                        customModel = customModel.trim(),
                                        geminiModel = geminiModel.trim(),
                                        useLocalOfflineEngine = useLocalOffline,
                                        autoFallbackToLocal = it
                                    )
                                    viewModel.updateApiConfig(updatedConfig)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Provider Toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ElevatedFilterChip(
                            selected = selectedProvider == ProviderType.GEMINI,
                            onClick = { selectedProvider = ProviderType.GEMINI },
                            label = { Text("Google Gemini 官方") }
                        )
                        ElevatedFilterChip(
                            selected = selectedProvider == ProviderType.OPENAI_COMPATIBLE,
                            onClick = { selectedProvider = ProviderType.OPENAI_COMPATIBLE },
                            label = { Text("OpenAI 兼容接口") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedProvider == ProviderType.GEMINI) {
                        OutlinedTextField(
                            value = geminiModel,
                            onValueChange = { geminiModel = it },
                            label = { Text("Gemini 视觉模型 (默认: gemini-3.5-flash)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        // Warning if user pasted share.gemini.google link
                        val isWrongGeminiShareUrl = customBaseUrl.contains("share.gemini.google") || customBaseUrl.contains("gemini.google.com")
                        if (isWrongGeminiShareUrl) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AlertRed.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "⚠️ 检测到「$customBaseUrl」是 Gemini 网页分享链接，不是标准 API 端点！会导致连接超时。若需使用 Gemini，请在上方选择「Google Gemini 官方」；若使用通义千问等，请点击下方快捷预设。",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlertRed
                                    )
                                }
                            }
                        }

                        // Preset chips for OpenAI compatible mode
                        Text(
                            text = "常用兼容服务商快捷填入：",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
                                    customModel = "qwen-vl-plus"
                                    imageMaxDim = 512
                                    imageQuality = 70
                                    openAiLowDetail = true
                                },
                                label = { Text("⚡ 通义千问 Plus (极速推荐)") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
                                    customModel = "qwen2.5-vl-7b-instruct"
                                    imageMaxDim = 512
                                    imageQuality = 65
                                    openAiLowDetail = true
                                },
                                label = { Text("🚀 千问 2.5-7B (毫秒小钢炮)") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
                                    customModel = "qwen-vl-max"
                                },
                                label = { Text("千问 Max (高精/较慢)") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://open.bigmodel.cn/api/paas/v4"
                                    customModel = "glm-4v"
                                },
                                label = { Text("智谱清言 (GLM-4V)") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://api.siliconflow.cn/v1"
                                    customModel = "Qwen/Qwen2-VL-72B-Instruct"
                                },
                                label = { Text("硅基流动 (SiliconFlow)") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://api.openai.com/v1"
                                    customModel = "gpt-4o"
                                },
                                label = { Text("官方 OpenAI (GPT-4o)") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 千问 1 秒极速响应智能指导卡片
                        val isQwenOrDashScope = customModel.lowercase().contains("qwen") || customBaseUrl.contains("dashscope")
                        if (isQwenOrDashScope) {
                            val isSlowMax = customModel.lowercase().contains("qwen-vl-max")
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSlowMax) AlertRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = if (isSlowMax) AlertRed else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSlowMax) "⚡ 千问延迟过高/失败原因与 1 秒提速方案" else "⚡ 通义千问 1 秒极速响应调优指南",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSlowMax) AlertRed else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isSlowMax) {
                                            "当前选中的 qwen-vl-max 为 72B+ 巨型模型，云端推理与排队常常耗时 10~25 秒，极易触发超时导致识别失败。推荐点击下方按钮一键切换为 qwen-vl-plus 或 7B 模型，耗时将从 15 秒立降至 800ms~1 秒！"
                                        } else {
                                            "千问模型已处于推荐的轻量极速状态。结合已生效的 512px 压缩与 Token 约束 (min_pixels=3136)，端到端识别通常在 600ms~1.2 秒内极速完成。"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isSlowMax) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    customModel = "qwen-vl-plus"
                                                    imageMaxDim = 512
                                                    imageQuality = 70
                                                    openAiLowDetail = true
                                                }
                                            ) {
                                                Text("一键优化为 qwen-vl-plus (极速)")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    customModel = "qwen2.5-vl-7b-instruct"
                                                    imageMaxDim = 512
                                                    imageQuality = 65
                                                    openAiLowDetail = true
                                                }
                                            ) {
                                                Text("切换 7B (毫秒级)")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customBaseUrl,
                            onValueChange = { customBaseUrl = it },
                            label = { Text("API Base URL (如: https://dashscope.aliyuncs.com/compatible-mode/v1)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customModel,
                            onValueChange = { customModel = it },
                            label = { Text("视觉模型代号 (如: qwen-vl-max, glm-4v, gpt-4o)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ⚡ 传输效率与 Token 压缩优化模块
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "传输效率与 Token 极致提速优化",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "降低照片上传体积与冗余 Token，提升首包响应速度 3~5 倍并显著降低费用。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 图片长边压缩规格
                            Text(
                                text = "照片传输最大边长 (当前: ${imageMaxDim}px):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = imageMaxDim == 640,
                                    onClick = { imageMaxDim = 640 },
                                    label = { Text("640px 极速") }
                                )
                                FilterChip(
                                    selected = imageMaxDim == 1024,
                                    onClick = { imageMaxDim = 1024 },
                                    label = { Text("1024px 推荐高清") }
                                )
                                FilterChip(
                                    selected = imageMaxDim == 1280,
                                    onClick = { imageMaxDim = 1280 },
                                    label = { Text("1280px 超清全景") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 图片编码质量
                            Text(
                                text = "JPEG 压缩质量 (当前: ${imageQuality}%):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = imageQuality == 70,
                                    onClick = { imageQuality = 70 },
                                    label = { Text("70% 紧凑") }
                                )
                                FilterChip(
                                    selected = imageQuality == 85,
                                    onClick = { imageQuality = 85 },
                                    label = { Text("85% 推荐质量") }
                                )
                                FilterChip(
                                    selected = imageQuality == 90,
                                    onClick = { imageQuality = 90 },
                                    label = { Text("90% 超清无损感") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 极速模式开关
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "OpenAI 视觉极速模式 (detail=low)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "开启后每张图片仅消耗固定 85 Token，极大缩减排队耗时与 API 消耗",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = openAiLowDetail,
                                    onCheckedChange = { openAiLowDetail = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Gemini 极速直出 (跳过漫长思考)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "跳过数千 Token 的链式思考过程，让模型秒级直接输出 JSON，彻底杜绝超时",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = fastThinkingMode,
                                    onCheckedChange = { fastThinkingMode = it }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Anti-Mistake Filter notice (防呆拦截机制)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AlertGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "防呆拦截已开启：自动检测并拦截 DALL-E、语音、嵌入等非视觉大模型，防止误配置造成 API 失败。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (testResultText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTestSuccess) AlertGreen.copy(alpha = 0.12f) else AlertRed.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResultText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isTestSuccess) AlertGreen else AlertRed,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val currentConfig = ApiConfig(
                                    provider = selectedProvider,
                                    apiKey = apiKeyInput.trim(),
                                    customBaseUrl = customBaseUrl.trim(),
                                    customModel = customModel.trim(),
                                    geminiModel = geminiModel.trim(),
                                    useLocalOfflineEngine = useLocalOffline,
                                    autoFallbackToLocal = autoFallbackLocal,
                                    imageMaxDimension = imageMaxDim,
                                    imageQuality = imageQuality,
                                    openAiLowDetail = openAiLowDetail,
                                    fastThinkingMode = fastThinkingMode
                                )
                                viewModel.testAiApiConnection(currentConfig) { success, msg ->
                                    isTestSuccess = success
                                    testResultText = msg
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("测试连通性")
                        }

                        Button(
                            onClick = {
                                val newConfig = ApiConfig(
                                    provider = selectedProvider,
                                    apiKey = apiKeyInput.trim(),
                                    customBaseUrl = customBaseUrl.trim(),
                                    customModel = customModel.trim(),
                                    geminiModel = geminiModel.trim(),
                                    useLocalOfflineEngine = useLocalOffline,
                                    autoFallbackToLocal = autoFallbackLocal,
                                    imageMaxDimension = imageMaxDim,
                                    imageQuality = imageQuality,
                                    openAiLowDetail = openAiLowDetail,
                                    fastThinkingMode = fastThinkingMode
                                )
                                viewModel.updateApiConfig(newConfig)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存配置")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Local-First 纯本地备份与无损恢复
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "纯本地数据备份与恢复 (Local-First)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "数据存储于设备私有沙盒中。一键打包所有物品清单、分类层级及图片 Base64 编码为标准化 .json 文件，瞬间无损恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackupJson { json ->
                                    backupJsonDialogContent = json
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("生成本地备份")
                        }

                        OutlinedButton(
                            onClick = { restoreJsonInput = "" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("上传/导入恢复")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. WebDAV 私有网盘同步
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WebDAV 私有云同步 (群晖 NAS / 坚果云)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "告别第三方商业云垄断，支持自主连接 NAS 或支持 WebDAV 的个人网盘，端到端加密同步。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = webDavUrl,
                        onValueChange = { webDavUrl = it },
                        label = { Text("WebDAV 服务器 URL (如: https://dav.jianguoyun.com/dav/)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = webDavUser,
                            onValueChange = { webDavUser = it },
                            label = { Text("账号 / 邮箱") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = webDavPass,
                            onValueChange = { webDavPass = it },
                            label = { Text("密码 / 应用授权码") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (webDavConfig.lastSyncTime > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(webDavConfig.lastSyncTime))
                        Text("上次云端同步时间: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cfg = webDavConfig.copy(serverUrl = webDavUrl.trim(), username = webDavUser.trim(), password = webDavPass.trim())
                                viewModel.updateWebDavConfig(cfg)
                                viewModel.testWebDavConnection { _, _ -> }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("测试连接")
                        }

                        Button(
                            onClick = {
                                val cfg = webDavConfig.copy(serverUrl = webDavUrl.trim(), username = webDavUser.trim(), password = webDavPass.trim())
                                viewModel.updateWebDavConfig(cfg)
                                viewModel.uploadToWebDav()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("备份上传")
                        }

                        OutlinedButton(
                            onClick = {
                                val cfg = webDavConfig.copy(serverUrl = webDavUrl.trim(), username = webDavUser.trim(), password = webDavPass.trim())
                                viewModel.updateWebDavConfig(cfg)
                                viewModel.downloadFromWebDav()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("拉取恢复")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. 极致隐私与本地安全锁
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "应用隐私锁 (PIN 密码)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { isLockEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "开启后，解锁后退出软件（彻底关闭）后才重新锁定生效，使用中切后台不会频繁中断。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isLockEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pinCodeInput,
                            onValueChange = { if (it.length <= 6) pinCodeInput = it },
                            label = { Text("设置 4~6 位 PIN 码") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateSecuritySettings(
                                    SecuritySettings(
                                        isLockEnabled = isLockEnabled,
                                        pinCode = pinCodeInput.trim()
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存隐私锁设置")
                        }

                        if (isLockEnabled && pinCodeInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.lockApp()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("立即锁定")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Exported Backup JSON Viewer Dialog
    backupJsonDialogContent?.let { json ->
        Dialog(onDismissRequest = { backupJsonDialogContent = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("本地全量备份已生成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "备份大小: ${json.length / 1024} KB。包含物品数据、空间结构及图片编码。您可复制并保存在安全位置：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { backupJsonDialogContent = null }) {
                            Text("关闭")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("万物收纳Pro备份", json))
                                viewModel.showMessage("已复制备份 JSON 到剪贴板")
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("复制备份内容")
                        }
                    }
                }
            }
        }
    }

    // Restore Backup JSON Input Dialog
    restoreJsonInput?.let { _ ->
        var pastedText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { restoreJsonInput = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("导入与无损恢复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "粘贴备份的 .json 格式内容，应用将瞬间覆盖恢复全部物品和空间分类：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        placeholder = { Text("在此粘贴备份 JSON 内容...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { restoreJsonInput = null }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (pastedText.isNotBlank()) {
                                    viewModel.restoreBackupJson(pastedText.trim())
                                    restoreJsonInput = null
                                }
                            }
                        ) {
                            Text("立即执行恢复")
                        }
                    }
                }
            }
        }
    }

    // Active Restore Progress Dialog
    if (restoreProgress.isRestoring) {
        Dialog(onDismissRequest = { /* non-dismissible during active write */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(42.dp),
                        strokeWidth = 3.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在安全批量恢复数据",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = restoreProgress.stage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (restoreProgress.total > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (restoreProgress.total > 0) {
                                    (restoreProgress.current.toFloat() / restoreProgress.total.toFloat()).coerceIn(0f, 1f)
                                } else 0f
                            },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${restoreProgress.current} / ${restoreProgress.total}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
