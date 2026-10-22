package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.ApiConfig
import com.example.ai.ProviderType
import com.example.ui.components.NetworkStatusBar
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AppThemePreset
import com.example.util.SecuritySettings
import com.example.util.WebDavConfig
import com.example.util.WebDavManager
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
    val currentThemePreset by viewModel.currentThemePreset.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val allContainers by viewModel.allContainers.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    // Local form state for API
    var selectedProvider by remember(apiConfig) { mutableStateOf(apiConfig.provider) }
    var apiKeyInput by remember(apiConfig) { mutableStateOf(apiConfig.apiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var hunyuanBaseUrl by remember(apiConfig) { mutableStateOf(apiConfig.hunyuanBaseUrl) }
    var hunyuanModel by remember(apiConfig) { mutableStateOf(apiConfig.hunyuanModel) }
    var customBaseUrl by remember(apiConfig) { mutableStateOf(apiConfig.customBaseUrl) }
    var customModel by remember(apiConfig) { mutableStateOf(apiConfig.customModel) }
    var geminiModel by remember(apiConfig) { mutableStateOf(apiConfig.geminiModel) }
    var useLocalOffline by remember(apiConfig) { mutableStateOf(apiConfig.useLocalOfflineEngine) }
    var autoFallbackLocal by remember(apiConfig) { mutableStateOf(apiConfig.autoFallbackToLocal) }
    var prioritizeLocalMatching by remember(apiConfig) { mutableStateOf(apiConfig.prioritizeLocalMatching) }
    var imageMaxDim by remember(apiConfig) { mutableStateOf(apiConfig.imageMaxDimension) }
    var imageQuality by remember(apiConfig) { mutableStateOf(apiConfig.imageQuality) }
    var openAiLowDetail by remember(apiConfig) { mutableStateOf(apiConfig.openAiLowDetail) }
    var fastThinkingMode by remember(apiConfig) { mutableStateOf(apiConfig.fastThinkingMode) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(true) }

    // Active tutorial tab: "hunyuan", "siliconflow", "qwen", "zhipu", "gemini"
    var activeTutorialTab by remember { mutableStateOf("hunyuan") }

    // WebDav form state
    var webDavUrl by remember(webDavConfig) { mutableStateOf(webDavConfig.serverUrl) }
    var webDavUser by remember(webDavConfig) { mutableStateOf(webDavConfig.username) }
    var webDavPass by remember(webDavConfig) { mutableStateOf(webDavConfig.password) }
    var isWebDavPassVisible by remember { mutableStateOf(false) }

    // Security form state
    var isLockEnabled by remember(securitySettings) { mutableStateOf(securitySettings.isLockEnabled) }
    var pinCodeInput by remember(securitySettings) { mutableStateOf(securitySettings.pinCode) }
    var isPinCodeVisible by remember { mutableStateOf(false) }

    // Backup view dialog
    var backupJsonDialogContent by remember { mutableStateOf<String?>(null) }
    var restoreJsonInput by remember { mutableStateOf<String?>(null) }
    var showWhitepaperDialog by remember { mutableStateOf(false) }
    var showMiniProgramDialog by remember { mutableStateOf(false) }

    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val jsonContent = pendingExportJson
            if (!jsonContent.isNullOrBlank()) {
                try {
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        output.write(jsonContent.toByteArray(Charsets.UTF_8))
                    }
                    viewModel.showMessage("已成功导出保存备份文件至本地存储！")
                } catch (e: Exception) {
                    viewModel.showMessage("保存文件失败: ${e.localizedMessage}")
                }
            } else {
                viewModel.showMessage("备份内容为空，无法保存")
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val jsonText = context.contentResolver.openInputStream(it)?.use { input ->
                    input.bufferedReader().use { reader -> reader.readText() }
                }
                if (!jsonText.isNullOrBlank()) {
                    viewModel.restoreBackupJson(jsonText)
                } else {
                    viewModel.showMessage("选中的备份文件内容为空，无法恢复")
                }
            } catch (e: Exception) {
                viewModel.showMessage("读取备份文件失败: ${e.localizedMessage}")
            }
        }
    }

    val totalAssetValue = remember(allItems) {
        allItems.mapNotNull { it.price }.sum()
    }

    // Helper for copying and opening URLs with WeChat Mini-Program adaptations
    fun copyUrl(label: String, url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, url))
        viewModel.showMessage("已复制 $label 注册链接，请在手机浏览器或微信中粘贴打开！")
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            copyUrl("官方注册", url)
        }
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

            // 1. 自定义主题配色方案 (Theme Switcher)
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "应用主题与视觉风格",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = currentThemePreset.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "支持 8 套精心配套的主题调色板，界面全局元素、按钮、卡片随选即换，完美契合微信小程序及现代质感。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid-like layout for 8 theme presets
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = AppThemePreset.values().toList()
                        presets.chunked(2).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val isSelected = preset == currentThemePreset
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setThemePreset(preset) }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Color Dot
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.primaryLight)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = preset.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "已选择",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 微信小程序源码与部署专区 (WeChat Mini Program Center)
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
                            Text(text = "🟢", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "微信小程序版本已就绪",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF07C160)
                                )
                                Text(
                                    text = "包含全部 6 个页面源码及极速发布指南",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showMiniProgramDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("查看与导出", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF6FFED),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB7EB8F))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "纯前端无缝运行：已预置在 assets/miniprogram 目录，直接在微信开发者工具中导入即可编译体验真机扫码与AI识图！",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF389E0D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Asset & Local Storage Statistics Card
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

                        OutlinedButton(
                            onClick = { showWhitepaperDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("软件使用白皮书", style = MaterialTheme.typography.labelMedium)
                        }
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

            // 3. API 服务商分步注册教程与领取指南 (含微信小程序适配)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "服务商注册与密钥领取分步教程",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "各主流服务商均提供免费额度。点击下方标签查看详细领取步骤，一键复制直达链接：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 微信小程序环境重点适配说明卡片
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💡 微信小程序适配指南：受微信小程序外链安全域名白名单规则限制，推荐点击「一键复制链接」后，在微信「文件传输助手」或手机自带浏览器中粘贴打开，即可顺利完成免费注册与密钥生成。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Provider Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeTutorialTab == "hunyuan",
                            onClick = { activeTutorialTab = "hunyuan" },
                            label = { Text("💎 腾讯混元 3 (首选)") }
                        )
                        FilterChip(
                            selected = activeTutorialTab == "siliconflow",
                            onClick = { activeTutorialTab = "siliconflow" },
                            label = { Text("⚡ 硅基流动 (免翻/送额度)") }
                        )
                        FilterChip(
                            selected = activeTutorialTab == "qwen",
                            onClick = { activeTutorialTab = "qwen" },
                            label = { Text("🌟 阿里通义千问") }
                        )
                        FilterChip(
                            selected = activeTutorialTab == "zhipu",
                            onClick = { activeTutorialTab = "zhipu" },
                            label = { Text("✨ 智谱 AI (GLM-4V)") }
                        )
                        FilterChip(
                            selected = activeTutorialTab == "gemini",
                            onClick = { activeTutorialTab = "gemini" },
                            label = { Text("🔮 Google Gemini (备选)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tutorial Content details based on selected tab
                    when (activeTutorialTab) {
                        "hunyuan" -> {
                            TutorialStepCard(
                                providerName = "腾讯云混元 3 (首选主推)",
                                officialUrl = "https://console.cloud.tencent.com/hunyuan",
                                steps = listOf(
                                    "步骤 1：点击下方按钮复制或访问腾讯云控制台，使用微信或 QQ 扫码一秒极速登录；",
                                    "步骤 2：进入「混元大模型」服务页面，开通服务（新注册账号自动赠送免费 Token 体验额度）；",
                                    "步骤 3：点击左侧「API KEY 管理」->「新建密钥」，复制生成的 SecretKey 填入本页下方密钥输入框。"
                                ),
                                onOpenUrl = { openUrl("https://console.cloud.tencent.com/hunyuan") },
                                onCopyUrl = { copyUrl("腾讯混元3", "https://console.cloud.tencent.com/hunyuan") }
                            )
                        }
                        "siliconflow" -> {
                            TutorialStepCard(
                                providerName = "硅基流动 SiliconFlow (极速免费 Qwen2.5-VL)",
                                officialUrl = "https://cloud.siliconflow.cn/",
                                steps = listOf(
                                    "步骤 1：访问硅基流动官网，手机号或微信一键注册登录；",
                                    "步骤 2：平台新用户直接赠送可用余额，支持 Qwen2.5-VL 等视觉大模型高速低延时调用；",
                                    "步骤 3：在控制台左侧点击「API 密钥」->「新建 API 密钥」，复制以 sk- 开头的密钥填入下方输入框。"
                                ),
                                onOpenUrl = { openUrl("https://cloud.siliconflow.cn/") },
                                onCopyUrl = { copyUrl("硅基流动", "https://cloud.siliconflow.cn/") }
                            )
                        }
                        "qwen" -> {
                            TutorialStepCard(
                                providerName = "阿里通义千问 (阿里云百炼 DashScope)",
                                officialUrl = "https://bailian.console.aliyun.com/",
                                steps = listOf(
                                    "步骤 1：访问阿里云百炼大模型控制台，支持支付宝、淘宝或阿里云扫码直接登录；",
                                    "步骤 2：开通百炼大模型服务（赠送百万级免费 Token 额度，支持 qwen-vl-plus 极速视觉解析）；",
                                    "步骤 3：在右上角点击「API-KEY」->「创建我的 API-KEY」，复制生成的专属密钥并填入下方。"
                                ),
                                onOpenUrl = { openUrl("https://bailian.console.aliyun.com/") },
                                onCopyUrl = { copyUrl("阿里通义千问", "https://bailian.console.aliyun.com/") }
                            )
                        }
                        "zhipu" -> {
                            TutorialStepCard(
                                providerName = "智谱 AI (BigModel 开放平台)",
                                officialUrl = "https://open.bigmodel.cn/",
                                steps = listOf(
                                    "步骤 1：访问智谱清言开放平台 (open.bigmodel.cn)，手机验证码注册；",
                                    "步骤 2：注册即领新手体验资源包，免费体验 glm-4v 视觉识别接口；",
                                    "步骤 3：在右上角个人中心或「API Keys」页面复制专属密钥，粘贴至下方输入框。"
                                ),
                                onOpenUrl = { openUrl("https://open.bigmodel.cn/") },
                                onCopyUrl = { copyUrl("智谱AI", "https://open.bigmodel.cn/") }
                            )
                        }
                        "gemini" -> {
                            TutorialStepCard(
                                providerName = "Google Gemini (Google AI Studio 备选项)",
                                officialUrl = "https://aistudio.google.com/app/apikey",
                                steps = listOf(
                                    "步骤 1：访问 Google AI Studio 官网并登录个人 Google 账号；",
                                    "步骤 2：点击左上角「Get API Key」->「Create API Key in new project」；",
                                    "步骤 3：复制生成的 API Key 填入下方输入框（注意：需在支持访问 Google 的国际网络环境中使用）。"
                                ),
                                onOpenUrl = { openUrl("https://aistudio.google.com/app/apikey") },
                                onCopyUrl = { copyUrl("Google Gemini", "https://aistudio.google.com/app/apikey") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 万能 API 配置中心
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
                        text = "支持腾讯混元 3 (首选直连)、OpenAI 兼容视觉格式 (通义千问/智谱/SiliconFlow) 及 Google Gemini 备选项。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Notice if API Key is empty
                    if (apiKeyInput.isBlank() && !useLocalOffline) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AlertAmber.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AlertAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚡ 当前未填写 API 密钥：拍照识图时将自动转为全功能「本地离线智能引擎」，零等待秒级解析，安全隐私。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

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
                                    text = if (useLocalOffline) "已开启：纯本地规则与启发式推断，断网即时响应，零隐私泄露风险。" else "关闭后将优先使用下方配置的云端视觉大模型 (需填入 API Key)。",
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
                                        hunyuanBaseUrl = hunyuanBaseUrl.trim(),
                                        hunyuanModel = hunyuanModel.trim(),
                                        customBaseUrl = customBaseUrl.trim(),
                                        customModel = customModel.trim(),
                                        geminiModel = geminiModel.trim(),
                                        useLocalOfflineEngine = it,
                                        autoFallbackToLocal = autoFallbackLocal,
                                        prioritizeLocalMatching = prioritizeLocalMatching,
                                        imageMaxDimension = imageMaxDim,
                                        imageQuality = imageQuality,
                                        openAiLowDetail = openAiLowDetail,
                                        fastThinkingMode = fastThinkingMode
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
                                    text = "云端异常或超时(5s)自动切换本地引擎",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (autoFallbackLocal) "已启用：当云端接口响应超5秒、未配密钥或网络异常时，自动无缝降级为本地离线智能引擎，保证流程永不中断。" else "已停用：云端失败时将显示错误提示。",
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
                                        hunyuanBaseUrl = hunyuanBaseUrl.trim(),
                                        hunyuanModel = hunyuanModel.trim(),
                                        customBaseUrl = customBaseUrl.trim(),
                                        customModel = customModel.trim(),
                                        geminiModel = geminiModel.trim(),
                                        useLocalOfflineEngine = useLocalOffline,
                                        autoFallbackToLocal = it,
                                        prioritizeLocalMatching = prioritizeLocalMatching,
                                        imageMaxDimension = imageMaxDim,
                                        imageQuality = imageQuality,
                                        openAiLowDetail = openAiLowDetail,
                                        fastThinkingMode = fastThinkingMode
                                    )
                                    viewModel.updateApiConfig(updatedConfig)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prioritize Local Matching Before Cloud Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (prioritizeLocalMatching) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant,
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
                                    text = "云端请求前优先本地特征匹配",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (prioritizeLocalMatching) "已启用：在发起云端 AI 联想前，优先匹配本地加权词库与历史记录。命中时秒级无网响应并节省 API 消耗。" else "已停用：输入物品名称时将无视本地词库，直接发起云端 AI 请求。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = prioritizeLocalMatching,
                                onCheckedChange = {
                                    prioritizeLocalMatching = it
                                    val updatedConfig = ApiConfig(
                                        provider = selectedProvider,
                                        apiKey = apiKeyInput.trim(),
                                        hunyuanBaseUrl = hunyuanBaseUrl.trim(),
                                        hunyuanModel = hunyuanModel.trim(),
                                        customBaseUrl = customBaseUrl.trim(),
                                        customModel = customModel.trim(),
                                        geminiModel = geminiModel.trim(),
                                        useLocalOfflineEngine = useLocalOffline,
                                        autoFallbackToLocal = autoFallbackLocal,
                                        prioritizeLocalMatching = it,
                                        imageMaxDimension = imageMaxDim,
                                        imageQuality = imageQuality,
                                        openAiLowDetail = openAiLowDetail,
                                        fastThinkingMode = fastThinkingMode
                                    )
                                    viewModel.updateApiConfig(updatedConfig)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Provider Toggle
                    Text(
                        text = "选择 AI 服务商 (混元 3 首选)：",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ElevatedFilterChip(
                            selected = selectedProvider == ProviderType.HUNYUAN_3,
                            onClick = { selectedProvider = ProviderType.HUNYUAN_3 },
                            label = { Text("💎 腾讯混元 3 (首选)") }
                        )
                        ElevatedFilterChip(
                            selected = selectedProvider == ProviderType.OPENAI_COMPATIBLE,
                            onClick = { selectedProvider = ProviderType.OPENAI_COMPATIBLE },
                            label = { Text("🌐 OpenAI 兼容接口") }
                        )
                        ElevatedFilterChip(
                            selected = selectedProvider == ProviderType.GEMINI,
                            onClick = { selectedProvider = ProviderType.GEMINI },
                            label = { Text("🔮 Google Gemini (备选)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedProvider == ProviderType.HUNYUAN_3) {
                        // Hunyuan 3 Information Guide
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "💎 腾讯混元 3 视觉大模型 (旗舰首选)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "国内原生直连免代理，具备超低延迟毫秒级视觉理解能力，针对中文物品收纳分类、多物品全家福定位与发票账单结构化全面优化。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // API Key with Masking & Placeholder
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("腾讯云混元 API 密钥 (SecretKey / Bearer Token)") },
                            placeholder = { Text("留空自动启用本地离线识图，或填入您的密钥") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isApiKeyVisible) "隐藏密钥" else "显示密钥",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick model presets for Hunyuan
                        Text(
                            text = "混元 3 推荐模型：",
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
                                    hunyuanModel = "hunyuan-vision"
                                    hunyuanBaseUrl = "https://api.hunyuan.cloud.tencent.com/v1"
                                    imageMaxDim = 640
                                    imageQuality = 75
                                },
                                label = { Text("💎 混元 3 视觉旗舰 (hunyuan-vision)") }
                            )
                            AssistChip(
                                onClick = {
                                    hunyuanModel = "hunyuan-turbo-vision"
                                    hunyuanBaseUrl = "https://api.hunyuan.cloud.tencent.com/v1"
                                    imageMaxDim = 512
                                    imageQuality = 70
                                },
                                label = { Text("⚡ 混元 3 Turbo (极速)") }
                            )
                            AssistChip(
                                onClick = {
                                    hunyuanModel = "hunyuan-3"
                                    hunyuanBaseUrl = "https://api.hunyuan.cloud.tencent.com/v1"
                                },
                                label = { Text("🚀 混元 3 (通用)") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = hunyuanModel,
                            onValueChange = { hunyuanModel = it },
                            label = { Text("混元视觉模型代号 (默认: hunyuan-vision)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = hunyuanBaseUrl,
                            onValueChange = { hunyuanBaseUrl = it },
                            label = { Text("混元 Base URL (默认: https://api.hunyuan.cloud.tencent.com/v1)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else if (selectedProvider == ProviderType.GEMINI) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🔮 Google Gemini 官方接口 (备选项)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "作为备选云端视觉引擎。需在可访问 Google 服务的网络环境下使用，未填密钥时将自动无缝切换本地离线引擎。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gemini API Key with Masking & Placeholder
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("留空自动启用本地离线识图，或填入您的 Gemini Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isApiKeyVisible) "隐藏密钥" else "显示密钥",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Gemini model presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { geminiModel = "gemini-3.5-flash" },
                                label = { Text("⚡ gemini-3.5-flash (推荐)") }
                            )
                            AssistChip(
                                onClick = { geminiModel = "gemini-2.5-flash" },
                                label = { Text("🚀 gemini-2.5-flash") }
                            )
                            AssistChip(
                                onClick = { geminiModel = "gemini-1.5-flash" },
                                label = { Text("📦 gemini-1.5-flash") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = geminiModel,
                            onValueChange = { geminiModel = it },
                            label = { Text("Gemini 视觉模型 (默认: gemini-3.5-flash)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        // OpenAI / Compatible Key with Masking & Placeholder
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("OpenAI / 通义千问 / 智谱 / SiliconFlow API Key") },
                            placeholder = { Text("留空自动启用本地离线识图，或在此粘贴 sk-xxxx") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isApiKeyVisible) "隐藏密钥" else "显示密钥",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
                                        text = "⚠️ 检测到「$customBaseUrl」是 Gemini 网页分享链接，不是标准 API 端点！会导致连接超时。若需使用 Gemini，请在上方选择「Google Gemini (备选)」；若使用腾讯混元/通义千问等，请点击下方快捷预设。",
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
                                    customBaseUrl = "https://api.hunyuan.cloud.tencent.com/v1"
                                    customModel = "hunyuan-vision"
                                    imageMaxDim = 640
                                    imageQuality = 75
                                    openAiLowDetail = false
                                },
                                label = { Text("💎 腾讯混元 3 (首选)") }
                            )
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
                                    customBaseUrl = "https://api.siliconflow.cn/v1"
                                    customModel = "Qwen/Qwen2.5-VL-7B-Instruct"
                                    imageMaxDim = 512
                                    imageQuality = 70
                                    openAiLowDetail = true
                                },
                                label = { Text("⚡ 硅基流动 Qwen2.5-VL") }
                            )
                            AssistChip(
                                onClick = {
                                    customBaseUrl = "https://open.bigmodel.cn/api/paas/v4"
                                    customModel = "glm-4v-flash"
                                    imageMaxDim = 512
                                    imageQuality = 70
                                },
                                label = { Text("智谱清言 (GLM-4V-Flash)") }
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
                            label = { Text("视觉模型代号 (如: qwen-vl-plus, glm-4v-flash, gpt-4o)") },
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
                                    hunyuanBaseUrl = hunyuanBaseUrl.trim(),
                                    hunyuanModel = hunyuanModel.trim(),
                                    customBaseUrl = customBaseUrl.trim(),
                                    customModel = customModel.trim(),
                                    geminiModel = geminiModel.trim(),
                                    useLocalOfflineEngine = useLocalOffline,
                                    autoFallbackToLocal = autoFallbackLocal,
                                    prioritizeLocalMatching = prioritizeLocalMatching,
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
                                    hunyuanBaseUrl = hunyuanBaseUrl.trim(),
                                    hunyuanModel = hunyuanModel.trim(),
                                    customBaseUrl = customBaseUrl.trim(),
                                    customModel = customModel.trim(),
                                    geminiModel = geminiModel.trim(),
                                    useLocalOfflineEngine = useLocalOffline,
                                    autoFallbackToLocal = autoFallbackLocal,
                                    prioritizeLocalMatching = prioritizeLocalMatching,
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

            // 5. Local-First 纯本地备份与无损恢复
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
                        text = "数据完全存储于设备本地私有库中。支持直接导出保存为 `.json` 备份文件到本地下载/文档目录，随时选择本地备份文件一键恢复。",
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
                                    pendingExportJson = json
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    createDocumentLauncher.launch("StoragePro_Backup_${timeStamp}.json")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("保存备份为文件")
                        }

                        OutlinedButton(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("读取文件恢复")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                viewModel.exportBackupJson { json ->
                                    backupJsonDialogContent = json
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("查看/复制 JSON 源码", style = MaterialTheme.typography.labelSmall)
                        }

                        androidx.compose.material3.TextButton(
                            onClick = { restoreJsonInput = "" }
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("粘贴 JSON 源码恢复", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. WebDAV 私有网盘同步
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

                    // 坚果云等网盘快捷预设与防坑提示
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                webDavUrl = "https://dav.jianguoyun.com/dav/StoragePro/"
                            },
                            label = { Text("🚀 快捷填入: 坚果云 (Jianguoyun)") }
                        )
                        AssistChip(
                            onClick = {
                                webDavUrl = "https://your-nas-ip:5006/webdav/"
                            },
                            label = { Text("群晖 Synology NAS") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val isJianguoyun = webDavUrl.contains("jianguoyun")
                    if (isJianguoyun) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💡 坚果云提示：请在「坚果云网页版 -> 账户信息 -> 高级设置 -> 添加应用密码」生成专用授权码填充至密码框。坚果云必须写入子文件夹，建议保持默认地址: https://dav.jianguoyun.com/dav/StoragePro/",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

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
                        // Password with Masking
                        OutlinedTextField(
                            value = webDavPass,
                            onValueChange = { webDavPass = it },
                            label = { Text("密码 / 授权码") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            visualTransformation = if (isWebDavPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isWebDavPassVisible = !isWebDavPassVisible }) {
                                    Icon(
                                        imageVector = if (isWebDavPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isWebDavPassVisible) "隐藏密码" else "显示密码",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
                                val formattedUrl = WebDavManager.formatServerUrl(webDavUrl)
                                webDavUrl = formattedUrl
                                val cfg = webDavConfig.copy(serverUrl = formattedUrl, username = webDavUser.trim(), password = webDavPass.trim())
                                viewModel.updateWebDavConfig(cfg)
                                viewModel.testWebDavConnection { _, _ -> }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("测试连接")
                        }

                        Button(
                            onClick = {
                                val formattedUrl = WebDavManager.formatServerUrl(webDavUrl)
                                webDavUrl = formattedUrl
                                val cfg = webDavConfig.copy(serverUrl = formattedUrl, username = webDavUser.trim(), password = webDavPass.trim())
                                viewModel.updateWebDavConfig(cfg)
                                viewModel.uploadToWebDav()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("备份上传")
                        }

                        OutlinedButton(
                            onClick = {
                                val formattedUrl = WebDavManager.formatServerUrl(webDavUrl)
                                webDavUrl = formattedUrl
                                val cfg = webDavConfig.copy(serverUrl = formattedUrl, username = webDavUser.trim(), password = webDavPass.trim())
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

            // 7. 极致隐私与本地安全锁
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
                            singleLine = true,
                            visualTransformation = if (isPinCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPinCodeVisible = !isPinCodeVisible }) {
                                    Icon(
                                        imageVector = if (isPinCodeVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPinCodeVisible) "隐藏PIN码" else "显示PIN码",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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

    if (showWhitepaperDialog) {
        com.example.ui.dialogs.AppWhitepaperDialog(
            onDismiss = { showWhitepaperDialog = false }
        )
    }

    if (showMiniProgramDialog) {
        com.example.ui.dialogs.MiniProgramExportDialog(
            onDismiss = { showMiniProgramDialog = false },
            onShowToast = { viewModel.showMessage(it) }
        )
    }
}

@Composable
fun TutorialStepCard(
    providerName: String,
    officialUrl: String,
    steps: List<String>,
    onOpenUrl: () -> Unit,
    onCopyUrl: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📌 $providerName 获取步骤：",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            steps.forEach { step ->
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyUrl,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制注册链接", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onOpenUrl,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("直达官网注册", style = MaterialTheme.typography.labelMedium)
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
