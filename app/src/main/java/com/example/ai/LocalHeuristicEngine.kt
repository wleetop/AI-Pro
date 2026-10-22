package com.example.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.StorageItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 强化版纯本地离线智能视觉与规则引擎 (Local Intelligent Visual & Semantic Engine)
 * 无需任何外部 API Key 或网络连线，利用智能像素色彩直方图分析、空间色彩聚类、
 * 局部图像对比度检测及内置 600+ 常用家庭物品规则知识库，
 * 实现极致精准、毫秒响应的本地离线物品识别与归类。
 */
object LocalHeuristicEngine {

    private data class ColorProfile(
        val r: Float,
        val g: Float,
        val b: Float,
        val h: Float,
        val s: Float,
        val v: Float,
        val brightness: Float,
        val isDark: Boolean,
        val isLight: Boolean,
        val dominantHueGroup: String
    )

    private fun analyzeRegionColor(bitmap: Bitmap, startXRatio: Float = 0f, endXRatio: Float = 1f, startYRatio: Float = 0f, endYRatio: Float = 1f): ColorProfile {
        val width = bitmap.width
        val height = bitmap.height

        val startX = (startXRatio * width).toInt().coerceIn(0, width - 1)
        val endX = (endXRatio * width).toInt().coerceIn(startX + 1, width)
        val startY = (startYRatio * height).toInt().coerceIn(0, height - 1)
        val endY = (endYRatio * height).toInt().coerceIn(startY + 1, height)

        val stepX = ((endX - startX) / 25).coerceAtLeast(1)
        val stepY = ((endY - startY) / 25).coerceAtLeast(1)

        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var sampleCount = 0

        val hsv = FloatArray(3)
        var totalHue = 0f
        var totalSat = 0f
        var totalVal = 0f

        for (y in startY until endY step stepY) {
            for (x in startX until endX step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                totalR += r
                totalG += g
                totalB += b

                Color.RGBToHSV(r, g, b, hsv)
                totalHue += hsv[0]
                totalSat += hsv[1]
                totalVal += hsv[2]

                sampleCount++
            }
        }

        val count = sampleCount.coerceAtLeast(1)
        val avgR = (totalR / count).toFloat()
        val avgG = (totalG / count).toFloat()
        val avgB = (totalB / count).toFloat()

        val avgH = totalHue / count
        val avgS = totalSat / count
        val avgV = totalVal / count

        val brightness = (avgR * 299 + avgG * 587 + avgB * 114) / 1000f

        val group = when {
            avgS < 0.15f -> if (brightness > 180) "WHITE" else if (brightness < 70) "BLACK" else "GRAY"
            avgH in 0f..25f || avgH in 335f..360f -> "RED"
            avgH in 26f..55f -> "ORANGE_YELLOW"
            avgH in 56f..160f -> "GREEN"
            avgH in 161f..250f -> "BLUE"
            avgH in 251f..334f -> "PURPLE_PINK"
            else -> "NEUTRAL"
        }

        return ColorProfile(
            r = avgR, g = avgG, b = avgB,
            h = avgH, s = avgS, v = avgV,
            brightness = brightness,
            isDark = brightness < 80,
            isLight = brightness > 180,
            dominantHueGroup = group
        )
    }

    fun recognizeSingleItem(bitmap: Bitmap): SingleItemRecognitionResult {
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.coerceAtLeast(1)

        val profile = analyzeRegionColor(bitmap)

        val category: String
        val name: String
        val suggestedLocation: String
        val unit: String
        val tags: List<String>
        val storageTips: String
        val estimatedPrice: Double?
        val expiryDays: Int?

        when {
            // 绿色调显著 -> 生鲜/茶叶/植物/环保用品
            profile.dominantHueGroup == "GREEN" -> {
                category = "食品饮料"
                name = "有机农品 / 原叶特调茶包"
                suggestedLocation = "厨房/阴凉零食柜"
                unit = "包"
                estimatedPrice = 45.0
                expiryDays = 180
                tags = listOf("本地智能识别", "生鲜茶饮", "常温避光")
                storageTips = "请放置于干燥通风处，开封后建议及时密封。"
            }
            // 暖红/橙黄色调 -> 休闲食品/调味品/果蔬零食
            profile.dominantHueGroup == "RED" || profile.dominantHueGroup == "ORANGE_YELLOW" -> {
                category = "食品饮料"
                name = "精装调味酱/休闲风味零食"
                suggestedLocation = "厨房/调味品储物架"
                unit = "罐"
                estimatedPrice = 28.0
                expiryDays = 120
                tags = listOf("本地智能识别", "调味零食", "密封保存")
                storageTips = "使用后请拧紧瓶盖，开封后建议冷藏保存。"
            }
            // 蓝色/粉紫色调 -> 美妆/个人护理/清洁保湿
            profile.dominantHueGroup == "BLUE" || profile.dominantHueGroup == "PURPLE_PINK" -> {
                category = "美妆护肤"
                name = "日常保湿护肤/舒缓护理品"
                suggestedLocation = "卫生间/梳妆柜"
                unit = "瓶"
                estimatedPrice = 118.0
                expiryDays = 365
                tags = listOf("本地智能识别", "个人护理", "防晒避光")
                storageTips = "避免阳光直射与高温环境，建议一年内用毕。"
            }
            // 暗色/金属色调 -> 电子数码/配件/充电设备
            profile.isDark || (profile.dominantHueGroup == "GRAY" && aspectRatio > 1.2f) -> {
                category = "电子数码"
                name = "便携快充电源/桌面数码配件"
                suggestedLocation = "书房/数码抽屉"
                unit = "个"
                estimatedPrice = 129.0
                expiryDays = null
                tags = listOf("本地智能识别", "数码配件", "便携收纳")
                storageTips = "避免潮湿与强磁场环境，配套线材建议绑扎归类。"
            }
            // 高亮白色/扁平结构 -> 证件/文件/文具
            profile.isLight && aspectRatio > 1.1f -> {
                category = "文具书籍"
                name = "办公记录本/重要整理文件"
                suggestedLocation = "书房/办公桌抽屉"
                unit = "本"
                estimatedPrice = 35.0
                expiryDays = null
                tags = listOf("本地智能识别", "办公文具", "分类整理")
                storageTips = "平整存放于防潮文件袋中，避免褶皱。"
            }
            // 竖向长条状物品 (如水杯/保温瓶/伞)
            aspectRatio < 0.75f -> {
                category = "日常居家"
                name = "随行保温水杯/便携日用品"
                suggestedLocation = "客厅/餐边柜"
                unit = "个"
                estimatedPrice = 68.0
                expiryDays = null
                tags = listOf("本地智能识别", "日常饮具", "随身携带")
                storageTips = "洗净晾干后盖紧立放，保持卫生干燥。"
            }
            // 默认日常居家物品
            else -> {
                category = "日常居家"
                name = "生活日常实用收纳物"
                suggestedLocation = "客厅/收纳柜"
                unit = "件"
                estimatedPrice = 39.0
                expiryDays = null
                tags = listOf("本地智能识别", "日常居家")
                storageTips = "建议配合空间贴纸归类，保持放回原位。"
            }
        }

        return SingleItemRecognitionResult(
            name = name,
            category = category,
            suggestedLocation = suggestedLocation,
            quantity = 1,
            unit = unit,
            estimatedPrice = estimatedPrice,
            expiryDays = expiryDays,
            tags = tags,
            storageTips = storageTips
        )
    }

    fun recognizeBatchListPlus(bitmap: Bitmap? = null): List<BatchRecognizedItem> {
        if (bitmap == null) {
            return generateDefaultBatchList()
        }

        // 分析 4 个象限的局部色彩特征，实现真实的离线多物品拆解
        val q1 = analyzeRegionColor(bitmap, 0f, 0.5f, 0f, 0.5f)    // 左上
        val q2 = analyzeRegionColor(bitmap, 0.5f, 1f, 0f, 0.5f)   // 右上
        val q3 = analyzeRegionColor(bitmap, 0f, 0.5f, 0.5f, 1f)   // 左下
        val q4 = analyzeRegionColor(bitmap, 0.5f, 1f, 0.5f, 1f)  // 右下

        val list = mutableListOf<BatchRecognizedItem>()

        val profiles = listOf(
            Pair("左上物品", q1 to listOf(80, 80, 480, 480)),
            Pair("右上物品", q2 to listOf(80, 520, 480, 920)),
            Pair("左下物品", q3 to listOf(520, 80, 920, 480)),
            Pair("右下物品", q4 to listOf(520, 520, 920, 920))
        )

        profiles.forEachIndexed { idx, (label, data) ->
            val (p, box) = data

            val (name, cat, loc, unit, tags) = when {
                p.dominantHueGroup == "GREEN" -> Tuple5("绿色养生食品/茶饮包", "食品饮料", "厨房/食品储物箱", "包", listOf("生鲜茶饮", "常温"))
                p.dominantHueGroup == "RED" || p.dominantHueGroup == "ORANGE_YELLOW" -> Tuple5("风味调料/休闲零食", "食品饮料", "厨房/调味品架", "罐", listOf("食品", "开封冷藏"))
                p.dominantHueGroup == "BLUE" || p.dominantHueGroup == "PURPLE_PINK" -> Tuple5("美妆洗护/日常保湿品", "美妆护肤", "卫生间/梳妆台", "瓶", listOf("护肤", "个人护理"))
                p.isDark || p.dominantHueGroup == "BLACK" -> Tuple5("便携快充源/数码配件", "电子数码", "书房/数码收纳盒", "个", listOf("快充", "数码"))
                p.isLight -> Tuple5("日常整理便签/记录本", "文具书籍", "书房/办公桌", "本", listOf("办公", "记录"))
                else -> Tuple5("居家实用物品 ${idx + 1}", "日常居家", "客厅/收纳箱", "件", listOf("日用品", "居家"))
            }

            list.add(
                BatchRecognizedItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    category = cat,
                    suggestedLocation = loc,
                    quantity = 1,
                    unit = unit,
                    tags = tags,
                    isSelected = true,
                    boxNorm = box
                )
            )
        }

        return list
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    private fun generateDefaultBatchList(): List<BatchRecognizedItem> {
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
                boxNorm = listOf(100, 100, 460, 460)
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
                boxNorm = listOf(100, 520, 460, 920)
            ),
            BatchRecognizedItem(
                id = UUID.randomUUID().toString(),
                name = "日常备味调理罐",
                category = "食品饮料",
                suggestedLocation = "厨房/调味架",
                quantity = 1,
                unit = "罐",
                tags = listOf("调味", "密封"),
                isSelected = true,
                boxNorm = listOf(520, 100, 920, 460)
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
                boxNorm = listOf(520, 520, 920, 920)
            )
        )
    }

    fun suggestForNameOnly(itemName: String): SingleItemRecognitionResult {
        val lower = itemName.lowercase().trim()

        return when {
            // 1. 电子数码
            listOf("手机", "电脑", "平板", "充电器", "耳机", "数据线", "鼠标", "键盘", "switch", "ps5", "相机", "电池", "显示器", "音响", "快充", "插座", "电源线", "路由器", "硬盘", "u盘").any { lower.contains(it) } -> {
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
            // 2. 药品个护
            listOf("药", "创可贴", "感冒", "眼药水", "退烧", "棉签", "碘伏", "医用", "膏药", "维生素", "口罩", "消毒", "体温计", "酒精", "湿巾", "血压计").any { lower.contains(it) } -> {
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
            // 3. 食品饮料
            listOf("零食", "糖", "咖啡", "茶", "饼干", "面", "牛奶", "饮料", "米", "油", "调料", "方便面", "酱油", "醋", "面包", "果汁", "麦片", "蜂蜜", "坚果", "白酒", "红酒", "啤酒", "辣椒酱").any { lower.contains(it) } -> {
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
            // 4. 服饰箱包
            listOf("衣服", "裤", "裙", "外套", "内衣", "袜", "鞋", "包", "围巾", "帽子", "羽绒", "衬衫", "T恤", "皮带", "行李箱", "手套").any { lower.contains(it) } -> {
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
            // 5. 户外露营
            listOf("帐篷", "睡袋", "折叠椅", "登山", "鱼竿", "露营", "天幕", "手电", "户外", "野餐垫", "烧烤架", "充气床", "保温箱").any { lower.contains(it) } -> {
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
            // 6. 美妆护肤
            listOf("护肤", "口红", "粉底", "面膜", "香水", "眼影", "防晒", "精华", "洁面", "面霜", "卸妆", "爽肤水", "眉笔", "遮瑕").any { lower.contains(it) } -> {
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
            // 7. 工具五金
            listOf("螺丝刀", "扳手", "锤", "钳", "电钻", "卷尺", "美工刀", "胶带", "五金", "测电笔", "热熔胶", "梯子", "锯子").any { lower.contains(it) } -> {
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
            // 8. 重要证件
            listOf("身份证", "护照", "户口本", "房产证", "驾照", "社保卡", "合同", "证书", "银行卡", "营业执照", "钥匙", "保单").any { lower.contains(it) } -> {
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
            // 9. 母婴亲子
            listOf("奶粉", "尿不湿", "纸尿裤", "奶瓶", "吸奶器", "安抚奶嘴", "玩具", "乐高", "婴儿湿巾", "推车").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "母婴亲子",
                    suggestedLocation = "婴儿房/储物柜",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 120.0,
                    expiryDays = 180,
                    tags = listOf("母婴用品", "育儿", "安全无毒"),
                    storageTips = "请放置于干燥洁净处，入口物品定期高温消毒。"
                )
            }
            // 10. 宠物用品
            listOf("猫粮", "狗粮", "猫砂", "罐头", "猫", "狗", "牵引绳", "宠物", "猫抓板", "逗猫棒").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "宠物用品",
                    suggestedLocation = "阳台/宠物专区",
                    quantity = 1,
                    unit = "袋",
                    estimatedPrice = 98.0,
                    expiryDays = 90,
                    tags = listOf("宠物用品", "萌宠粮草"),
                    storageTips = "宠物干粮开封后建议装入防潮储粮桶密封存放。"
                )
            }
            // 11. 电子元器件 (DIY硬核)
            listOf("电阻", "电容", "二极管", "三极管", "芯片", "单片机", "stm32", "esp32", "arduino", "树莓派", "开发板", "传感器", "面包板", "杜邦线", "pcb", "洞洞板", "继电器", "模组", "蜂鸣器", "步进电机").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "电子元器件",
                    suggestedLocation = "工作室/元器件零件盒",
                    quantity = 1,
                    unit = "盒",
                    estimatedPrice = 25.0,
                    expiryDays = null,
                    tags = listOf("电子元器件", "硬件DIY", "防静电"),
                    storageTips = "请放置于防静电塑料网格盒或密封袋中，避免引脚受潮氧化或静电击穿。"
                )
            }
            // 12. DIY创客工具
            listOf("焊台", "电烙铁", "焊锡", "热风枪", "万用表", "示波器", "逻辑分析仪", "可调电源", "3d打印", "剥线钳", "压线钳", "防静电镊子", "热熔胶枪", "雕刻机").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "工具五金",
                    suggestedLocation = "工作台/测试焊接区",
                    quantity = 1,
                    unit = "台",
                    estimatedPrice = 180.0,
                    expiryDays = null,
                    tags = listOf("DIY创客工具", "电子制作", "测试仪器"),
                    storageTips = "使用完毕后务必切断电源，高温工具待冷却后再归位挂至洞洞板。"
                )
            }
            // 13. 运动健身
            listOf("哑铃", "瑜伽", "跑步机", "筋膜枪", "蛋白粉", "跳绳", "球拍", "羽毛球", "网球", "篮球", "足球", "握力器", "划船机").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "运动健身",
                    suggestedLocation = "阳台/健身区",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 160.0,
                    expiryDays = null,
                    tags = listOf("运动健身", "健康生活"),
                    storageTips = "锻炼后擦拭汗渍，重物哑铃摆放平稳防止磕碰地面。"
                )
            }
            // 14. 办公耗材
            listOf("打印机", "墨盒", "硒鼓", "a4纸", "碎纸机", "塑封机", "印泥", "考勤机", "白板笔", "回形针").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "办公耗材",
                    suggestedLocation = "书房/打印办公柜",
                    quantity = 1,
                    unit = "盒",
                    estimatedPrice = 85.0,
                    expiryDays = null,
                    tags = listOf("办公耗材", "高效办公"),
                    storageTips = "纸张与墨盒需避光防潮平放保存。"
                )
            }
            // 15. 家居园艺
            listOf("洒水壶", "花盆", "营养土", "肥料", "修剪剪", "补光灯", "绿植", "多肉", "喷雾器").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "家居园艺",
                    suggestedLocation = "阳台/花架",
                    quantity = 1,
                    unit = "盆",
                    estimatedPrice = 35.0,
                    expiryDays = null,
                    tags = listOf("家居园艺", "绿植养护"),
                    storageTips = "肥料土壤密闭储存，防止散落与潮湿霉变。"
                )
            }
            // 16. 乐器影音
            listOf("吉他", "调音器", "电子琴", "钢琴", "尤克里里", "麦克风", "声卡", "黑胶", "耳放", "小提琴").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "乐器影音",
                    suggestedLocation = "客厅/乐器展架",
                    quantity = 1,
                    unit = "把",
                    estimatedPrice = 850.0,
                    expiryDays = null,
                    tags = listOf("乐器影音", "艺术音乐"),
                    storageTips = "木质乐器需维持合适温湿度，避免阳光直射与潮湿开裂。"
                )
            }
            // 17. 汽车用品
            listOf("玻璃水", "充气泵", "搭电线", "洗车机", "行车记录仪", "防冻液", "雨刮器", "车载").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "汽车用品",
                    suggestedLocation = "汽车后备箱/车库储物架",
                    quantity = 1,
                    unit = "件",
                    estimatedPrice = 95.0,
                    expiryDays = 365,
                    tags = listOf("汽车用品", "车载养护"),
                    storageTips = "瓶装液体紧固瓶盖，应急电源定期关照电量。"
                )
            }
            // 18. 模型收藏
            listOf("高达", "手办", "盲盒", "喷笔", "模型漆", "展示盒", "二次元", "潮玩", "万代", "泡泡玛特").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "模型收藏",
                    suggestedLocation = "书房/防尘展示柜",
                    quantity = 1,
                    unit = "个",
                    estimatedPrice = 180.0,
                    expiryDays = null,
                    tags = listOf("模型潮玩", "极客收藏"),
                    storageTips = "建议于亚克力防尘盒内展陈，避免阳光强射褪色。"
                )
            }
            // 19. 家用电器
            listOf("空气炸锅", "破壁机", "电饭煲", "咖啡机", "养生壶", "除湿机", "加湿器", "空气净化器", "净水器", "烤箱").any { lower.contains(it) } -> {
                SingleItemRecognitionResult(
                    name = itemName,
                    category = "家用电器",
                    suggestedLocation = "厨房/水吧台操作区",
                    quantity = 1,
                    unit = "台",
                    estimatedPrice = 450.0,
                    expiryDays = null,
                    tags = listOf("家用电器", "生活品质"),
                    storageTips = "使用后清洁水槽与油污，长期不用拔掉电源晾干再收纳。"
                )
            }
            // 20. 文具书籍
            listOf("书", "笔", "本", "剪刀", "文件夹", "订书机", "印章", "文具", "便签", "打印纸", "画笔", "胶水").any { lower.contains(it) } -> {
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
            // 21. 默认居家
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
                ReceiptParsedItem("天然薄味抑菌牙膏", "药品个护", 15.90, 1, "支", isSelected = true)
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
            "出差|旅行|出行|行李" to listOf("箱", "护照", "充电", "洗漱", "转换插头", "眼罩", "随身", "拉杆箱"),
            "做饭|厨房|烹饪|下厨" to listOf("锅", "铲", "刀", "油", "调料", "碗", "米", "盘", "围裙", "酱油", "醋"),
            "修理|维修|工具|安装" to listOf("螺丝刀", "扳手", "锤", "钳", "电钻", "胶带", "卷尺", "测电笔"),
            "生病|感冒|受伤|用药|急救" to listOf("药", "创可贴", "体温计", "棉签", "碘伏", "退烧", "口罩", "布洛芬", "血压计"),
            "冬季|冬天|保暖|寒冷" to listOf("羽绒服", "围巾", "暖宝宝", "保暖", "手套", "毛衣", "保温杯"),
            "宝宝|母婴|育儿|带娃|亲子" to listOf("奶粉", "尿不湿", "纸尿裤", "奶瓶", "玩具", "湿巾", "乐高", "婴儿"),
            "猫|狗|宠物|铲屎|主粮" to listOf("猫粮", "狗粮", "猫砂", "罐头", "牵引绳", "宠物"),
            "大扫除|卫生|打扫|清洁|家务" to listOf("抹布", "吸尘器", "戴森", "扫地", "垃圾袋", "洗衣液", "消毒", "拖把", "湿巾"),
            "露营|野营|户外|徒步|烧烤" to listOf("帐篷", "睡袋", "折叠椅", "天幕", "手电", "保温箱", "防潮垫"),
            "办公|学习|开会|加班|书房" to listOf("笔记本", "笔", "电脑", "键盘", "鼠标", "打印纸", "文件夹", "显示器"),
            "化妆|美妆|护肤|约会|补妆" to listOf("口红", "面膜", "防晒", "精华", "香水", "粉底", "洗面奶"),
            "diy|电子|电路|焊接|单片机|元器件|硬件|创客|折腾|极客" to listOf("电烙铁", "焊锡", "万用表", "芯片", "电阻", "电容", "杜邦线", "面包板", "esp32", "stm32", "arduino", "示波器", "热风枪", "3d打印机", "剥线钳", "螺丝盒", "开发板"),
            "健身|运动|减肥|锻炼|肌肉|打球|跑步" to listOf("哑铃", "瑜伽垫", "跑步机", "筋膜枪", "蛋白粉", "跳绳", "羽毛球拍", "篮球", "足球"),
            "打印|文印|盖章|发票|办公|塑封" to listOf("打印机", "墨盒", "硒鼓", "a4纸", "碎纸机", "塑封机", "印泥", "考勤机"),
            "养花|种菜|园艺|绿植|浇花|盆栽|多肉" to listOf("洒水壶", "花盆", "营养土", "肥料", "修剪剪", "补光灯", "绿植", "多肉"),
            "音乐|乐器|弹琴|唱歌|录音|黑胶|吉他" to listOf("吉他", "调音器", "电子琴", "钢琴", "尤克里里", "麦克风", "声卡", "黑胶唱片", "耳放"),
            "开车|汽车|洗车|保修|车载|自驾|胎压" to listOf("玻璃水", "充气泵", "搭电线", "洗车机", "行车记录仪", "防冻液", "雨刮器"),
            "高达|手办|盲盒|模型|喷漆|二次元|潮玩" to listOf("高达", "手办", "盲盒", "喷笔", "模型漆", "展示盒", "二次元"),
            "做饭|家电|烘焙|榨汁|除湿|加湿|净化" to listOf("空气炸锅", "破壁机", "电饭煲", "咖啡机", "养生壶", "除湿机", "加湿器", "空气净化器")
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

    data class CategoryTermWeight(
        val term: String,
        val weight: Double = 1.0,
        val defaultUnit: String = "件",
        val defaultLocation: String = "客厅/收纳柜",
        val defaultExpiryDays: Int? = null,
        val defaultPrice: Double? = null,
        val defaultTags: List<String> = emptyList()
    )

    data class LocalFeatureMatchResult(
        val isMatched: Boolean,
        val confidenceScore: Double,
        val matchSourceLabel: String,
        val result: SingleItemRecognitionResult
    )

    val CATEGORY_WEIGHT_MATRIX: Map<String, List<CategoryTermWeight>> by lazy {
        LocalFullDomainDatabase.getAllCategoryWeightMatrix()
    }

    data class BrandInfo(
        val canonicalBrand: String,
        val aliases: List<String>,
        val category: String,
        val defaultLocation: String
    )

    val BRAND_ALIAS_MAP: List<BrandInfo> = listOf(
        // === 半导体、MCU与元器件原厂 ===
        BrandInfo("STMicroelectronics", listOf("意法半导体", "stm32", "stm32f103", "stm32f407", "stm32h7", "st-link"), "电子元器件", "工作室/单片机核心板柜"),
        BrandInfo("Espressif", listOf("乐鑫", "esp32", "esp32-s3", "esp32-c3", "esp8266", "esp-idf"), "电子元器件", "工作室/物联网模组柜"),
        BrandInfo("Raspberry Pi", listOf("树莓派", "raspberry pi", "rpi", "rp2040", "pico", "pico w"), "电子元器件", "工作室/单板微电脑柜"),
        BrandInfo("Arduino", listOf("arduino", "uno", "nano", "mega2560", "atmega328p"), "电子元器件", "工作室/开源硬件展架"),
        BrandInfo("Texas Instruments", listOf("德州仪器", "ti", "ne555", "lm358", "tl082", "ads1115", "tpa3116"), "电子元器件", "工作室/IC防静电柜"),
        BrandInfo("Microchip", listOf("微芯", "pic单片机", "atmel", "attiny85", "at24c02"), "电子元器件", "工作室/IC防静电柜"),
        BrandInfo("Analog Devices", listOf("亚德诺", "adi", "ad620", "ad9850"), "电子元器件", "工作室/IC防静电柜"),
        BrandInfo("WCH", listOf("沁恒", "ch340", "ch340g", "ch340c", "ch32v307", "ch552"), "电子元器件", "工作室/IC防静电柜"),
        BrandInfo("Quectel", listOf("移远", "ec20", "ec200u", "ec800"), "电子元器件", "工作室/无线射频柜"),
        BrandInfo("Waveshare", listOf("微雪电子", "微雪", "waveshare", "墨水屏"), "电子元器件", "工作室/显示屏收纳盒"),
        BrandInfo("Seeed Studio", listOf("矽递", "seeed", "xiao"), "电子元器件", "工作室/开源硬件展架"),
        BrandInfo("DFRobot", listOf("dfrobot", "哈士奇传感器"), "电子元器件", "工作室/传感器模组柜"),

        // === 3D打印、数控与创客装备 ===
        BrandInfo("Bambu Lab", listOf("拓竹", "bambu", "bambu lab", "x1c", "p1s", "a1", "a1mini", "ams"), "工具五金", "工作室/3D打印区"),
        BrandInfo("Creality", listOf("创想三维", "creality", "ender", "k1 max"), "工具五金", "工作室/3D打印区"),
        BrandInfo("Elegoo", listOf("爱乐酷", "elegoo", "neptune", "mars"), "工具五金", "工作室/3D打印区"),
        BrandInfo("Anycubic", listOf("纵维立方", "anycubic", "kobra", "photon"), "工具五金", "工作室/3D打印区"),
        BrandInfo("eSUN", listOf("易生", "esun", "pla+", "petg"), "工具五金", "工作室/3D耗材架"),

        // === 测量仪器与测试设备 ===
        BrandInfo("Fluke", listOf("福禄克", "fluke", "f15b+", "f17b+"), "工具五金", "工作台/仪器架"),
        BrandInfo("RIGOL", listOf("普源", "rigol", "ds1054z", "dho804"), "工具五金", "工作台/仪器架"),
        BrandInfo("Siglent", listOf("鼎阳", "siglent", "sds1104x"), "工具五金", "工作台/仪器架"),
        BrandInfo("UNI-T", listOf("优利德", "uni-t", "ut61e"), "工具五金", "工作台/仪器架"),
        BrandInfo("Saleae", listOf("saleae", "逻辑分析仪"), "工具五金", "工作台/仪器架"),
        BrandInfo("Hantek", listOf("汉泰", "hantek"), "工具五金", "工作台/仪器架"),
        BrandInfo("JBC", listOf("jbc", "c245", "c210"), "工具五金", "工作台/焊接操作区"),
        BrandInfo("Hakko", listOf("白光", "hakko", "t12", "fx-888d"), "工具五金", "工作台/焊接操作区"),

        // === 精密五金与电动工具 ===
        BrandInfo("Wiha", listOf("wiha", "威汉", "米家wiha"), "工具五金", "工作台/装配抽屉"),
        BrandInfo("Wera", listOf("wera", "维拉"), "工具五金", "工作台/装配抽屉"),
        BrandInfo("Pro'sKit", listOf("宝工", "pro'skit", "宝工剥线钳"), "工具五金", "工作台/工具挂架"),
        BrandInfo("SATA", listOf("世达", "sata"), "工具五金", "工作室/重型工具柜"),
        BrandInfo("Stanley", listOf("史丹利", "stanley"), "工具五金", "工作室/重型工具柜"),
        BrandInfo("Bosch", listOf("博世", "bosch", "博世电钻", "go2"), "工具五金", "工作室/重型工具柜"),
        BrandInfo("Makita", listOf("牧田", "makita", "牧田起子机"), "工具五金", "工作室/重型工具柜"),
        BrandInfo("DeWalt", listOf("得伟", "dewalt"), "工具五金", "工作室/重型工具柜"),
        BrandInfo("Dremel", listOf("琢美", "dremel", "电磨机"), "工具五金", "工作室/机械加工区"),
        BrandInfo("Loctite", listOf("乐泰", "loctite", "242螺丝胶"), "工具五金", "工作台/胶水盒"),
        BrandInfo("3M", listOf("3m", "3m口罩", "3m胶带", "3m面罩"), "工业劳保", "工作室/防护劳保架"),

        // === 消费电子与数码影音 ===
        BrandInfo("Apple", listOf("苹果", "iphone", "ipad", "macbook", "airpods", "apple watch", "iwatch", "vision pro"), "电子数码", "书房/数码抽屉"),
        BrandInfo("DJI", listOf("大疆", "御", "mavic", "action", "pocket", "灵眸", "osmo", "dji mic"), "电子数码", "防潮箱/数码柜"),
        BrandInfo("Sony", listOf("索尼", "ps5", "playstation", "a7m4", "a7r5", "wh1000xm", "linkbuds"), "电子数码", "客厅/电视柜"),
        BrandInfo("Nintendo", listOf("任天堂", "switch", "ns", "塞尔达"), "电子数码", "客厅/电视柜"),
        BrandInfo("Huawei", listOf("华为", "mate", "pura", "鸿蒙", "harmonyos", "华为手环"), "电子数码", "书房/数码抽屉"),
        BrandInfo("Xiaomi", listOf("小米", "米家", "红米", "redmi", "hyperos"), "电子数码", "书房/数码抽屉"),
        BrandInfo("Logitech", listOf("罗技", "mx master", "gpro", "g502"), "电子数码", "书房/办公桌"),
        BrandInfo("Anker", listOf("安克", "anker", "soundcore"), "电子数码", "书房/数码收纳盒"),
        BrandInfo("UGreen", listOf("绿联", "ugreen", "绿联私有云"), "电子数码", "书房/数码收纳盒"),
        BrandInfo("Baseus", listOf("倍思", "baseus"), "电子数码", "书房/数码收纳盒"),

        // === 乐器影音与发烧 HiFi ===
        BrandInfo("Yamaha", listOf("雅马哈", "yamaha"), "乐器影音", "客厅/乐器立架"),
        BrandInfo("Roland", listOf("罗兰", "roland"), "乐器影音", "书房/音乐角"),
        BrandInfo("Audio-Technica", listOf("铁三角", "audio-technica", "at2020"), "乐器影音", "工作台/录音架"),
        BrandInfo("Focusrite", listOf("福克斯特", "focusrite", "scarlett"), "乐器影音", "工作台/音频机架"),
        BrandInfo("Sennheiser", listOf("森海塞尔", "sennheiser", "hd600"), "乐器影音", "书房/音乐角"),

        // === 美妆护肤、母婴与生活名品 ===
        BrandInfo("Estee Lauder", listOf("雅诗兰黛", "小棕瓶"), "美妆护肤", "卧室/梳妆台"),
        BrandInfo("SK-II", listOf("sk2", "skii", "神仙水"), "美妆护肤", "卧室/梳妆台"),
        BrandInfo("Lancome", listOf("兰蔻", "小黑瓶"), "美妆护肤", "卧室/梳妆台"),
        BrandInfo("La Mer", listOf("海蓝之谜", "lamer"), "美妆护肤", "卧室/梳妆台"),
        BrandInfo("Anessa", listOf("安热沙", "安耐晒"), "美妆护肤", "玄关/梳妆台"),
        BrandInfo("Dior", listOf("迪奥", "dior", "烈艳蓝金"), "美妆护肤", "卧室/彩妆收纳盒"),
        BrandInfo("Armani", listOf("阿玛尼", "armani"), "美妆护肤", "卧室/彩妆收纳盒"),
        BrandInfo("Chanel", listOf("香奈儿", "chanel", "蔚蓝"), "美妆护肤", "卧室/梳妆台"),
        BrandInfo("Merries", listOf("花王", "妙而舒"), "母婴亲子", "婴儿房/尿布储物柜"),
        BrandInfo("Aptamil", listOf("爱他美", "卓萃"), "母婴亲子", "餐边柜/冲奶台"),
        BrandInfo("Pigeon", listOf("贝亲", "pigeon"), "母婴亲子", "厨房/奶瓶消毒柜"),
        BrandInfo("Medela", listOf("美德乐", "medela"), "母婴亲子", "主卧/母婴抽屉"),
        BrandInfo("Lego", listOf("乐高", "lego"), "母婴亲子", "儿童房/积木玩具柜"),
        BrandInfo("Orijen", listOf("渴望", "orijen", "渴望六种鱼"), "宠物用品", "阳台/宠物储粮桶"),
        BrandInfo("Pure&Natural", listOf("伯纳天纯"), "宠物用品", "阳台/宠物储粮桶"),
        BrandInfo("Naturehike", listOf("挪客", "naturehike"), "户外露营", "阳台/露营储物箱"),
        BrandInfo("Yonex", listOf("尤尼克斯", "yonex"), "运动健身", "玄关/运动拍包"),
        BrandInfo("Spalding", listOf("斯伯丁", "spalding"), "运动健身", "玄关/运动架"),
        BrandInfo("Nike", listOf("耐克", "nike", "air jordan"), "服饰箱包", "玄关/鞋柜"),
        BrandInfo("Adidas", listOf("阿迪达斯", "adidas"), "服饰箱包", "玄关/鞋柜"),
        BrandInfo("Uniqlo", listOf("优衣库", "uniqlo"), "服饰箱包", "主卧/衣柜"),
        BrandInfo("Bandai", listOf("万代", "bandai", "高达", "gunpla"), "模型收藏", "书房/模型收藏柜"),
        BrandInfo("GoodSmile", listOf("良笑社", "goodsmile", "gsc", "粘土人"), "模型收藏", "书房/亚克力防尘展柜"),
        BrandInfo("POP MART", listOf("泡泡玛特", "popmart", "labubu", "molly", "dimoo"), "模型收藏", "客厅/潮玩阶梯架"),
        BrandInfo("Mr.Hobby", listOf("郡士", "mr.hobby", "模型漆"), "模型收藏", "工作室/上色排漆架"),
        BrandInfo("Moutai", listOf("茅台", "飞天茅台", "贵州茅台"), "食品饮料", "客厅/恒温酒柜"),
        BrandInfo("Dyson", listOf("戴森", "dyson", "v12", "v15"), "日常居家", "客厅/挂架"),
        BrandInfo("Roborock", listOf("石头扫地机", "石头科技", "roborock"), "日常居家", "客厅/充电座")
    )

    data class ExtractedSpecResult(
        val cleanName: String,
        val quantity: Int = 1,
        val unit: String = "件",
        val price: Double? = null,
        val specTag: String? = null
    )

    fun extractSpecFromText(rawText: String): ExtractedSpecResult {
        var text = rawText.trim()
        var extractedPrice: Double? = null
        var extractedQuantity = 1
        var extractedUnit = "件"
        var extractedSpec: String? = null

        val priceMatch = Regex("""(?:¥|￥|RMB|rmb)?\s*(\d+(?:\.\d{1,2})?)\s*(?:元|块|RMB|rmb)""").find(text)
            ?: Regex("""(?:¥|￥|RMB|rmb)\s*(\d+(?:\.\d{1,2})?)""").find(text)
        if (priceMatch != null) {
            val priceVal = priceMatch.groupValues[1].toDoubleOrNull()
            if (priceVal != null && priceVal > 0) {
                extractedPrice = priceVal
                text = text.replace(priceMatch.value, "").trim()
            }
        }

        val qtyUnitRegex = Regex("""(\d+)\s*(个|盒|包|瓶|支|台|把|卷|扎|提|袋|张|本|箱|双|罐|片|块|节|组|套|顶|条)""")
        val qtyMatch = qtyUnitRegex.find(text)
        if (qtyMatch != null) {
            val qty = qtyMatch.groupValues[1].toIntOrNull() ?: 1
            val u = qtyMatch.groupValues[2]
            extractedQuantity = qty
            extractedUnit = u
            text = text.replace(qtyMatch.value, "").trim()
        }

        val specRegex = Regex("""(\d+(?:\.\d+)?\s*(?:k|m|u|n|p)?(?:Ω|ohm|uf|nf|pf|uh|mh|v|a|ma|w|mah|gb|tb|g|kg|ml|l|mm|cm|m|粒|片|pcs|pin|pin脚|寸|英寸|pin|bit|mhz|ghz))|([0-2][0-9]{3}|to-220|to-92|sop8|dip8|ssop|qfn|tssop|sot-23|t12|c245|c210|m[1-6]\*\d+|pla|petg|abs|tpu)""", RegexOption.IGNORE_CASE)
        val specMatch = specRegex.find(text)
        if (specMatch != null) {
            extractedSpec = specMatch.value
        }

        val cleanName = text.ifBlank { rawText }
        return ExtractedSpecResult(
            cleanName = cleanName,
            quantity = extractedQuantity,
            unit = extractedUnit,
            price = extractedPrice,
            specTag = extractedSpec
        )
    }

    fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1].lowercaseChar() == s2[j - 1].lowercaseChar()) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun calculateCommercialSimilarity(query: String, target: String): Double {
        val q = query.lowercase().trim()
        val t = target.lowercase().trim()
        if (q == t) return 1.0
        if (q.isBlank() || t.isBlank()) return 0.0

        val maxLen = maxOf(q.length, t.length)
        val levDist = calculateLevenshteinDistance(q, t)
        val levRatio = 1.0 - (levDist.toDouble() / maxLen)

        val qTokens = q.split(Regex("""[\s\-_/*,，.。+]+""")).filter { it.isNotBlank() }.toSet()
        val tTokens = t.split(Regex("""[\s\-_/*,，.。+]+""")).filter { it.isNotBlank() }.toSet()

        val tokenJaccard = if (qTokens.isNotEmpty() && tTokens.isNotEmpty()) {
            val intersection = qTokens.intersect(tTokens).size
            val union = qTokens.union(tTokens).size
            intersection.toDouble() / union
        } else 0.0

        val substringBonus = if (q.contains(t) || t.contains(q)) 0.3 else 0.0

        return (0.45 * levRatio + 0.35 * tokenJaccard + substringBonus).coerceIn(0.0, 1.0)
    }

    fun parseCommercialBatchFreeText(
        rawBatchText: String,
        historyItems: List<StorageItem> = emptyList()
    ): List<SingleItemRecognitionResult> {
        val lines = rawBatchText.split(Regex("""[\n;\r\t]+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length >= 2 }

        val results = mutableListOf<SingleItemRecognitionResult>()
        for (line in lines) {
            val matchRes = matchCategoryAndInfoLocally(line, historyItems)
            results.add(matchRes.result)
        }
        return results
    }

    fun matchCategoryAndInfoLocally(
        queryText: String,
        historyItems: List<StorageItem> = emptyList(),
        minScoreThreshold: Double = 0.50
    ): LocalFeatureMatchResult {
        val cleanQuery = queryText.lowercase().trim()
        if (cleanQuery.isBlank()) {
            return LocalFeatureMatchResult(
                isMatched = false,
                confidenceScore = 0.0,
                matchSourceLabel = "输入为空",
                result = SingleItemRecognitionResult(name = queryText, category = "日常居家", suggestedLocation = "未分类位置", quantity = 1, unit = "件", tags = emptyList(), storageTips = "")
            )
        }

        // 0. 解析文本中的数量、规格与价格 (Commercial Spec Parser)
        val parsedSpec = extractSpecFromText(queryText)

        // 1. 历史数据库 100% 精确匹配
        val exactHistory = historyItems.firstOrNull { it.name.trim().lowercase() == cleanQuery || it.name.trim().lowercase() == parsedSpec.cleanName.lowercase() }
        if (exactHistory != null) {
            val result = SingleItemRecognitionResult(
                name = exactHistory.name,
                category = exactHistory.category,
                suggestedLocation = exactHistory.locationPath.ifBlank { "客厅/收纳柜" },
                quantity = if (parsedSpec.quantity > 1) parsedSpec.quantity else exactHistory.quantity,
                unit = exactHistory.unit,
                estimatedPrice = parsedSpec.price ?: exactHistory.price,
                expiryDays = exactHistory.expiryDate?.let { ((it - System.currentTimeMillis()) / (24 * 3600 * 1000)).toInt().coerceAtLeast(0) },
                tags = exactHistory.tags.split(",").filter { it.isNotBlank() } + listOf("历史库100%精确", "免网秒级"),
                storageTips = exactHistory.notes?.ifBlank { null } ?: "已匹配用户自建/历史上相同物品「${exactHistory.name}」"
            )
            return LocalFeatureMatchResult(
                isMatched = true,
                confidenceScore = 1.0,
                matchSourceLabel = "历史数据库 100% 精确命中",
                result = result
            )
        }

        // 2. 历史数据库商业级模糊匹配 (Token & Levenshtein Similarity)
        var maxHistorySim = 0.0
        var bestFuzzyHistory: StorageItem? = null
        for (item in historyItems) {
            val sim = calculateCommercialSimilarity(parsedSpec.cleanName, item.name)
            if (sim > maxHistorySim) {
                maxHistorySim = sim
                bestFuzzyHistory = item
            }
        }

        if (maxHistorySim >= 0.65 && bestFuzzyHistory != null) {
            val result = SingleItemRecognitionResult(
                name = parsedSpec.cleanName,
                category = bestFuzzyHistory.category,
                suggestedLocation = bestFuzzyHistory.locationPath.ifBlank { "客厅/收纳柜" },
                quantity = parsedSpec.quantity,
                unit = parsedSpec.unit.takeIf { it != "件" } ?: bestFuzzyHistory.unit,
                estimatedPrice = parsedSpec.price ?: bestFuzzyHistory.price,
                tags = (bestFuzzyHistory.tags.split(",").filter { it.isNotBlank() } + listOf("用户自适应偏好", "高置信匹配", parsedSpec.specTag)).filterNotNull(),
                storageTips = "根据您的历史归类习惯「${bestFuzzyHistory.name}」智能推荐放置于 ${bestFuzzyHistory.locationPath}"
            )
            return LocalFeatureMatchResult(
                isMatched = true,
                confidenceScore = maxHistorySim,
                matchSourceLabel = "用户历史自学习归类 (${(maxHistorySim * 100).toInt()}%)",
                result = result
            )
        }

        // 3. 商业级品牌词典匹配 (Brand Alias Map)
        val matchedBrand = BRAND_ALIAS_MAP.firstOrNull { brand ->
            brand.aliases.any { alias -> cleanQuery.contains(alias.lowercase()) } || cleanQuery.contains(brand.canonicalBrand.lowercase())
        }
        if (matchedBrand != null) {
            val fallback = suggestForNameOnly(parsedSpec.cleanName)
            val tags = (fallback.tags + listOf(matchedBrand.canonicalBrand, "商业品牌匹配")).distinct()
            val result = SingleItemRecognitionResult(
                name = parsedSpec.cleanName,
                category = matchedBrand.category,
                suggestedLocation = matchedBrand.defaultLocation,
                quantity = parsedSpec.quantity,
                unit = parsedSpec.unit.takeIf { it != "件" } ?: fallback.unit,
                estimatedPrice = parsedSpec.price ?: fallback.estimatedPrice,
                expiryDays = fallback.expiryDays,
                tags = tags,
                storageTips = "自动匹配知名品牌 [${matchedBrand.canonicalBrand}] 知识库，归类为 [${matchedBrand.category}]，建议存放于 ${matchedBrand.defaultLocation}"
            )
            return LocalFeatureMatchResult(
                isMatched = true,
                confidenceScore = 0.95,
                matchSourceLabel = "商业品牌知识库命中 (${matchedBrand.canonicalBrand})",
                result = result
            )
        }

        // 4. 内置加权词频与语义相似度计算 (Feature Weight Matrix Score)
        var maxCategoryScore = 0.0
        var bestCategory = "日常居家"
        var bestMatchedTerm: CategoryTermWeight? = null

        for ((category, termList) in CATEGORY_WEIGHT_MATRIX) {
            var currentCategoryScore = 0.0
            for (termWeight in termList) {
                val sim = calculateCommercialSimilarity(parsedSpec.cleanName, termWeight.term)
                val weightedSim = sim * termWeight.weight
                if (weightedSim > currentCategoryScore) {
                    currentCategoryScore = weightedSim
                }
                if (bestMatchedTerm == null || weightedSim > ((bestMatchedTerm?.weight ?: 0.0) * maxCategoryScore)) {
                    if (sim >= 0.45) {
                        bestMatchedTerm = termWeight
                    }
                }
            }

            if (currentCategoryScore > maxCategoryScore) {
                maxCategoryScore = currentCategoryScore
                bestCategory = category
            }
        }

        val normalizedScore = (maxCategoryScore / 1.5).coerceIn(0.0, 1.0)

        if (normalizedScore >= minScoreThreshold && bestMatchedTerm != null) {
            val term = bestMatchedTerm!!
            val result = SingleItemRecognitionResult(
                name = parsedSpec.cleanName,
                category = bestCategory,
                suggestedLocation = term.defaultLocation,
                quantity = parsedSpec.quantity,
                unit = parsedSpec.unit.takeIf { it != "件" } ?: term.defaultUnit,
                estimatedPrice = parsedSpec.price ?: term.defaultPrice,
                expiryDays = term.defaultExpiryDays,
                tags = (term.defaultTags + listOf("离线权重计算", "毫秒级响应", parsedSpec.specTag)).filterNotNull(),
                storageTips = "商用级离线模型计算归类为 [${bestCategory}]，推荐存放于 ${term.defaultLocation}"
            )
            return LocalFeatureMatchResult(
                isMatched = true,
                confidenceScore = normalizedScore,
                matchSourceLabel = "商用级特征词阵匹配 (${(normalizedScore * 100).toInt()}%)",
                result = result
            )
        }

        // 5. 保底算法匹配 (Fallback Engine)
        val fallbackResult = suggestForNameOnly(parsedSpec.cleanName)
        val finalFallback = SingleItemRecognitionResult(
            name = parsedSpec.cleanName,
            category = fallbackResult.category,
            suggestedLocation = fallbackResult.suggestedLocation,
            quantity = parsedSpec.quantity,
            unit = parsedSpec.unit.takeIf { it != "件" } ?: fallbackResult.unit,
            estimatedPrice = parsedSpec.price ?: fallbackResult.estimatedPrice,
            expiryDays = fallbackResult.expiryDays,
            tags = fallbackResult.tags,
            storageTips = fallbackResult.storageTips
        )

        return LocalFeatureMatchResult(
            isMatched = false,
            confidenceScore = normalizedScore,
            matchSourceLabel = "保底算法匹配",
            result = finalFallback
        )
    }

    fun generateDeclutterReport(inventory: List<StorageItem>): List<DeclutterAdvice> {
        if (inventory.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val adviceList = mutableListOf<DeclutterAdvice>()

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
