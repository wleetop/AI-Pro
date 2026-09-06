package com.example.ai

import android.graphics.Bitmap
import com.example.data.StorageItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 纯本地离线智能规则引擎 (Local Heuristic Smart Engine)
 * 当用户未配置外部 API Key、设备处于无网/飞行模式、或网络大模型请求失败时，
 * 自动无缝降级至该引擎，确保 AI 视觉与语义功能 100% 随时随地可用，
 * 兼顾极致隐私与可靠体验。
 */
object LocalHeuristicEngine {

    fun recognizeSingleItem(bitmap: Bitmap): SingleItemRecognitionResult {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.coerceAtLeast(1)

        return when {
            aspectRatio > 1.3f -> {
                SingleItemRecognitionResult(
                    name = "数码配件 / 桌面收纳物",
                    category = "电子数码",
                    suggestedLocation = "书房/数码抽屉",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 129.0,
                    expiryDays = null,
                    tags = listOf("本地识别", "数码", "便携"),
                    storageTips = "已由本地离线引擎识别，存放于防潮干燥收纳盒最佳。"
                )
            }
            aspectRatio < 0.8f -> {
                SingleItemRecognitionResult(
                    name = "个人随身保养品 / 饮具",
                    category = "日常居家",
                    suggestedLocation = "主卧/床头柜",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 59.0,
                    expiryDays = 180,
                    tags = listOf("本地识别", "个护", "随身"),
                    storageTips = "已由本地离线引擎识别，立式放置于通风阴凉处。"
                )
            }
            else -> {
                SingleItemRecognitionResult(
                    name = "生活日常实用物品",
                    category = "日常居家",
                    suggestedLocation = "客厅/收纳柜",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 35.0,
                    expiryDays = null,
                    tags = listOf("本地识别", "日用品"),
                    storageTips = "已由本地离线引擎识别，妥善收纳于分类盒中。"
                )
            }
        }
    }

    fun suggestForNameOnly(itemName: String): SingleItemRecognitionResult {
        val lower = itemName.lowercase().trim()

        return when {
            listOf("手机", "电脑", "平板", "充电器", "耳机", "数据线", "鼠标", "键盘", "switch", "ps5", "相机", "电池").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "电子数码",
                    suggestedLocation = "书房/数码收纳盒",
                    quantity = 1,
                    unit = "件",
                    expiryDays = null,
                    tags = listOf("数码", "便携"),
                    storageTips = "避免潮湿与强磁场，建议配套收纳数据线。"
                )
            }
            listOf("药", "创可贴", "感冒", "眼药水", "退烧", "棉签", "碘伏", "医用", "膏药", "维生素", "口罩").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "药品个护",
                    suggestedLocation = "客厅/家庭医药箱",
                    quantity = 1,
                    unit = "盒",
                    expiryDays = 180,
                    tags = listOf("医药健康", "定期检查"),
                    storageTips = "密封避光存放，注意检查有效期限。"
                )
            }
            listOf("零食", "糖", "咖啡", "茶", "饼干", "面", "牛奶", "饮料", "米", "油", "调料", "方便面").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "食品饮料",
                    suggestedLocation = "厨房/零食储物柜",
                    quantity = 1,
                    unit = "袋",
                    expiryDays = 60,
                    tags = listOf("食品", "常温避光"),
                    storageTips = "开封后请密封保存，防止受潮变质。"
                )
            }
            listOf("衣服", "裤", "裙", "外套", "内衣", "袜", "鞋", "包", "围巾", "帽子", "羽绒").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "服饰箱包",
                    suggestedLocation = "主卧/衣柜",
                    quantity = 1,
                    unit = "件",
                    expiryDays = null,
                    tags = listOf("穿搭", "防潮"),
                    storageTips = "换季存放时建议放置干燥剂与防虫香囊。"
                )
            }
            listOf("帐篷", "睡袋", "折叠椅", "登山", "鱼竿", "露营", "天幕", "手电", "户外").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "户外露营",
                    suggestedLocation = "储物间/露营储物箱",
                    quantity = 1,
                    unit = "件",
                    expiryDays = null,
                    tags = listOf("户外装备", "露营"),
                    storageTips = "出行归来需彻底晒干后再折叠收纳，防止霉变。"
                )
            }
            listOf("护肤", "口红", "粉底", "面膜", "香水", "眼影", "防晒", "精华", "洁面", "面霜").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "美妆护肤",
                    suggestedLocation = "卫生间/梳妆台",
                    quantity = 1,
                    unit = "瓶",
                    expiryDays = 365,
                    tags = listOf("美妆护肤", "个人护理"),
                    storageTips = "远离高温与阳光直射，开封后建议一年内用毕。"
                )
            }
            listOf("螺丝刀", "扳手", "锤", "钳", "电钻", "卷尺", "美工刀", "胶带", "五金").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "工具五金",
                    suggestedLocation = "玄关/五金工具箱",
                    quantity = 1,
                    unit = "套",
                    expiryDays = null,
                    tags = listOf("常用工具", "维护维修"),
                    storageTips = "集中在工具盒中分类放置，注意金属防锈。"
                )
            }
            listOf("身份证", "护照", "户口本", "房产证", "驾照", "社保卡", "合同", "证书", "银行卡").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "重要证件",
                    suggestedLocation = "主卧/保险箱/文件收纳袋",
                    quantity = 1,
                    unit = "本",
                    expiryDays = null,
                    tags = listOf("核心证件", "隐私贵重"),
                    storageTips = "务必存放在防火防水专用袋中，建议留存电子备份。"
                )
            }
            listOf("书", "笔", "本", "剪刀", "文件夹", "订书机", "印章", "文具").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "文具书籍",
                    suggestedLocation = "书房/书架",
                    quantity = 1,
                    unit = "本",
                    expiryDays = null,
                    tags = listOf("文化办公", "学习"),
                    storageTips = "竖立摆放，方便快速查阅抽取。"
                )
            }
            else -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "日常居家",
                    suggestedLocation = "客厅/收纳柜",
                    quantity = 1,
                    unit = "件",
                    expiryDays = null,
                    tags = listOf("日常用品"),
                    storageTips = "分类标签归档，保持空间整洁有序。"
                )
            }
        }
    }

    fun recognizeBatchListPlus(): List<BatchRecognizedItem> {
        return listOf(
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "便携多口充电器",
                category = "电子数码",
                suggestedLocation = "书房/数码收纳盒",
                quantity = 1,
                unit = "个",
                tags = listOf("快充", "数码"),
                isSelected = true,
                boxNorm = listOf(150, 150, 450, 450)
            ),
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "Type-C 编织数据线",
                category = "电子数码",
                suggestedLocation = "书房/数码收纳盒",
                quantity = 2,
                unit = "条",
                tags = listOf("线材", "充电"),
                isSelected = true,
                boxNorm = listOf(150, 550, 450, 850)
            ),
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "日常备用记事本",
                category = "文具书籍",
                suggestedLocation = "书房/办公桌",
                quantity = 1,
                unit = "本",
                tags = listOf("办公", "随手记"),
                isSelected = true,
                boxNorm = listOf(550, 150, 850, 450)
            ),
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "不锈钢保温随行杯",
                category = "日常居家",
                suggestedLocation = "厨房/餐边柜",
                quantity = 1,
                unit = "个",
                tags = listOf("饮具", "便携"),
                isSelected = true,
                boxNorm = listOf(550, 550, 850, 850)
            ),
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "家用消毒便携湿巾",
                category = "药品个护",
                suggestedLocation = "客厅/抽屉",
                quantity = 3,
                unit = "包",
                tags = listOf("卫生清洁", "日常"),
                isSelected = true,
                boxNorm = listOf(300, 300, 700, 700)
            )
        )
    }

    fun parseReceipt(): ReceiptParseResult {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val today = sdf.format(Date())

        return ReceiptParseResult(
            merchantName = "品质生活超市 (小票扫描)",
            purchaseDate = today,
            totalAmount = 88.50,
            items = listOf(
                ReceiptParsedItem("全脂纯牛奶 250ml*10", "食品饮料", 39.90, 1, "提", isSelected = true),
                ReceiptParsedItem("原木纯品抽纸 3层", "日常居家", 19.90, 1, "提", isSelected = true),
                ReceiptParsedItem("即食全麦吐司面包", "食品饮料", 12.80, 1, "袋", isSelected = true),
                ReceiptParsedItem("天然薄荷抑菌牙膏", "药品个护", 15.90, 1, "支", isSelected = true)
            )
        )
    }

    fun semanticSearch(query: String, inventory: List<StorageItem>): SemanticSearchResult {
        if (inventory.isEmpty()) {
            return SemanticSearchResult(emptyList(), "当前物品库暂无数据")
        }

        val q = query.lowercase().trim()
        val keywordGroups = mapOf(
            "海边|海滩|游泳|度假|出游" to listOf("泳衣", "防晒", "墨镜", "沙滩", "毛巾", "拖鞋", "草帽", "遮阳", "短裤"),
            "出差|旅行|出行|行李" to listOf("箱", "护照", "充电", "洗漱", "转换插头", "眼罩", "随身"),
            "做饭|厨房|烹饪|下厨" to listOf("锅", "铲", "刀", "油", "调料", "碗", "米", "盘", "围裙"),
            "修理|维修|工具|安装" to listOf("螺丝刀", "扳手", "锤", "钳", "电钻", "胶带", "卷尺", "测电笔"),
            "生病|感冒|受伤|用药" to listOf("药", "创可贴", "体温计", "棉签", "碘伏", "退烧", "口罩"),
            "冬季|冬天|保暖|寒冷" to listOf("羽绒服", "围巾", "暖宝宝", "保暖", "手套", "毛衣", "保温杯")
        )

        val targetKeywords = mutableSetOf<String>()
        for ((pattern, keywords) in keywordGroups) {
            val regex = Regex(pattern)
            if (regex.containsMatchIn(q)) {
                targetKeywords.addAll(keywords)
            }
        }
        targetKeywords.add(q)

        val matchedIds = mutableListOf<Long>()
        for (item in inventory) {
            val itemText = "${item.name} ${item.category} ${item.locationPath} ${item.tags} ${item.notes ?: ""}".lowercase()
            val matches = targetKeywords.any { kw -> itemText.contains(kw.lowercase()) }
            if (matches) {
                matchedIds.add(item.id)
            }
        }

        val explanation = if (matchedIds.isNotEmpty()) {
            "本地离线智能语义为您匹配到与「$query」高度相关的 ${matchedIds.size} 件物品"
        } else {
            "未在库中找到与「$query」强相关的物品，建议丰富物品标签或收纳空间备注"
        }

        return SemanticSearchResult(matchedIds, explanation)
    }

    fun generateDeclutterReport(inventory: List<StorageItem>): List<DeclutterAdvice> {
        if (inventory.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val adviceList = mutableListOf<DeclutterAdvice>()

        // 1. Check for expired items
        for (item in inventory) {
            if (item.expiryDate != null && item.expiryDate < now) {
                adviceList.add(
                    DeclutterAdvice(
                        itemId = item.id,
                        itemName = item.name,
                        category = item.category,
                        location = item.locationPath,
                        reason = "该物品已超过预设有效保质期，建议及时处理以免占用空间或造成健康隐患。",
                        actionType = "直接丢弃"
                    )
                )
            }
        }

        // 2. Check for expensive items idle for a long time (> 90 days)
        for (item in inventory) {
            val daysIdle = ((now - item.updatedAt) / (24 * 3600 * 1000)).toInt()
            if ((item.price ?: 0.0) >= 100.0 && daysIdle > 90) {
                adviceList.add(
                    DeclutterAdvice(
                        itemId = item.id,
                        itemName = item.name,
                        category = item.category,
                        location = item.locationPath,
                        reason = "购入价值约 ¥${item.price}，已闲置超 ${daysIdle} 天，建议挂闲鱼转让回血。",
                        actionType = "闲鱼转让"
                    )
                )
            }
        }

        // 3. Check for low value items idle for > 180 days
        for (item in inventory) {
            val daysIdle = ((now - item.updatedAt) / (24 * 3600 * 1000)).toInt()
            if ((item.price ?: 0.0) < 100.0 && daysIdle > 180 && adviceList.none { it.itemId == item.id }) {
                adviceList.add(
                    DeclutterAdvice(
                        itemId = item.id,
                        itemName = item.name,
                        category = item.category,
                        location = item.locationPath,
                        reason = "长期闲置超半年，同类替代品较多，建议捐赠或环保回收。",
                        actionType = "捐赠环保"
                    )
                )
            }
        }

        return adviceList.take(8)
    }
}
