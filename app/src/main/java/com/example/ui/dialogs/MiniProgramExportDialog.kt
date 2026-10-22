package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class MiniProgramFileItem(
    val path: String,
    val description: String,
    val codeContent: String
)

@Composable
fun MiniProgramExportDialog(
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFileIndex by remember { mutableStateOf(0) }

    val fileList = remember {
        listOf(
            MiniProgramFileItem(
                path = "app.json",
                description = "小程序全局路由、页面配置与顶部/底栏 TabBar 导航",
                codeContent = """{
  "pages": [
    "pages/index/index",
    "pages/ai-scan/ai-scan",
    "pages/containers/containers",
    "pages/item-edit/item-edit",
    "pages/item-detail/item-detail",
    "pages/settings/settings"
  ],
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#07C160",
    "navigationBarTitleText": "AI 智存空间",
    "navigationBarTextStyle": "white",
    "backgroundColor": "#F7F8FA"
  },
  "tabBar": {
    "color": "#8A8A8A",
    "selectedColor": "#07C160",
    "backgroundColor": "#FFFFFF",
    "borderStyle": "black",
    "list": [
      { "pagePath": "pages/index/index", "text": "物品清单" },
      { "pagePath": "pages/ai-scan/ai-scan", "text": "AI识图" },
      { "pagePath": "pages/containers/containers", "text": "空间收纳" },
      { "pagePath": "pages/settings/settings", "text": "设置与同步" }
    ]
  },
  "style": "v2",
  "sitemapLocation": "sitemap.json",
  "permission": {
    "scope.camera": { "desc": "用于拍照识别物品与扫描条码二维码" }
  }
}"""
            ),
            MiniProgramFileItem(
                path = "utils/aiService.js",
                description = "多模态 AI 识图服务：通义千问/混元/智谱/DeepSeek + 离线知识库",
                codeContent = """// utils/aiService.js - 微信小程序端多模态 AI 识别服务
const storage = require('./storage.js');
const localHeuristic = require('./localHeuristic.js');

function analyzeImageOrText(options) {
  const { imageBase64, userPrompt, onProgress } = options;
  const config = storage.getApiConfig();

  return new Promise((resolve) => {
    if (!config.apiKey || config.provider === 'OFFLINE_HEURISTIC') {
      if (onProgress) onProgress('正在启动小程序离线智能知识库...');
      setTimeout(() => {
        const result = localHeuristic.recognizeLocally(userPrompt || '智能识别物品');
        resolve({
          source: 'OFFLINE_HEURISTIC',
          items: [result],
          rawSummary: '已通过小程序离线知识库完成特征匹配与分类推荐'
        });
      }, 500);
      return;
    }

    if (onProgress) onProgress(`正在连接云端大模型 (${'$'}{config.provider})...`);

    const promptText = `你是一个专业的物品收纳与元器件资产管理专家。请分析传入的图片或描述，输出标准 JSON 数组：
[
  {
    "name": "物品精确名称",
    "categoryName": "电子数码/电子元器件/工具五金/药品个护/食品饮料/服饰箱包/美妆护肤/日常居家",
    "suggestedLocation": "推荐收纳位置",
    "quantity": 1,
    "unit": "个/包/盒/台/支/块/片",
    "spec": "型号规格(如 10k 0805, 100uF 25V)",
    "brand": "品牌名称",
    "estimatedPrice": 0.0,
    "tags": ["标签1", "标签2"]
  }
]`;

    let endpoint = 'https://api.deepseek.com/chat/completions';
    if (config.provider === 'ALIYUN_BAILIAN') endpoint = 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions';
    else if (config.provider === 'TENCENT_HUNYUAN') endpoint = 'https://api.hunyuan.cloud.tencent.com/v1/chat/completions';
    else if (config.provider === 'ZHIPU_GLM') endpoint = 'https://open.bigmodel.cn/api/paas/v4/chat/completions';

    wx.request({
      url: endpoint,
      method: 'POST',
      header: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${'$'}{config.apiKey}` },
      data: {
        model: config.model || 'gpt-4o-mini',
        messages: [{ role: 'user', content: promptText + '\n' + (userPrompt || '') }]
      },
      success(res) {
        try {
          const content = res.data.choices[0].message.content;
          const jsonMatch = content.match(/\[[\s\S]*\]/);
          if (jsonMatch) {
            resolve({ source: config.provider, items: JSON.parse(jsonMatch[0]) });
            return;
          }
        } catch(e) {}
        resolve({ source: 'FALLBACK', items: [localHeuristic.recognizeLocally(userPrompt || '物品')] });
      },
      fail() {
        resolve({ source: 'OFFLINE_FALLBACK', items: [localHeuristic.recognizeLocally(userPrompt || '物品')] });
      }
    });
  });
}

module.exports = { analyzeImageOrText };"""
            ),
            MiniProgramFileItem(
                path = "utils/storage.js",
                description = "小程序端 Storage 本地数据库层，支持物品/空间/分类 CRUD 与全量导入导出",
                codeContent = """// utils/storage.js - 微信小程序端持久化存储
const STORAGE_KEYS = {
  ITEMS: 'AI_STORAGE_ITEMS',
  CONTAINERS: 'AI_STORAGE_CONTAINERS',
  CATEGORIES: 'AI_STORAGE_CATEGORIES',
  API_CONFIG: 'AI_STORAGE_API_CONFIG'
};

function getItems() { return wx.getStorageSync(STORAGE_KEYS.ITEMS) || []; }
function saveItem(item) {
  const items = getItems();
  if (!item.id) {
    item.id = 'item_' + Date.now();
    item.createdAt = Date.now();
    items.unshift(item);
  } else {
    const idx = items.findIndex(i => i.id === item.id);
    if (idx >= 0) items[idx] = item;
    else items.unshift(item);
  }
  wx.setStorageSync(STORAGE_KEYS.ITEMS, items);
  return item;
}
function deleteItem(id) {
  const items = getItems().filter(i => i.id !== id);
  wx.setStorageSync(STORAGE_KEYS.ITEMS, items);
}

module.exports = { getItems, saveItem, deleteItem, /* ...更多方法... */ };"""
            ),
            MiniProgramFileItem(
                path = "pages/ai-scan/ai-scan.js",
                description = "AI 识图页面逻辑：调用原生 wx.chooseMedia 拍照与 Base64 编码入库",
                codeContent = """// pages/ai-scan/ai-scan.js
const aiService = require('../../utils/aiService.js');
const storage = require('../../utils/storage.js');

Page({
  data: {
    imagePath: '',
    imageBase64: '',
    userPrompt: '',
    isAnalyzing: false,
    recognizedItems: []
  },
  onChooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      camera: 'back',
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.setData({ imagePath: tempFilePath });
        wx.getFileSystemManager().readFile({
          filePath: tempFilePath,
          encoding: 'base64',
          success: (bRes) => { this.setData({ imageBase64: bRes.data }); }
        });
      }
    });
  },
  onStartAnalyze() {
    this.setData({ isAnalyzing: true });
    aiService.analyzeImageOrText({
      imageBase64: this.data.imageBase64,
      userPrompt: this.data.userPrompt
    }).then(res => {
      this.setData({ isAnalyzing: false, recognizedItems: res.items || [] });
      wx.showToast({ title: `识别到 ${'$'}{res.items.length} 件物品`, icon: 'success' });
    });
  }
});"""
            )
        )
    }

    fun copyToClipboard(text: String, label: String) {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
      onShowToast("已复制 $label 源码到剪贴板！")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF07C160))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🟢",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "微信小程序版本源码与发布中心",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "零门槛导入微信开发者工具，即刻上架真机体验",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White
                        )
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("极速部署指南")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("小程序核心源码")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("全端架构对照")
                            }
                        }
                    )
                }

                // Body content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Step-by-step deployment guide
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FFED)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB7EB8F)))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "✨ 为什么本项目可以直接作为微信小程序运行？",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF237804),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "本工程已在 assets/miniprogram 目录下预置了 100% 纯原生微信小程序全套工程源码，包含所有 6 个核心页面、多模态 AI 识图、条码/二维码原生扫码、空间拓扑树以及本地离线知识库，无需后端即可独立运行！",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF389E0D),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "🛠️ 三步发布到微信小程序：",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                DeploymentStepItem(
                                    step = "1",
                                    title = "下载并打开微信开发者工具",
                                    desc = "前往微信官方开放平台 (mp.weixin.qq.com) 下载「微信开发者工具 (Stable版)」。"
                                )

                                DeploymentStepItem(
                                    step = "2",
                                    title = "导入工程目录",
                                    desc = "在开发者工具中点击「导入项目」，选择本项目的 assets/miniprogram 目录，填入您的微信小程序 AppID（或选择测试号）。"
                                )

                                DeploymentStepItem(
                                    step = "3",
                                    title = "真机预览与发布",
                                    desc = "点击开发者工具顶部的「真机预览」扫描二维码，即可在您的手机微信上体验完整拍照识图、物品入库与空间管理，点击「上传」即可提交微信审核上线！"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        copyToClipboard(
                                            """1. 打开微信开发者工具
2. 选择「导入项目」
3. 目录选择项目的 assets/miniprogram
4. AppID 填入测试号或个人小程序 AppID
5. 点击编译即可即时运行""",
                                            "微信小程序部署流程"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160))
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("一键复制三步导入操作备忘录")
                                }
                            }
                        }

                        1 -> {
                            // Source code viewer
                            val currentFile = fileList[selectedFileIndex]
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Horizontal file selector
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fileList.forEachIndexed { index, file ->
                                        val isSelected = index == selectedFileIndex
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFF07C160) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable { selectedFileIndex = index }
                                        ) {
                                            Text(
                                                text = file.path,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentFile.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { copyToClipboard(currentFile.codeContent, currentFile.path) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160))
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("复制代码", style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Code Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1E1E),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .horizontalScroll(rememberScrollState())
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = currentFile.codeContent,
                                            color = Color(0xFFD4D4D4),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Architecture mapping
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📱 Android 原生与微信小程序核心技术栈对照表",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                ArchMappingRow("本地持久化数据库", "Room SQLite / Flow", "wx.setStorageSync / 微信云开发 DB")
                                ArchMappingRow("拍照与相册选择", "CameraX / ActivityResultContracts", "wx.chooseMedia / wx.createCameraContext")
                                ArchMappingRow("条码与二维码扫描", "ML Kit Barcode Scanning", "wx.scanCode 原生扫码组件")
                                ArchMappingRow("AI 多模态大模型", "OkHttp + 协程挂起", "wx.request HTTPS 或 微信云函数")
                                ArchMappingRow("离线启发式规则引擎", "LocalHeuristicEngine.kt", "localHeuristic.js 纯 JS 离线词库")
                                ArchMappingRow("剪贴板备份与分享", "ClipboardManager", "wx.setClipboardData / wx.getClipboardData")
                                ArchMappingRow("多主题动态配色", "AppThemePreset (Material 3)", "app.wxss 全局 CSS 变量与主题类")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeploymentStepItem(
    step: String,
    title: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF07C160),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ArchMappingRow(
    feature: String,
    androidTech: String,
    miniProgramTech: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🔹 $feature",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Android 原生端:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(androidTech, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("微信小程序端:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF07C160))
                    Text(miniProgramTech, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF07C160))
                }
            }
        }
    }
}
