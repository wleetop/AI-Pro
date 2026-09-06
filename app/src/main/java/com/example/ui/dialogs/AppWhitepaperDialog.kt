package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppWhitepaperDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "软件使用白皮书",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "万物收纳Pro · 全场景极速物品管理指南",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    WhitepaperSection(
                        title = "一、 产品定位与核心设计理念",
                        icon = Icons.Default.Psychology,
                        content = "《万物收纳Pro》是一款基于端云融合 AI 视觉识别与极速离线双引擎架构的智能家庭与个人物品收纳管理应用。" +
                                "致力于解决传统收纳软件“录入繁琐”、“找不到物品存放位置”、“过期浪费”等核心痛点。" +
                                "产品设计遵循“零门槛拍照录入”、“多层级空间透视”、“隐私数据自主可控”三大准则。"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhitepaperSection(
                        title = "二、 AI 视觉识别与智能录入指南",
                        icon = Icons.Default.AutoAwesome,
                        content = "1. 单物极速识别：拍摄单个物品，AI 将秒级识别物品名称、智能推荐分类、估算存放过期天数并生成防重提炼标签。\n" +
                                "2. ListPlus 多物全家福拆解：对桌面或收纳箱拍摄全张照片，AI 将智能定位画幅中所有可见独立物品，自动生成切割独立照片并一次性批量导入。\n" +
                                "3. 购物小票解析：对小票拍照，自动批量解析消费明细、单价、购买日期并批量生成入库条目。\n" +
                                "4. 调优建议：批量识别推荐使用 1024px/1280px 高清模型模式，可在设置中灵活调整。"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhitepaperSection(
                        title = "三、 多维空间与箱码透视体系",
                        icon = Icons.Default.GridView,
                        content = "1. 空间层级建构：支持建立“客厅”、“主卧”、“地下室”等大空间，并在大空间下建立具体的收纳盒、抽屉、柜子（如 BOX-2026-001）。\n" +
                                "2. 一箱一码（条形码/二维码）：通过打印或贴标签方式将唯一箱码贴于收纳箱外部。使用扫码寻物扫描箱码，无需打开箱盖即可透视箱内所有物品。\n" +
                                "3. 快速查找：搜索页面支持按关键词、防重标签、分类及空间多重过滤。"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhitepaperSection(
                        title = "四、 效期预警与资产统计",
                        icon = Icons.Default.TrendingUp,
                        content = "1. 过期预警机制：支持针对食品、药品、化妆品等设置保质期与提前预警天数。软件将在首页与物品详情中高亮提示即期与过期物品。\n" +
                                "2. 资产价值统计：自动统计全量物品采购价格与总资产价值，帮助您掌握个人资产沉淀与消费习惯。"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WhitepaperSection(
                        title = "五、 数据安全与备份同步",
                        icon = Icons.Default.Security,
                        content = "1. 私有 WebDAV 跨端同步：支持配置坚果云、Nextcloud 等私有 WebDAV 服务器，实现跨设备多端数据自动同步。\n" +
                                "2. JSON 无损备份恢复：支持一键导出包含全部图片路径、空间分类、物品属性的本地 JSON 文本，并支持一键导入无损恢复。\n" +
                                "3. 应用 PIN 密码锁：提供安全加密应用锁，保护您的私人居家空间资产不被窥探。"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "万物收纳Pro 团队 敬上",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Footer Actions
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("已知晓并返回")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhitepaperSection(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
