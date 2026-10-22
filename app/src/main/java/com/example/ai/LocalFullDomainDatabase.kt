package com.example.ai

/**
 * 全领域商用级本地智能知识库 (Full-Domain Commercial Offline Database)
 * 涵盖全场景生活领域：医药急救、母婴、数码、生鲜食品、服饰箱包、美妆洗护、
 * 户外探险、运动健身、办公文印、乐器影音、汽车车品、模玩潮玩、园艺、智能家电、重要证件及工业劳保。
 */
object LocalFullDomainDatabase {

    val CONSUMER_DIGITAL: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("iPhone", 1.8, "台", "书房/数码抽屉", defaultPrice = 5999.0, defaultTags = listOf("苹果", "iOS", "智能手机", "数码")),
        LocalHeuristicEngine.CategoryTermWeight("华为手机", 1.7, "台", "书房/数码抽屉", defaultPrice = 4999.0, defaultTags = listOf("华为", "Mate/Pura", "鸿蒙", "旗舰手机")),
        LocalHeuristicEngine.CategoryTermWeight("小米手机", 1.7, "台", "书房/数码抽屉", defaultPrice = 2999.0, defaultTags = listOf("小米", "HyperOS", "拍照手机")),
        LocalHeuristicEngine.CategoryTermWeight("iPad平板", 1.8, "台", "书房/数码柜", defaultPrice = 3899.0, defaultTags = listOf("Apple", "iPad Pro/Air", "视网膜屏")),
        LocalHeuristicEngine.CategoryTermWeight("MacBook笔记本", 1.8, "台", "书房/办公桌", defaultPrice = 8999.0, defaultTags = listOf("Apple", "M系列芯片", "超轻薄便携")),
        LocalHeuristicEngine.CategoryTermWeight("ThinkPad商务本", 1.7, "台", "书房/办公桌", defaultPrice = 6500.0, defaultTags = listOf("联想", "小红点", "商务办公")),
        LocalHeuristicEngine.CategoryTermWeight("4K高清显示器", 1.7, "台", "书房/办公桌", defaultPrice = 1899.0, defaultTags = listOf("Type-C反向供电", "IPS广色域", "办公大屏")),
        LocalHeuristicEngine.CategoryTermWeight("机械键盘", 1.7, "把", "书房/电脑桌", defaultPrice = 399.0, defaultTags = listOf("热插拔", "客制化", "RGB背光", "青轴/红轴")),
        LocalHeuristicEngine.CategoryTermWeight("无线鼠标", 1.6, "只", "书房/电脑桌", defaultPrice = 189.0, defaultTags = listOf("罗技MX", "人体工学", "双模连接")),
        LocalHeuristicEngine.CategoryTermWeight("AirPods无线耳机", 1.8, "副", "玄关/随身包袋", defaultPrice = 1299.0, defaultTags = listOf("主动降噪", "通透模式", "空间音频")),
        LocalHeuristicEngine.CategoryTermWeight("头戴降噪耳机", 1.7, "副", "书房/耳机挂架", defaultPrice = 1899.0, defaultTags = listOf("索尼WH-1000XM", "高保真", "长续航")),
        LocalHeuristicEngine.CategoryTermWeight("100W氮化镓快充头", 1.8, "个", "书房/数码收纳盒", defaultPrice = 149.0, defaultTags = listOf("GaN", "多口快充", "PD3.0/QC4+")),
        LocalHeuristicEngine.CategoryTermWeight("20000mAh大容量充电宝", 1.8, "个", "玄关/随身包袋", defaultPrice = 139.0, defaultTags = listOf("双向快充", "数显电量", "可上飞机")),
        LocalHeuristicEngine.CategoryTermWeight("索尼微单数码相机", 1.9, "台", "防潮箱/摄影柜", defaultPrice = 12800.0, defaultTags = listOf("全画幅", "A7M4", "4K60P视频", "防抖")),
        LocalHeuristicEngine.CategoryTermWeight("大疆DJI航拍无人机", 1.9, "架", "阳台/露营箱", defaultPrice = 4599.0, defaultTags = listOf("Mini/Air", "三轴云台", "超长图传")),
        LocalHeuristicEngine.CategoryTermWeight("任天堂Switch游戏机", 1.8, "台", "客厅/电视柜", defaultPrice = 1899.0, defaultTags = listOf("OLED版", "便携体感", "手柄底座")),
        LocalHeuristicEngine.CategoryTermWeight("索尼PS5游戏主机", 1.8, "台", "客厅/电视柜", defaultPrice = 3599.0, defaultTags = listOf("4K光追", "超高速SSD", "DualSense手柄")),
        LocalHeuristicEngine.CategoryTermWeight("千兆WiFi6双频路由器", 1.7, "台", "玄关/弱电箱", defaultPrice = 299.0, defaultTags = listOf("Mesh组网", "全千兆网口", "穿墙抗干扰")),
        LocalHeuristicEngine.CategoryTermWeight("2TB移动固态硬盘SSD", 1.8, "个", "书房/数码收纳盒", defaultPrice = 850.0, defaultTags = listOf("Type-C 10Gbps", "高速读写", "抗震防摔")),
        LocalHeuristicEngine.CategoryTermWeight("智能手表", 1.7, "只", "卧室/床头充电座", defaultPrice = 1999.0, defaultTags = listOf("心率血氧监测", "运动计步", "NFC刷卡"))
    )

    val MEDICAL_AND_CARE: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("医用创可贴", 1.8, "盒", "家庭医药箱", defaultExpiryDays = 365, defaultPrice = 12.0, defaultTags = listOf("防水透气", "止血消炎", "急救常备")),
        LocalHeuristicEngine.CategoryTermWeight("布洛芬缓释胶囊", 1.9, "盒", "家庭医药箱", defaultExpiryDays = 180, defaultPrice = 24.0, defaultTags = listOf("退烧镇痛", "OTC", "应急备药")),
        LocalHeuristicEngine.CategoryTermWeight("连花清瘟/感冒清热颗粒", 1.8, "盒", "家庭医药箱", defaultExpiryDays = 180, defaultPrice = 28.0, defaultTags = listOf("清热解毒", "感冒常备")),
        LocalHeuristicEngine.CategoryTermWeight("医用碘伏棉棒", 1.8, "盒", "家庭医药箱", defaultExpiryDays = 180, defaultPrice = 15.0, defaultTags = listOf("独立封装", "皮肤消毒", "外伤处理")),
        LocalHeuristicEngine.CategoryTermWeight("红霉素软膏", 1.7, "支", "家庭医药箱", defaultExpiryDays = 365, defaultPrice = 5.0, defaultTags = listOf("抗菌消炎", "外用药膏")),
        LocalHeuristicEngine.CategoryTermWeight("无菌医用纱布绷带", 1.7, "包", "家庭医药箱", defaultExpiryDays = 365, defaultPrice = 18.0, defaultTags = listOf("透气包扎", "止血防感染")),
        LocalHeuristicEngine.CategoryTermWeight("红外电子体温计", 1.9, "支", "家庭医药箱", defaultPrice = 68.0, defaultTags = listOf("额温枪", "一秒测温", "高烧背光预警")),
        LocalHeuristicEngine.CategoryTermWeight("电子上臂式血压计", 1.9, "台", "父母房/床头柜", defaultPrice = 230.0, defaultTags = listOf("欧姆龙", "一键全自动", "语音播报心率")),
        LocalHeuristicEngine.CategoryTermWeight("便携家用指夹式血氧仪", 1.8, "个", "家庭医药箱", defaultPrice = 85.0, defaultTags = listOf("脉搏血氧监测", "OLED屏显示")),
        LocalHeuristicEngine.CategoryTermWeight("KN95医用防护口罩", 1.8, "盒", "玄关/收纳架", defaultExpiryDays = 365, defaultPrice = 35.0, defaultTags = listOf("3D立体", "高过滤防粉尘飞沫")),
        LocalHeuristicEngine.CategoryTermWeight("75%医用酒精消毒喷雾", 1.8, "瓶", "玄关/消毒角", defaultExpiryDays = 180, defaultPrice = 16.0, defaultTags = listOf("速干杀菌", "随身消杀")),
        LocalHeuristicEngine.CategoryTermWeight("复合维生素C泡腾片", 1.7, "管", "餐边柜/营养角", defaultExpiryDays = 180, defaultPrice = 32.0, defaultTags = listOf("增强免疫力", "果味冲饮")),
        LocalHeuristicEngine.CategoryTermWeight("深海鱼油软胶囊", 1.7, "瓶", "餐边柜/营养角", defaultExpiryDays = 365, defaultPrice = 158.0, defaultTags = listOf("Omega-3", "高纯度DHA/EPA", "心血管保健")),
        LocalHeuristicEngine.CategoryTermWeight("碳酸钙D3咀嚼片", 1.7, "瓶", "餐边柜/营养角", defaultExpiryDays = 180, defaultPrice = 68.0, defaultTags = listOf("补钙", "促进骨骼健康")),
        LocalHeuristicEngine.CategoryTermWeight("玻璃酸钠滴眼液", 1.8, "盒", "书房/办公桌", defaultExpiryDays = 60, defaultPrice = 45.0, defaultTags = listOf("缓解眼疲劳", "无防腐剂人工泪液")),
        LocalHeuristicEngine.CategoryTermWeight("声波电动牙刷", 1.7, "支", "卫生间/洗手台", defaultPrice = 229.0, defaultTags = listOf("IPX7防水", "杜邦软毛", "无线感应充电")),
        LocalHeuristicEngine.CategoryTermWeight("抗敏多效牙膏", 1.6, "支", "卫生间/置物架", defaultExpiryDays = 365, defaultPrice = 26.0, defaultTags = listOf("舒适达", "修护牙釉质", "清新口气"))
    )

    val FOOD_AND_BEVERAGE: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("特级初榨橄榄油", 1.8, "瓶", "厨房/调味品架", defaultExpiryDays = 180, defaultPrice = 128.0, defaultTags = listOf("低温冷榨", "健康轻食", "地中海品质")),
        LocalHeuristicEngine.CategoryTermWeight("头道特级生抽酱油", 1.8, "瓶", "厨房/调味品架", defaultExpiryDays = 180, defaultPrice = 18.0, defaultTags = listOf("0添加", "自然酿造", "烹饪提鲜")),
        LocalHeuristicEngine.CategoryTermWeight("陈酿老陈醋", 1.7, "瓶", "厨房/调味品架", defaultExpiryDays = 365, defaultPrice = 15.0, defaultTags = listOf("传统固态发酵", "酸香浓郁")),
        LocalHeuristicEngine.CategoryTermWeight("研磨黑胡椒粒带研磨器", 1.7, "瓶", "厨房/调味品架", defaultExpiryDays = 365, defaultPrice = 22.0, defaultTags = listOf("现磨牛排调料", "辛香纯正")),
        LocalHeuristicEngine.CategoryTermWeight("金龙鱼优质东北大米", 1.8, "袋", "厨房/密封米桶", defaultExpiryDays = 120, defaultPrice = 65.0, defaultTags = listOf("真空包装", "五常稻花香", "油润软糯")),
        LocalHeuristicEngine.CategoryTermWeight("纯牛奶 250ml*24", 1.9, "箱", "厨房/常温储物角", defaultExpiryDays = 45, defaultPrice = 58.0, defaultTags = listOf("原生高钙", "3.6g优质乳蛋白", "每日早餐")),
        LocalHeuristicEngine.CategoryTermWeight("意式深烘精品咖啡豆", 1.9, "袋", "水吧台/咖啡角", defaultExpiryDays = 90, defaultPrice = 88.0, defaultTags = listOf("单向排气阀", "阿拉比卡", "浓郁坚果巧克力风味")),
        LocalHeuristicEngine.CategoryTermWeight("冻干速溶黑咖啡粉", 1.8, "罐", "办公室/水吧台", defaultExpiryDays = 180, defaultPrice = 45.0, defaultTags = listOf("0糖0脂", "冷热双冲", "提神醒脑")),
        LocalHeuristicEngine.CategoryTermWeight("特级大红袍/正山小种茶叶", 1.9, "罐", "客厅/茶台", defaultExpiryDays = 365, defaultPrice = 360.0, defaultTags = listOf("武夷岩茶", "传统炭焙", "送礼自饮")),
        LocalHeuristicEngine.CategoryTermWeight("明前特级西湖龙井新茶", 1.9, "罐", "冰箱保鲜/茶水柜", defaultExpiryDays = 180, defaultPrice = 480.0, defaultTags = listOf("鲜嫩豆香", "扁平光滑", "春季采摘")),
        LocalHeuristicEngine.CategoryTermWeight("每日混合坚果礼盒", 1.8, "盒", "客厅/茶几", defaultExpiryDays = 90, defaultPrice = 118.0, defaultTags = listOf("核桃腰果巴旦木", "干湿分离包", "健康零食")),
        LocalHeuristicEngine.CategoryTermWeight("纯可可脂黑巧克力", 1.7, "盒", "客厅/零食柜", defaultExpiryDays = 180, defaultPrice = 38.0, defaultTags = listOf("85%高浓度", "低糖醇厚")),
        LocalHeuristicEngine.CategoryTermWeight("原味全麦即食欧包", 1.7, "袋", "厨房/面包箱", defaultExpiryDays = 15, defaultPrice = 19.9, defaultTags = listOf("控卡饱腹", "低GI碳水")),
        LocalHeuristicEngine.CategoryTermWeight("53度飞天茅台酱香白酒", 2.0, "瓶", "客厅/恒温酒柜", defaultExpiryDays = 3650, defaultPrice = 2499.0, defaultTags = listOf("贵州茅台", "高端酱香", "保值收藏")),
        LocalHeuristicEngine.CategoryTermWeight("法国原瓶进口干红葡萄酒", 1.9, "瓶", "客厅/恒温酒柜", defaultExpiryDays = 1800, defaultPrice = 268.0, defaultTags = listOf("赤霞珠", "橡木桶陈酿", "果香单宁平衡"))
    )

    val BEAUTY_AND_SKINCARE: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("雅诗兰黛特润修护肌活精华露(小棕瓶)", 1.9, "瓶", "卧室/梳妆台", defaultExpiryDays = 180, defaultPrice = 780.0, defaultTags = listOf("夜间修护", "抗老淡纹", "经典精华")),
        LocalHeuristicEngine.CategoryTermWeight("SK-II护肤精华露(神仙水)", 1.9, "瓶", "卧室/梳妆台", defaultExpiryDays = 180, defaultPrice = 1390.0, defaultTags = listOf("PITERA成分", "调理水油平衡", "提亮肤质")),
        LocalHeuristicEngine.CategoryTermWeight("兰蔻小黑瓶肌底精华液", 1.9, "瓶", "卧室/梳妆台", defaultExpiryDays = 180, defaultPrice = 850.0, defaultTags = listOf("强韧屏障", "促进吸收")),
        LocalHeuristicEngine.CategoryTermWeight("海蓝之谜精华面霜", 2.0, "罐", "卧室/梳妆台", defaultExpiryDays = 240, defaultPrice = 1800.0, defaultTags = listOf("神奇活性精萃", "深层修护滋养", "贵妇面霜")),
        LocalHeuristicEngine.CategoryTermWeight("安热沙金灿倍护防晒乳", 1.9, "瓶", "玄关/梳妆台", defaultExpiryDays = 180, defaultPrice = 198.0, defaultTags = listOf("SPF50+ PA++++", "遇热耐汗", "户外必备")),
        LocalHeuristicEngine.CategoryTermWeight("Dior迪奥烈艳蓝金唇膏(口红)", 1.9, "支", "卧室/彩妆收纳盒", defaultExpiryDays = 365, defaultPrice = 350.0, defaultTags = listOf("999正红色", "哑光丝绒", "显白持妆")),
        LocalHeuristicEngine.CategoryTermWeight("阿玛尼大师光芒粉底液", 1.9, "瓶", "卧室/彩妆收纳盒", defaultExpiryDays = 365, defaultPrice = 580.0, defaultTags = listOf("遮瑕自然", "持久服帖", "细腻光泽")),
        LocalHeuristicEngine.CategoryTermWeight("玻尿酸补水保湿面膜", 1.8, "盒", "卧室/梳妆抽屉", defaultExpiryDays = 180, defaultPrice = 99.0, defaultTags = listOf("密集补水", "舒缓修护", "蚕丝膜布")),
        LocalHeuristicEngine.CategoryTermWeight("香奈儿蔚蓝男士淡香水", 1.9, "瓶", "卧室/梳妆台", defaultExpiryDays = 730, defaultPrice = 920.0, defaultTags = listOf("木质芳香调", "持久留香", "绅士魅力")),
        LocalHeuristicEngine.CategoryTermWeight("无泡温和氨基酸洁面乳", 1.8, "支", "卫生间/洗手台", defaultExpiryDays = 180, defaultPrice = 88.0, defaultTags = listOf("温和不紧绷", "敏感肌可用", "深层清洁"))
    )

    val MATERNAL_AND_BABY: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("花王妙而舒婴儿纸尿裤", 1.9, "包", "婴儿房/尿布储物柜", defaultPrice = 118.0, defaultTags = listOf("瞬吸透气", "弱酸表层", "防侧漏")),
        LocalHeuristicEngine.CategoryTermWeight("爱他美卓萃婴儿配方奶粉3段", 1.9, "罐", "餐边柜/冲奶台", defaultExpiryDays = 30, defaultPrice = 298.0, defaultTags = listOf("天然乳脂", "益生元配方", "易吸收")),
        LocalHeuristicEngine.CategoryTermWeight("贝亲PPSU宽口径婴儿奶瓶", 1.9, "个", "厨房/奶瓶消毒柜", defaultPrice = 89.0, defaultTags = listOf("防胀气", "耐高温消毒", "母乳实感奶嘴")),
        LocalHeuristicEngine.CategoryTermWeight("美德乐双边电动吸奶器", 2.0, "台", "主卧/母婴抽屉", defaultPrice = 980.0, defaultTags = listOf("双韵律吸乳", "静音便携", "促乳高效")),
        LocalHeuristicEngine.CategoryTermWeight("婴儿手口专用纯水湿巾", 1.8, "包", "婴儿房/抽屉", defaultExpiryDays = 180, defaultPrice = 28.0, defaultTags = listOf("EDI纯水", "无酒精香精", "加厚珍珠纹")),
        LocalHeuristicEngine.CategoryTermWeight("乐高LEGO经典创意积木颗粒盒", 1.9, "盒", "儿童房/积木玩具柜", defaultPrice = 320.0, defaultTags = listOf("益智拼插", "环保ABS", "发挥空间想象力")),
        LocalHeuristicEngine.CategoryTermWeight("蒙特梭利早教启智木质玩具", 1.8, "套", "儿童房/玩具箱", defaultPrice = 145.0, defaultTags = listOf("精细动作训练", "几何形状配对", "天然木材"))
    )

    val PET_SUPPLIES: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("渴望无谷六种鱼全价猫粮", 1.9, "袋", "阳台/宠物储粮桶", defaultExpiryDays = 90, defaultPrice = 420.0, defaultTags = listOf("高肉高蛋白", "美毛护肤", "无谷低敏")),
        LocalHeuristicEngine.CategoryTermWeight("伯纳天纯全价成年犬狗粮", 1.8, "袋", "阳台/宠物储粮桶", defaultExpiryDays = 90, defaultPrice = 260.0, defaultTags = listOf("天然粮", "强健骨骼", "适口性佳")),
        LocalHeuristicEngine.CategoryTermWeight("豆腐膨润土混合结团猫砂", 1.8, "袋", "阳台/猫砂盆专区", defaultPrice = 45.0, defaultTags = listOf("除臭无尘", "秒吸结团", "可冲马桶")),
        LocalHeuristicEngine.CategoryTermWeight("猫咪主食肉泥纯肉罐头", 1.8, "罐", "阳台/宠物零食柜", defaultExpiryDays = 180, defaultPrice = 15.0, defaultTags = listOf("98%纯肉", "补水增肥", "无诱食剂")),
        LocalHeuristicEngine.CategoryTermWeight("耐磨瓦楞纸猫抓板", 1.7, "个", "客厅/宠物角", defaultPrice = 28.0, defaultTags = listOf("磨爪解压", "不掉屑蜂窝结构")),
        LocalHeuristicEngine.CategoryTermWeight("反光防爆冲狗胸背牵引绳", 1.8, "条", "玄关/挂钩", defaultPrice = 65.0, defaultTags = listOf("减震防拉伤", "夜间安全反光"))
    )

    val OUTDOOR_CAMPING: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("挪客Naturehike双人防暴雨帐篷", 1.9, "顶", "阳台/露营储物箱", defaultPrice = 480.0, defaultTags = listOf("铝合金支架", "PU3000mm防水", "轻量透气")),
        LocalHeuristicEngine.CategoryTermWeight("黑胶加厚防晒露营天幕", 1.9, "顶", "阳台/露营储物箱", defaultPrice = 280.0, defaultTags = listOf("UPF50+", "八角大面积", "野餐遮阳")),
        LocalHeuristicEngine.CategoryTermWeight("户外便携折叠克米特椅", 1.8, "把", "后备箱/露营箱", defaultPrice = 120.0, defaultTags = listOf("实木/铝合金", "秒折叠收纳", "舒适靠背")),
        LocalHeuristicEngine.CategoryTermWeight("白鸭绒户外抗寒木乃伊睡袋", 1.9, "个", "储物间/防潮袋", defaultPrice = 380.0, defaultTags = listOf("温标-10度", "800蓬松度", "轻便保暖")),
        LocalHeuristicEngine.CategoryTermWeight("卡式炉便携防风气炉", 1.8, "台", "露营装备箱", defaultPrice = 99.0, defaultTags = listOf("磁吸防爆过压切断", "3.3KW大火力", "户外野炊")),
        LocalHeuristicEngine.CategoryTermWeight("户外大容量移动储能电源", 2.0, "台", "储物间/露营箱", defaultPrice = 2499.0, defaultTags = listOf("1000Wh", "220V正弦波输出", "露营用电自驾游")),
        LocalHeuristicEngine.CategoryTermWeight("碳素超轻路亚鱼竿", 1.8, "支", "后备箱/渔具包", defaultPrice = 260.0, defaultTags = listOf("高碳竿身", "快速回弹", "配纺车轮"))
    )

    val SPORTS_AND_FITNESS: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("环保可调节电镀哑铃套装", 1.9, "副", "阳台/力量健身区", defaultPrice = 180.0, defaultTags = listOf("20kg/30kg", "快速组合拧紧", "自由力量训练")),
        LocalHeuristicEngine.CategoryTermWeight("天然橡胶防滑加厚瑜伽垫", 1.8, "张", "阳台/健身收纳桶", defaultPrice = 128.0, defaultTags = listOf("TPE/PU", "对齐正位线", "静音减震")),
        LocalHeuristicEngine.CategoryTermWeight("专业深层肌肉放松筋膜枪", 1.9, "个", "卧室/床头柜", defaultPrice = 350.0, defaultTags = listOf("无刷电机", "大扭矩高冲程", "多款按摩头")),
        LocalHeuristicEngine.CategoryTermWeight("ON乳清蛋白粉桶装", 1.9, "桶", "餐边柜/运动营养角", defaultExpiryDays = 180, defaultPrice = 420.0, defaultTags = listOf("分离乳清", "24g蛋白/份", "增肌塑形")),
        LocalHeuristicEngine.CategoryTermWeight("尤尼克斯YONEX全碳素羽毛球拍", 1.9, "支", "玄关/运动拍包", defaultPrice = 580.0, defaultTags = listOf("天斧/弓箭", "高弹性中杆", "进攻控球")),
        LocalHeuristicEngine.CategoryTermWeight("斯伯丁SPALDING室内外耐磨篮球", 1.8, "个", "玄关/运动架", defaultPrice = 160.0, defaultTags = listOf("PU吸湿皮质", "7号标准球", "耐打抓握感好"))
    )

    val INSTRUMENTS_AND_AUDIO: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("雅马哈YAMAHA民谣全单吉他", 1.9, "把", "客厅/乐器立架", defaultPrice = 2400.0, defaultTags = listOf("云杉面单", "通透饱满音色", "原声共鸣")),
        LocalHeuristicEngine.CategoryTermWeight("罗兰Roland 88键重锤电钢琴", 2.0, "台", "书房/音乐角", defaultPrice = 4600.0, defaultTags = listOf("PHA-4标准键盘", "真实琴槌手感", "蓝牙音频连接")),
        LocalHeuristicEngine.CategoryTermWeight("铁三角AT2020专业电容麦克风", 1.9, "支", "工作台/录音架", defaultPrice = 690.0, defaultTags = listOf("心形指向", "人声录音直播", "XLR专业接口")),
        LocalHeuristicEngine.CategoryTermWeight("Focusrite福克斯特双通道声卡", 1.9, "台", "工作台/音频机架", defaultPrice = 980.0, defaultTags = listOf("Scarlett 2i2", "高动态话放", "低延迟监听")),
        LocalHeuristicEngine.CategoryTermWeight("铁三角黑胶唱片机", 1.9, "台", "客厅/音响边几", defaultPrice = 1200.0, defaultTags = listOf("动磁唱头", "全自动皮带驱动", "高保真黑胶体验"))
    )

    val AUTOMOTIVE_CARE: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("高浓缩四季防冻玻璃水", 1.7, "瓶", "汽车后备箱/车库", defaultExpiryDays = 365, defaultPrice = 15.0, defaultTags = listOf("-25度防冻", "去油膜树胶", "视野清晰")),
        LocalHeuristicEngine.CategoryTermWeight("车载数显预设胎压充气泵", 1.8, "台", "汽车后备箱", defaultPrice = 148.0, defaultTags = listOf("满电自动充停", "LED应急照明", "快速补气")),
        LocalHeuristicEngine.CategoryTermWeight("汽车应急启动搭电电源", 1.9, "台", "汽车后备箱", defaultPrice = 220.0, defaultTags = listOf("大倍率启动电流", "防反接夹", "电瓶亏电打火")),
        LocalHeuristicEngine.CategoryTermWeight("4K高清夜视行车记录仪", 1.9, "台", "车内后视镜区", defaultPrice = 399.0, defaultTags = listOf("索尼传感器", "前后双录", "24小时停车监控")),
        LocalHeuristicEngine.CategoryTermWeight("长效有机冷却防冻液", 1.8, "桶", "车库/汽车养护区", defaultExpiryDays = 730, defaultPrice = 75.0, defaultTags = listOf("乙二醇型", "防沸防腐蚀", "发动机冷却"))
    )

    val MODEL_COLLECTIBLES: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("万代BANDAI RG/MG高达拼装模型", 1.9, "盒", "书房/模型收藏柜", defaultPrice = 280.0, defaultTags = listOf("GUNPLA", "可动骨架", "分色精细", "机械美学")),
        LocalHeuristicEngine.CategoryTermWeight("正版手办二次元PVC完成品", 1.9, "个", "书房/亚克力防尘展柜", defaultPrice = 680.0, defaultTags = listOf("GoodSmile/Alter", "涂装精致", "收藏级")),
        LocalHeuristicEngine.CategoryTermWeight("POP MART泡泡玛特盲盒潮玩", 1.8, "个", "客厅/潮玩阶梯架", defaultPrice = 69.0, defaultTags = listOf("MOLLY/Labubu/Dimoo", "艺术潮玩", "盲盒收藏")),
        LocalHeuristicEngine.CategoryTermWeight("郡士Mr.Hobby模型油性硝基漆", 1.8, "瓶", "工作室/上色排漆架", defaultPrice = 16.0, defaultTags = listOf("发色鲜艳", "附着力强", "喷笔专用")),
        LocalHeuristicEngine.CategoryTermWeight("带LED顶灯磁吸亚克力展示盒", 1.8, "个", "书房/防尘柜", defaultPrice = 65.0, defaultTags = listOf("高透防尘", "可无限叠加", "氛围灯效"))
    )

    val HOME_APPLIANCES: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("空气炸锅可视智能触控", 1.8, "台", "厨房/操作台", defaultPrice = 269.0, defaultTags = listOf("无油低脂烘烤", "大容量5L", "一键菜单")),
        LocalHeuristicEngine.CategoryTermWeight("低音破壁料理机", 1.8, "台", "厨房/操作台", defaultPrice = 399.0, defaultTags = listOf("免滤豆浆", "精钢8叶刀", "隔音罩降噪")),
        LocalHeuristicEngine.CategoryTermWeight("半自动意式浓缩咖啡机", 1.9, "台", "水吧台/咖啡角", defaultPrice = 1880.0, defaultTags = listOf("20Bar高压萃取", "蒸汽强劲打奶泡", "全金属机身")),
        LocalHeuristicEngine.CategoryTermWeight("恒温养生壶加厚高硼硅", 1.7, "台", "书房/水吧台", defaultPrice = 129.0, defaultTags = listOf("花草茶煲汤", "精细温控", "预约保温")),
        LocalHeuristicEngine.CategoryTermWeight("强效压缩机抽湿除湿机", 1.9, "台", "卧室/阳台", defaultPrice = 880.0, defaultTags = listOf("每日20L大除湿量", "干衣防霉", "万向轮移动")),
        LocalHeuristicEngine.CategoryTermWeight("无雾冷蒸发纯净加湿器", 1.8, "台", "卧室/床头柜", defaultPrice = 280.0, defaultTags = listOf("抑菌无白粉", "大水箱上加水", "轻音睡眠")),
        LocalHeuristicEngine.CategoryTermWeight("除甲醛空气净化器", 1.9, "台", "客厅/转角处", defaultPrice = 1499.0, defaultTags = listOf("H13级HEPA滤网", "高CADR值", "甲醛数显传感器"))
    )

    val GARDENING_AND_PLANTS: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("长嘴不锈钢园艺浇水壶", 1.7, "个", "阳台/植物花架", defaultPrice = 35.0, defaultTags = listOf("精准根部给水", "极简北欧风")),
        LocalHeuristicEngine.CategoryTermWeight("加厚青山控根透气花盆", 1.7, "个", "阳台/植物花架", defaultPrice = 15.0, defaultTags = listOf("侧面开槽", "防止烂根绕根", "优质PP材质")),
        LocalHeuristicEngine.CategoryTermWeight("进口发酵泥炭营养土", 1.8, "袋", "阳台/园艺储物箱", defaultPrice = 38.0, defaultTags = listOf("配比珍珠岩蛭石", "疏松透气保水", "植物上盆")),
        LocalHeuristicEngine.CategoryTermWeight("美乐棵通用型植物营养液/缓释肥", 1.8, "瓶", "阳台/园艺药肥柜", defaultExpiryDays = 730, defaultPrice = 32.0, defaultTags = listOf("氮磷钾平衡", "促根壮苗", "滴灌通用")),
        LocalHeuristicEngine.CategoryTermWeight("SK5高碳钢省力修枝剪", 1.8, "把", "阳台/园艺工具架", defaultPrice = 45.0, defaultTags = listOf("锋利防滑", "果树花卉造型剪修"))
    )

    val VITAL_DOCUMENTS: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("中华人民共和国居民身份证", 2.0, "张", "主卧/保险箱/文件袋", defaultPrice = 0.0, defaultTags = listOf("核心身份证明", "芯片防伪", "绝密证件")),
        LocalHeuristicEngine.CategoryTermWeight("因私普通护照与港澳通行证", 2.0, "本", "主卧/保险箱/出国文件袋", defaultPrice = 0.0, defaultTags = listOf("出境签证", "身份凭证")),
        LocalHeuristicEngine.CategoryTermWeight("家庭居民户口簿", 2.0, "本", "主卧/保险箱/核心文件袋", defaultPrice = 0.0, defaultTags = listOf("户籍档案", "官方证明")),
        LocalHeuristicEngine.CategoryTermWeight("不动产权证书/房屋产权证", 2.0, "本", "主卧/防火保险箱", defaultPrice = 0.0, defaultTags = listOf("房屋资产", "不动产确权凭证")),
        LocalHeuristicEngine.CategoryTermWeight("机动车驾驶证与行驶证", 1.9, "本", "随身包袋/玄关钥匙架", defaultPrice = 0.0, defaultTags = listOf("驾驶资格", "车辆准驾档案")),
        LocalHeuristicEngine.CategoryTermWeight("商业合同与房屋租赁协议", 1.9, "份", "书房/档案收纳盒", defaultPrice = 0.0, defaultTags = listOf("签字盖章", "法律维权凭据")),
        LocalHeuristicEngine.CategoryTermWeight("银行储蓄卡与存折", 1.8, "张", "主卧/保险箱卡包", defaultPrice = 0.0, defaultTags = listOf("金融账户", "资金凭据"))
    )

    val INDUSTRIAL_SAFETY_PPE: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        LocalHeuristicEngine.CategoryTermWeight("3M防尘防颗粒物防毒半面罩", 1.9, "套", "工作室/防护劳保架", defaultPrice = 120.0, defaultTags = listOf("6200面具", "配6001滤毒盒", "焊接喷漆防有害气体")),
        LocalHeuristicEngine.CategoryTermWeight("防飞溅防雾护目镜", 1.8, "副", "工作台/个人防护区", defaultPrice = 25.0, defaultTags = listOf("聚碳酸酯PC防冲击", "打磨雕刻防护")),
        LocalHeuristicEngine.CategoryTermWeight("丁腈耐油防静电劳保手套", 1.8, "盒", "工作室/劳保抽屉", defaultPrice = 35.0, defaultTags = listOf("100只装", "防油污耐酸碱", "操作灵活")),
        LocalHeuristicEngine.CategoryTermWeight("高分贝隔音降噪耳罩", 1.8, "副", "工作室/重型加工区", defaultPrice = 65.0, defaultTags = listOf("NRR 30dB", "切割雕刻机降噪防护"))
    )

    /**
     * 汇集所有全领域专业词典，生成商业级加权矩阵
     */
    fun getAllCategoryWeightMatrix(): Map<String, List<LocalHeuristicEngine.CategoryTermWeight>> {
        return mapOf(
            "电子元器件" to LocalDiyComponentDatabase.DIY_ELECTRONIC_COMPONENTS,
            "工具五金" to LocalDiyComponentDatabase.DIY_MAKER_TOOLS,
            "电子数码" to CONSUMER_DIGITAL,
            "药品个护" to MEDICAL_AND_CARE,
            "食品饮料" to FOOD_AND_BEVERAGE,
            "美妆护肤" to BEAUTY_AND_SKINCARE,
            "母婴亲子" to MATERNAL_AND_BABY,
            "宠物用品" to PET_SUPPLIES,
            "户外露营" to OUTDOOR_CAMPING,
            "运动健身" to SPORTS_AND_FITNESS,
            "乐器影音" to INSTRUMENTS_AND_AUDIO,
            "汽车用品" to AUTOMOTIVE_CARE,
            "模型收藏" to MODEL_COLLECTIBLES,
            "家用电器" to HOME_APPLIANCES,
            "家居园艺" to GARDENING_AND_PLANTS,
            "重要证件" to VITAL_DOCUMENTS,
            "工业劳保" to INDUSTRIAL_SAFETY_PPE
        )
    }
}
