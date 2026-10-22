package com.example.ai

/**
 * 工业级/创客级电子元器件与 DIY 硬件专业知识库 (Commercial DIY & Hardware Database)
 * 涵盖：阻容感、半导体管件、集成芯片IC、单片机/SoC、传感器、射频物联网、
 * 电源管理、原型线材、焊接装配仪器、3D打印与数控工具、紧固五金等。
 */
object LocalDiyComponentDatabase {

    val DIY_ELECTRONIC_COMPONENTS: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        // === 1. 被动元件：电阻、电位器、敏感电阻 ===
        LocalHeuristicEngine.CategoryTermWeight("0805电阻", 1.8, "盘", "工作室/贴片元件柜", defaultPrice = 15.0, defaultTags = listOf("0805", "贴片电阻", "被动元件", "阻容包")),
        LocalHeuristicEngine.CategoryTermWeight("0603电阻", 1.8, "盘", "工作室/贴片元件柜", defaultPrice = 18.0, defaultTags = listOf("0603", "高密贴片", "电阻", "SMD")),
        LocalHeuristicEngine.CategoryTermWeight("1206电阻", 1.7, "盘", "工作室/贴片元件柜", defaultPrice = 20.0, defaultTags = listOf("1206", "大功率贴片", "电阻")),
        LocalHeuristicEngine.CategoryTermWeight("金属膜电阻", 1.6, "包", "工作室/直插零件盒", defaultPrice = 9.9, defaultTags = listOf("1/4W", "五色环", "1%精度", "直插电阻")),
        LocalHeuristicEngine.CategoryTermWeight("碳膜电阻", 1.5, "包", "工作室/直插零件盒", defaultPrice = 6.0, defaultTags = listOf("碳膜", "1/8W", "直插")),
        LocalHeuristicEngine.CategoryTermWeight("水泥电阻", 1.6, "只", "工作室/功率元件箱", defaultPrice = 3.5, defaultTags = listOf("大功率", "5W/10W", "陶瓷水泥")),
        LocalHeuristicEngine.CategoryTermWeight("采样电阻", 1.7, "只", "工作室/精密元件盒", defaultPrice = 8.0, defaultTags = listOf("毫欧级", "康铜丝", "电流采样", "高精度")),
        LocalHeuristicEngine.CategoryTermWeight("贴片排阻", 1.6, "条", "工作室/贴片元件柜", defaultPrice = 12.0, defaultTags = listOf("0804", "8P4R", "上拉排阻")),
        LocalHeuristicEngine.CategoryTermWeight("电位器", 1.6, "个", "工作室/电位器分类盒", defaultPrice = 2.5, defaultTags = listOf("可调电阻", "WH148", "音量旋钮")),
        LocalHeuristicEngine.CategoryTermWeight("多圈精密电位器", 1.7, "个", "工作室/电位器分类盒", defaultPrice = 4.5, defaultTags = listOf("3296W", "顶调/侧调", "精密微调")),
        LocalHeuristicEngine.CategoryTermWeight("热敏电阻", 1.6, "包", "工作室/传感器盒", defaultPrice = 5.0, defaultTags = listOf("NTC 10K", "PT100", "温度采集")),
        LocalHeuristicEngine.CategoryTermWeight("光敏电阻", 1.6, "包", "工作室/传感器盒", defaultPrice = 5.0, defaultTags = listOf("LDR 5528", "光控感应")),
        LocalHeuristicEngine.CategoryTermWeight("压敏电阻", 1.6, "包", "工作室/防雷保护盒", defaultPrice = 8.0, defaultTags = listOf("14D471K", "过压保护", "浪涌吸收")),
        LocalHeuristicEngine.CategoryTermWeight("自恢复保险丝", 1.7, "包", "工作室/保护器件盒", defaultPrice = 10.0, defaultTags = listOf("PPTC", "过流保护", "SMD1812")),

        // === 2. 被动元件：电容、超级电容 ===
        LocalHeuristicEngine.CategoryTermWeight("0805电容", 1.8, "盘", "工作室/贴片元件柜", defaultPrice = 22.0, defaultTags = listOf("MLCC", "0805", "贴片陶瓷电容", "104/105")),
        LocalHeuristicEngine.CategoryTermWeight("0603电容", 1.8, "盘", "工作室/贴片元件柜", defaultPrice = 25.0, defaultTags = listOf("MLCC", "0603", "滤波电容")),
        LocalHeuristicEngine.CategoryTermWeight("铝电解电容", 1.7, "盒", "工作室/电解电容箱", defaultPrice = 28.0, defaultTags = listOf("直插电解", "耐压25V/50V", "100uF/470uF/1000uF")),
        LocalHeuristicEngine.CategoryTermWeight("固态电容", 1.7, "盒", "工作室/电解电容箱", defaultPrice = 45.0, defaultTags = listOf("低ESR", "主板供电", "高频滤波")),
        LocalHeuristicEngine.CategoryTermWeight("贴片钽电容", 1.7, "条", "工作室/贴片元件柜", defaultPrice = 35.0, defaultTags = listOf("AVX", "A/B/C/D型", "高稳定滤波")),
        LocalHeuristicEngine.CategoryTermWeight("独石电容", 1.6, "包", "工作室/直插零件盒", defaultPrice = 8.0, defaultTags = listOf("直插瓷片", "104独石", "高频旁路")),
        LocalHeuristicEngine.CategoryTermWeight("CBB金属薄膜电容", 1.7, "盒", "工作室/交流滤波盒", defaultPrice = 18.0, defaultTags = listOf("CBB22", "降压电容", "高压400V/630V")),
        LocalHeuristicEngine.CategoryTermWeight("安规X2电容", 1.7, "盒", "工作室/交流滤波盒", defaultPrice = 15.0, defaultTags = listOf("275VAC", "EMI抗干扰", "安规认证")),
        LocalHeuristicEngine.CategoryTermWeight("超级法拉电容", 1.8, "只", "工作室/储能器件盒", defaultPrice = 30.0, defaultTags = listOf("5.5V/2.7V", "1F/10F/100F", "后备掉电保持")),

        // === 3. 被动元件：电感、变压器、磁珠 ===
        LocalHeuristicEngine.CategoryTermWeight("贴片功率电感", 1.8, "盘", "工作室/贴片元件柜", defaultPrice = 28.0, defaultTags = listOf("一体成型", "CD54/CD75/NR4030", "DC-DC滤波")),
        LocalHeuristicEngine.CategoryTermWeight("工字电感", 1.6, "盒", "工作室/电感箱", defaultPrice = 12.0, defaultTags = listOf("直插工字", "大电流", "滤波扼流")),
        LocalHeuristicEngine.CategoryTermWeight("贴片磁珠", 1.7, "盘", "工作室/贴片元件柜", defaultPrice = 15.0, defaultTags = listOf("0805/0603", "EMI抑制", "电源净化")),
        LocalHeuristicEngine.CategoryTermWeight("共模扼流圈", 1.7, "只", "工作室/交流滤波盒", defaultPrice = 6.0, defaultTags = listOf("共模电感", "开关电源滤波")),
        LocalHeuristicEngine.CategoryTermWeight("高频变压器", 1.7, "只", "工作室/电源DIY区", defaultPrice = 12.0, defaultTags = listOf("EE16/EE25", "反激式", "开关电源变压器")),

        // === 4. 分立半导体：二极管、整流桥、晶体管、MOSFET ===
        LocalHeuristicEngine.CategoryTermWeight("肖特基二极管", 1.8, "条", "工作室/半导体元件盒", defaultPrice = 12.0, defaultTags = listOf("SS34", "SS14", "1N5819", "低压降防反接")),
        LocalHeuristicEngine.CategoryTermWeight("整流二极管", 1.6, "包", "工作室/半导体元件盒", defaultPrice = 8.0, defaultTags = listOf("1N4007", "M7", "交流整流")),
        LocalHeuristicEngine.CategoryTermWeight("开关二极管", 1.7, "条", "工作室/半导体元件盒", defaultPrice = 6.0, defaultTags = listOf("1N4148", "1N4148WS", "高速开关")),
        LocalHeuristicEngine.CategoryTermWeight("稳压二极管", 1.6, "盒", "工作室/半导体元件盒", defaultPrice = 15.0, defaultTags = listOf("齐纳二极管", "3.3V/5.1V/12V", "基准稳压")),
        LocalHeuristicEngine.CategoryTermWeight("TVS瞬态抑制二极管", 1.8, "包", "工作室/半导体元件盒", defaultPrice = 18.0, defaultTags = listOf("SMAJ5.0A", "SMBJ12A", "静电防浪涌")),
        LocalHeuristicEngine.CategoryTermWeight("贴片发光二极管", 1.7, "盘", "工作室/光电器件盒", defaultPrice = 12.0, defaultTags = listOf("0805 LED", "红绿蓝黄白", "状态指示")),
        LocalHeuristicEngine.CategoryTermWeight("WS2812B幻彩灯珠", 1.9, "盘", "工作室/光电器件盒", defaultPrice = 35.0, defaultTags = listOf("RGB灯珠", "5050内置IC", "单线级联", "NeoPixel")),
        LocalHeuristicEngine.CategoryTermWeight("整流桥堆", 1.7, "条", "工作室/电源器件箱", defaultPrice = 15.0, defaultTags = listOf("MB6S", "DB107", "KBP206", "全桥整流")),
        LocalHeuristicEngine.CategoryTermWeight("小信号三极管", 1.7, "盒", "工作室/半导体元件盒", defaultPrice = 15.0, defaultTags = listOf("S8050", "S8550", "2N2222", "2N3904", "NPN/PNP")),
        LocalHeuristicEngine.CategoryTermWeight("贴片MOS管", 1.8, "条", "工作室/半导体元件盒", defaultPrice = 20.0, defaultTags = listOf("SOT-23", "AO3400", "AO3401", "Si2302", "N/P沟道")),
        LocalHeuristicEngine.CategoryTermWeight("大功率功率MOS管", 1.8, "盒", "工作室/半导体元件盒", defaultPrice = 35.0, defaultTags = listOf("TO-220", "IRFZ44N", "IRF540N", "IRF3205", "低导通内阻")),
        LocalHeuristicEngine.CategoryTermWeight("可控硅晶闸管", 1.6, "盒", "工作室/交流控制箱", defaultPrice = 20.0, defaultTags = listOf("BT136", "BTA16", "双向可控硅", "调光调速")),
        LocalHeuristicEngine.CategoryTermWeight("光电耦合器", 1.7, "管", "工作室/隔离器件盒", defaultPrice = 15.0, defaultTags = listOf("PC817", "EL817", "6N137", "光耦隔离")),

        // === 5. 集成电路IC：电源、运放、逻辑、通信、存储 ===
        LocalHeuristicEngine.CategoryTermWeight("AMS1117稳压芯片", 1.9, "条", "工作室/IC防静电柜", defaultPrice = 12.0, defaultTags = listOf("SOT-223", "AMS1117-3.3", "AMS1117-5.0", "LDO降压")),
        LocalHeuristicEngine.CategoryTermWeight("LM7805稳压管", 1.7, "盒", "工作室/IC防静电柜", defaultPrice = 15.0, defaultTags = listOf("TO-220", "7805", "7812", "线性稳压器")),
        LocalHeuristicEngine.CategoryTermWeight("TP4056锂电充电IC", 1.9, "条", "工作室/IC防静电柜", defaultPrice = 18.0, defaultTags = listOf("SOP8", "TP4056", "1A线性充电", "带温控保护")),
        LocalHeuristicEngine.CategoryTermWeight("DC-DC降压芯片", 1.8, "条", "工作室/IC防静电柜", defaultPrice = 25.0, defaultTags = listOf("MP2307", "XL4015", "LM2596S", "同步整流高效率")),
        LocalHeuristicEngine.CategoryTermWeight("升压转换芯片", 1.8, "条", "工作室/IC防静电柜", defaultPrice = 22.0, defaultTags = listOf("MT3608", "SX1308", "微型高效升压")),
        LocalHeuristicEngine.CategoryTermWeight("NE555定时器芯片", 1.8, "盒", "工作室/IC防静电柜", defaultPrice = 10.0, defaultTags = listOf("DIP8/SOP8", "经典时基电路", "方波发生器")),
        LocalHeuristicEngine.CategoryTermWeight("LM358双运算放大器", 1.8, "盒", "工作室/IC防静电柜", defaultPrice = 12.0, defaultTags = listOf("DIP8/SOP8", "通用单电源运放", "信号放大")),
        LocalHeuristicEngine.CategoryTermWeight("NE5532发烧音频运放", 1.8, "盒", "工作室/IC防静电柜", defaultPrice = 25.0, defaultTags = listOf("低噪双运放", "大S发烧运放", "前级放大")),
        LocalHeuristicEngine.CategoryTermWeight("74HC595移位寄存器", 1.9, "盒", "工作室/IC防静电柜", defaultPrice = 15.0, defaultTags = listOf("8位串转并", "IO扩展芯片", "数码管点阵驱动")),
        LocalHeuristicEngine.CategoryTermWeight("74HC138译码器", 1.7, "盒", "工作室/IC防静电柜", defaultPrice = 15.0, defaultTags = listOf("3-8线译码器", "地址片选")),
        LocalHeuristicEngine.CategoryTermWeight("CH340G串口芯片", 1.9, "条", "工作室/IC防静电柜", defaultPrice = 20.0, defaultTags = listOf("SOP16", "USB转TTL", "单片机烧录")),
        LocalHeuristicEngine.CategoryTermWeight("CP2102串口芯片", 1.8, "包", "工作室/IC防静电柜", defaultPrice = 35.0, defaultTags = listOf("QFN28", "Silicon Labs", "高速免晶振串口")),
        LocalHeuristicEngine.CategoryTermWeight("MAX485通信芯片", 1.8, "盒", "工作室/IC防静电柜", defaultPrice = 12.0, defaultTags = listOf("RS485差分总线", "工业抗干扰通信")),
        LocalHeuristicEngine.CategoryTermWeight("TJA1050控制器局域网收发器", 1.8, "盒", "工作室/IC防静电柜", defaultPrice = 18.0, defaultTags = listOf("CAN总线收发器", "车载工业通信")),
        LocalHeuristicEngine.CategoryTermWeight("ADS1115高精ADC芯片", 1.9, "片", "工作室/IC防静电柜", defaultPrice = 28.0, defaultTags = listOf("16位模数转换", "I2C接口", "4通道差分")),
        LocalHeuristicEngine.CategoryTermWeight("HX711称重传感器专用ADC", 1.8, "片", "工作室/IC防静电柜", defaultPrice = 8.0, defaultTags = listOf("24位高精ADC", "双通道", "电子秤专用")),
        LocalHeuristicEngine.CategoryTermWeight("W25Q64 SPI Flash闪存", 1.8, "管", "工作室/IC防静电柜", defaultPrice = 15.0, defaultTags = listOf("8MB", "SOP8", "固件字库存储")),
        LocalHeuristicEngine.CategoryTermWeight("AT24C02 EEPROM芯片", 1.7, "盒", "工作室/IC防静电柜", defaultPrice = 10.0, defaultTags = listOf("I2C非易失存储", "配置掉电保存")),
        LocalHeuristicEngine.CategoryTermWeight("DS3231高精度实时时钟", 1.9, "片", "工作室/IC防静电柜", defaultPrice = 18.0, defaultTags = listOf("内置温补晶振", "RTC时钟", "年漂移极小")),

        // === 6. 单片机、开发板与嵌入式核心板 ===
        LocalHeuristicEngine.CategoryTermWeight("STM32F103C8T6最小系统板", 2.0, "块", "工作室/单片机核心板柜", defaultPrice = 15.0, defaultTags = listOf("BluePill", "ARM Cortex-M3", "64KB Flash", "嵌入式")),
        LocalHeuristicEngine.CategoryTermWeight("STM32F401/F411黑丸开发板", 2.0, "块", "工作室/单片机核心板柜", defaultPrice = 28.0, defaultTags = listOf("BlackPill", "Cortex-M4", "带FPU浮点", "USB-C")),
        LocalHeuristicEngine.CategoryTermWeight("STM32H743高性能开发板", 2.0, "块", "工作室/单片机核心板柜", defaultPrice = 180.0, defaultTags = listOf("480MHz超强双核", "H7工业控制", "高清屏驱动")),
        LocalHeuristicEngine.CategoryTermWeight("ESP32-S3开发板", 2.0, "块", "工作室/物联网模组柜", defaultPrice = 26.0, defaultTags = listOf("双核LX7", "WiFi蓝牙5", "支持AI矢量", "双Type-C")),
        LocalHeuristicEngine.CategoryTermWeight("ESP32-C3极简开发板", 1.9, "块", "工作室/物联网模组柜", defaultPrice = 12.0, defaultTags = listOf("RISC-V架构", "低功耗IoT", "WiFi4+BLE5")),
        LocalHeuristicEngine.CategoryTermWeight("ESP32-CAM摄像头模组", 1.9, "套", "工作室/物联网模组柜", defaultPrice = 32.0, defaultTags = listOf("OV2640摄像头", "TF卡槽", "图传监控", "面部识别")),
        LocalHeuristicEngine.CategoryTermWeight("ESP8266 NodeMCU开发板", 1.8, "块", "工作室/物联网模组柜", defaultPrice = 13.0, defaultTags = listOf("CH340", "CP2102", "经典WiFi开发板", "Blinker接入")),
        LocalHeuristicEngine.CategoryTermWeight("树莓派5 Raspberry Pi 5", 2.0, "台", "工作室/单板微电脑柜", defaultPrice = 580.0, defaultTags = listOf("8GB RAM", "PCIe接口", "4K双显", "边缘计算")),
        LocalHeuristicEngine.CategoryTermWeight("树莓派4B Raspberry Pi 4B", 1.9, "台", "工作室/单板微电脑柜", defaultPrice = 380.0, defaultTags = listOf("4GB/8GB", "千兆网口", "双MicroHDMI")),
        LocalHeuristicEngine.CategoryTermWeight("树莓派Zero 2W", 1.9, "块", "工作室/单板微电脑柜", defaultPrice = 120.0, defaultTags = listOf("微型Linux卡片机", "四核A53", "无线轻薄")),
        LocalHeuristicEngine.CategoryTermWeight("Raspberry Pi Pico微控制器板", 1.9, "块", "工作室/单片机核心板柜", defaultPrice = 22.0, defaultTags = listOf("RP2040双核", "可编程PIO", "MicroPython/C++")),
        LocalHeuristicEngine.CategoryTermWeight("Arduino Uno R3开发板", 1.9, "块", "工作室/开源硬件展架", defaultPrice = 25.0, defaultTags = listOf("ATmega328P", "经典创客入门板", "DIP可换芯片")),
        LocalHeuristicEngine.CategoryTermWeight("Arduino Nano开发板", 1.9, "块", "工作室/开源硬件展架", defaultPrice = 14.0, defaultTags = listOf("面包板直插", "小体积创客神器")),
        LocalHeuristicEngine.CategoryTermWeight("Arduino Mega 2560开发板", 1.8, "块", "工作室/开源硬件展架", defaultPrice = 45.0, defaultTags = listOf("54个数字IO", "16路模拟", "3D打印主控板")),
        LocalHeuristicEngine.CategoryTermWeight("STC89C52RC 51单片机", 1.7, "片", "工作室/经典教学芯片盒", defaultPrice = 6.5, defaultTags = listOf("DIP40", "经典高校教程", "51内核")),
        LocalHeuristicEngine.CategoryTermWeight("CH32V307 RISC-V单片机开发板", 1.8, "块", "工作室/开源硬件展架", defaultPrice = 45.0, defaultTags = listOf("沁恒国产RISC-V", "以太网PHY", "高速USB")),

        // === 7. 传感器与外设输入模组 ===
        LocalHeuristicEngine.CategoryTermWeight("DHT11温湿度传感器", 1.7, "个", "工作室/传感器模组柜", defaultPrice = 6.0, defaultTags = listOf("单总线", "温湿度测量", "入门常用")),
        LocalHeuristicEngine.CategoryTermWeight("DHT22/AM2302高精温湿度", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 18.0, defaultTags = listOf("高灵敏", "宽量程温湿度")),
        LocalHeuristicEngine.CategoryTermWeight("BME280温湿度气压三合一", 1.9, "个", "工作室/传感器模组柜", defaultPrice = 22.0, defaultTags = listOf("Bosch博世", "I2C/SPI", "海拔气压测量")),
        LocalHeuristicEngine.CategoryTermWeight("DS18B20防水温度探头", 1.8, "条", "工作室/传感器模组柜", defaultPrice = 8.5, defaultTags = listOf("不锈钢封装", "1米引线", "水下测温")),
        LocalHeuristicEngine.CategoryTermWeight("MPU6050六轴姿态传感器", 1.9, "个", "工作室/传感器模组柜", defaultPrice = 12.0, defaultTags = listOf("3轴陀螺仪+3轴加速度", "DMP滤波", "平衡车无人机")),
        LocalHeuristicEngine.CategoryTermWeight("HC-SR04超声波测距模组", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 6.5, defaultTags = listOf("2cm-400cm", "避障测距", "智能小车")),
        LocalHeuristicEngine.CategoryTermWeight("VL53L0X激光飞行时间测距", 1.9, "个", "工作室/传感器模组柜", defaultPrice = 18.0, defaultTags = listOf("ToF毫米级", "940nm激光", "高精度定距")),
        LocalHeuristicEngine.CategoryTermWeight("HC-SR501人体红外感应模组", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 6.0, defaultTags = listOf("PIR热释电", "人体活动感应", "自动延时")),
        LocalHeuristicEngine.CategoryTermWeight("RCWL-0516微波雷达感应模组", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 5.0, defaultTags = listOf("多普勒微波", "穿墙感应", "抗干扰人体感应")),
        LocalHeuristicEngine.CategoryTermWeight("MQ-2可燃气体/烟雾传感器", 1.7, "个", "工作室/传感器模组柜", defaultPrice = 9.0, defaultTags = listOf("烟雾液化气报警", "模拟输出+数字比较")),
        LocalHeuristicEngine.CategoryTermWeight("MQ-135空气质量有害气体传感器", 1.7, "个", "工作室/传感器模组柜", defaultPrice = 9.5, defaultTags = listOf("氨气硫化物", "空气净化检测")),
        LocalHeuristicEngine.CategoryTermWeight("土壤湿度检测传感器", 1.7, "套", "工作室/传感器模组柜", defaultPrice = 5.5, defaultTags = listOf("电容防腐蚀探头", "植物自动浇花")),
        LocalHeuristicEngine.CategoryTermWeight("ACS712电流互感检测模组", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 12.0, defaultTags = listOf("5A/20A/30A", "霍尔隔离电流检测")),
        LocalHeuristicEngine.CategoryTermWeight("TCS34725颜色识别传感器", 1.8, "个", "工作室/传感器模组柜", defaultPrice = 25.0, defaultTags = listOf("RGB+Clear明光", "带红外阻隔滤镜", "色彩分拣")),

        // === 8. 显示屏与交互模组 ===
        LocalHeuristicEngine.CategoryTermWeight("0.96寸I2C OLED显示屏", 1.9, "块", "工作室/显示屏收纳盒", defaultPrice = 11.0, defaultTags = listOf("SSD1306", "128x64点阵", "黄蓝双色/纯白", "4针I2C")),
        LocalHeuristicEngine.CategoryTermWeight("1.3寸SPI OLED显示屏", 1.9, "块", "工作室/显示屏收纳盒", defaultPrice = 18.0, defaultTags = listOf("SH1106", "128x64高清", "7针SPI快速刷新")),
        LocalHeuristicEngine.CategoryTermWeight("LCD1602液晶屏(带I2C转接板)", 1.8, "块", "工作室/显示屏收纳盒", defaultPrice = 14.0, defaultTags = listOf("HD44780", "PCF8574T", "蓝底白字/黄绿底", "两线控制")),
        LocalHeuristicEngine.CategoryTermWeight("1.54寸微雪墨水屏电子纸", 2.0, "块", "工作室/显示屏收纳盒", defaultPrice = 68.0, defaultTags = listOf("E-Paper", "断电保持", "双色/三色墨水屏", "极低功耗")),
        LocalHeuristicEngine.CategoryTermWeight("2.4寸TFT触摸彩屏模组", 1.9, "块", "工作室/显示屏收纳盒", defaultPrice = 38.0, defaultTags = listOf("ILI9341", "240x320", "带电阻/电容触摸", "LVGL界面开发")),
        LocalHeuristicEngine.CategoryTermWeight("TM1637四位数码管模组", 1.8, "块", "工作室/显示屏收纳盒", defaultPrice = 5.0, defaultTags = listOf("带时钟冒号", "两线驱动", "电子钟DIY")),
        LocalHeuristicEngine.CategoryTermWeight("MAX7219点阵屏模组", 1.8, "块", "工作室/显示屏收纳盒", defaultPrice = 8.5, defaultTags = listOf("8x8红光LED点阵", "可多块级联拼接")),
        LocalHeuristicEngine.CategoryTermWeight("EC11旋转编码器模组", 1.8, "个", "工作室/按键开关盒", defaultPrice = 4.5, defaultTags = listOf("带按压开关", "360度无限旋转", "数字调节")),

        // === 9. 射频通信与物联网无线模组 ===
        LocalHeuristicEngine.CategoryTermWeight("SX1278 LoRa无线扩频通信模组", 1.9, "块", "工作室/无线射频柜", defaultPrice = 28.0, defaultTags = listOf("433MHz", "远距离数公里穿透", "低功耗长续航")),
        LocalHeuristicEngine.CategoryTermWeight("NRF24L01+ 2.4G无线传输模组", 1.8, "块", "工作室/无线射频柜", defaultPrice = 6.5, defaultTags = listOf("带PA功放+天线", "遥控小车模型传输")),
        LocalHeuristicEngine.CategoryTermWeight("HC-05主从一体蓝牙串口模组", 1.8, "块", "工作室/无线射频柜", defaultPrice = 18.0, defaultTags = listOf("经典蓝牙2.0", "AT指令配对", "无线串口透传")),
        LocalHeuristicEngine.CategoryTermWeight("CC2541 BLE低功耗蓝牙模组", 1.8, "块", "工作室/无线射频柜", defaultPrice = 15.0, defaultTags = listOf("BLE 4.0", "手机App直连透传")),
        LocalHeuristicEngine.CategoryTermWeight("EC200U 4G Cat.1全网通模组", 2.0, "套", "工作室/无线射频柜", defaultPrice = 65.0, defaultTags = listOf("移远Quectel", "插SIM卡免WiFi", "MQTT物联网云上报")),
        LocalHeuristicEngine.CategoryTermWeight("ATGM336H GPS北斗双模定位模组", 1.9, "套", "工作室/无线射频柜", defaultPrice = 28.0, defaultTags = listOf("陶瓷天线", "秒级授时", "经纬度航迹记录")),
        LocalHeuristicEngine.CategoryTermWeight("RC522 RFID射频刷卡模组", 1.8, "套", "工作室/无线射频柜", defaultPrice = 12.0, defaultTags = listOf("13.56MHz", "M1卡/S50读写", "门禁卡读卡器")),
        LocalHeuristicEngine.CategoryTermWeight("PN532 NFC近场通信模组", 1.9, "块", "工作室/无线射频柜", defaultPrice = 35.0, defaultTags = listOf("支持手机NFC交互", "全协议卡片读写仿真")),

        // === 10. 驱动与动力执行机构 ===
        LocalHeuristicEngine.CategoryTermWeight("SG90 9g微型舵机", 1.8, "个", "工作室/舵机电机箱", defaultPrice = 8.0, defaultTags = listOf("塑料齿轮", "180度角度控制", "机械臂小车")),
        LocalHeuristicEngine.CategoryTermWeight("MG996R金属大扭矩舵机", 1.9, "个", "工作室/舵机电机箱", defaultPrice = 28.0, defaultTags = listOf("金属铜齿", "13kg高扭矩", "大型机械关节")),
        LocalHeuristicEngine.CategoryTermWeight("42步进电机 NEMA17", 2.0, "台", "工作室/舵机电机箱", defaultPrice = 38.0, defaultTags = listOf("1.8度步距角", "42x42mm", "3D打印机/CNC驱动")),
        LocalHeuristicEngine.CategoryTermWeight("TMC2209静音步进电机驱动", 1.9, "片", "工作室/驱动芯片盒", defaultPrice = 22.0, defaultTags = listOf("StealthChop静音", "堵转检测", "256高微步细分")),
        LocalHeuristicEngine.CategoryTermWeight("A4988步进电机驱动板", 1.7, "片", "工作室/驱动芯片盒", defaultPrice = 6.0, defaultTags = listOf("带散热片", "16细分", "RAMPS 1.4配套")),
        LocalHeuristicEngine.CategoryTermWeight("L298N双H桥直流电机驱动板", 1.8, "块", "工作室/驱动芯片盒", defaultPrice = 12.0, defaultTags = listOf("大功率带散热片", "双路直流减速电机/步进控制")),
        LocalHeuristicEngine.CategoryTermWeight("5V单路光耦隔离继电器模组", 1.8, "个", "工作室/继电器盒", defaultPrice = 4.5, defaultTags = listOf("250V 10A", "高低电平触发", "强电控制")),
        LocalHeuristicEngine.CategoryTermWeight("固态继电器 SSR-40DA", 1.9, "只", "工作室/强电控制箱", defaultPrice = 25.0, defaultTags = listOf("无触点火花", "DC控AC", "40A加热棒控制")),

        // === 11. 供电电源与锂电管理配件 ===
        LocalHeuristicEngine.CategoryTermWeight("18650动力锂电池", 1.9, "节", "工作室/防爆电池箱", defaultPrice = 15.0, defaultTags = listOf("3.7V", "2600mAh/3500mAh", "正极尖头/平头", "松下/LG/三洋")),
        LocalHeuristicEngine.CategoryTermWeight("21700动力锂电芯", 1.9, "节", "工作室/防爆电池箱", defaultPrice = 22.0, defaultTags = listOf("3.7V", "4800mAh/5000mAh", "大容量强放电")),
        LocalHeuristicEngine.CategoryTermWeight("3S锂电池BMS保护板", 1.9, "块", "工作室/电池配件盒", defaultPrice = 12.0, defaultTags = listOf("11.1V/12.6V", "过充过放过流保护", "带均衡冲电")),
        LocalHeuristicEngine.CategoryTermWeight("Type-C PD快充诱骗触发板", 1.9, "块", "工作室/供电转换盒", defaultPrice = 6.5, defaultTags = listOf("9V/12V/15V/20V", "诱骗充电宝直供大功率工具")),
        LocalHeuristicEngine.CategoryTermWeight("航模XT60黄金大电流插头", 1.8, "对", "工作室/接插件盒", defaultPrice = 3.5, defaultTags = listOf("公母对插", "耐高温阻燃", "60A大电流")),

        // === 12. 连接排线、面包板与制作辅料 ===
        LocalHeuristicEngine.CategoryTermWeight("彩色杜邦线(40P排线)", 1.7, "扎", "工作室/线材挂架", defaultPrice = 6.0, defaultTags = listOf("20cm/10cm", "公对公/公对母/母对母", "免焊接测试")),
        LocalHeuristicEngine.CategoryTermWeight("830孔大号无焊面包板", 1.8, "块", "工作台/原型搭建区", defaultPrice = 12.0, defaultTags = listOf("插拔式实验板", "双电源导轨", "电路打样验证")),
        LocalHeuristicEngine.CategoryTermWeight("玻璃纤维单面/双面洞洞板", 1.7, "包", "工作室/PCB板材箱", defaultPrice = 15.0, defaultTags = listOf("FR-4玻纤", "2.54mm间距", "镀锡万用板")),
        LocalHeuristicEngine.CategoryTermWeight("2.54mm排针排母套装", 1.7, "盒", "工作室/接插件盒", defaultPrice = 15.0, defaultTags = listOf("单排直针/弯针", "圆孔排母", "镀金抗氧化")),
        LocalHeuristicEngine.CategoryTermWeight("XH2.54端子胶壳压线插头套装", 1.8, "盒", "工作室/端子压接区", defaultPrice = 25.0, defaultTags = listOf("2P/3P/4P/5P", "接线公母端子", "传感器接头")),
        LocalHeuristicEngine.CategoryTermWeight("耐高温特软硅胶导线", 1.8, "卷", "工作室/线材挂架", defaultPrice = 20.0, defaultTags = listOf("20AWG/24AWG/28AWG", "镀锡铜多股", "耐温200度")),
        LocalHeuristicEngine.CategoryTermWeight("带胶绝缘热缩管套装", 1.7, "盒", "工作室/线材辅料箱", defaultPrice = 16.0, defaultTags = listOf("不同口径", "加热收缩绝缘防短路")),
        LocalHeuristicEngine.CategoryTermWeight("Kapton金手指高温绝缘胶带", 1.8, "卷", "工作台/焊接辅助区", defaultPrice = 12.0, defaultTags = listOf("聚酰亚胺", "耐温300度", "BGA遮盖保护")),
        LocalHeuristicEngine.CategoryTermWeight("导热硅脂", 1.7, "支", "工作室/散热辅料盒", defaultPrice = 15.0, defaultTags = listOf("信越7921", "高导热系数", "CPU/芯片散热"))
    )

    val DIY_MAKER_TOOLS: List<LocalHeuristicEngine.CategoryTermWeight> = listOf(
        // === 1. 焊接与拆焊测试设备 ===
        LocalHeuristicEngine.CategoryTermWeight("T12智能数显焊台", 2.0, "台", "工作台/焊接操作区", defaultPrice = 180.0, defaultTags = listOf("白光发热芯", "秒速升温", "休眠唤醒", "恒温烙铁")),
        LocalHeuristicEngine.CategoryTermWeight("C245极速高功率焊台", 2.0, "台", "工作台/焊接操作区", defaultPrice = 320.0, defaultTags = listOf("JBC兼容", "2秒化锡", "130W大功率", "精密贴片焊接")),
        LocalHeuristicEngine.CategoryTermWeight("858D数显热风拆焊枪", 2.0, "台", "工作台/焊接操作区", defaultPrice = 150.0, defaultTags = listOf("旋涡风", "风量温度可调", "QFN/BGA贴片吹焊")),
        LocalHeuristicEngine.CategoryTermWeight("恒温微型加热台", 1.9, "台", "工作台/焊接操作区", defaultPrice = 120.0, defaultTags = listOf("预热台", "分体拆焊", "LED灯珠返修台")),
        LocalHeuristicEngine.CategoryTermWeight("优质无铅焊锡丝", 1.8, "卷", "工作台/焊接操作区", defaultPrice = 45.0, defaultTags = listOf("0.8mm/0.5mm", "含银低温锡", "助焊松香芯", "焊点饱满")),
        LocalHeuristicEngine.CategoryTermWeight("无酸中性助焊膏/松香", 1.7, "盒", "工作台/焊接操作区", defaultPrice = 15.0, defaultTags = listOf("免清洗焊油", "助焊剂针筒", "消除虚焊氧化")),
        LocalHeuristicEngine.CategoryTermWeight("吸锡带吸锡绳", 1.7, "卷", "工作台/焊接操作区", defaultPrice = 12.0, defaultTags = listOf("编织纯铜", "清除引脚连锡残锡")),
        LocalHeuristicEngine.CategoryTermWeight("全金属强力吸锡枪", 1.7, "把", "工作台/焊接操作区", defaultPrice = 18.0, defaultTags = listOf("双环吸锡器", "直插元件快速解焊")),
        LocalHeuristicEngine.CategoryTermWeight("ESD防静电精密镊子套装", 1.8, "套", "工作台/贴片操作区", defaultPrice = 28.0, defaultTags = listOf("ESD-10至15", "直头/弯头/扁头", "贴片夹持")),

        // === 2. 电子测量仪器与逻辑抓包 ===
        LocalHeuristicEngine.CategoryTermWeight("Fluke福禄克数字万用表", 2.0, "台", "工作台/仪器架", defaultPrice = 850.0, defaultTags = listOf("真有效值TrueRMS", "自动量程", "蜂鸣通断档", "高精度")),
        LocalHeuristicEngine.CategoryTermWeight("RIGOL普源双通道数字示波器", 2.0, "台", "工作台/仪器架", defaultPrice = 1680.0, defaultTags = listOf("DS1054Z", "100MHz带宽", "1GSa/s采样", "波形分析")),
        LocalHeuristicEngine.CategoryTermWeight("Saleae 8通道USB逻辑分析仪", 1.9, "台", "工作台/仪器架", defaultPrice = 58.0, defaultTags = listOf("24MHz采样", "I2C/SPI/UART/CAN协议解码", "数字总线抓包")),
        LocalHeuristicEngine.CategoryTermWeight("30V 5A数显直流可调稳压电源", 2.0, "台", "工作台/仪器架", defaultPrice = 240.0, defaultTags = listOf("四位精准数显", "恒压恒流CC/CV", "短路蜂鸣报警")),
        LocalHeuristicEngine.CategoryTermWeight("LCR-T4晶体管元器件图形测试仪", 1.8, "台", "工作台/测试架", defaultPrice = 48.0, defaultTags = listOf("自动识别三极管/MOS/二极管/电感/电容", "显示引脚图")),
        LocalHeuristicEngine.CategoryTermWeight("高精度数显不锈钢游标卡尺", 1.9, "把", "工作台/测量抽屉", defaultPrice = 55.0, defaultTags = listOf("0-150mm", "0.01mm分辨率", "外径/内径/深度测量")),

        // === 3. 3D打印与数控激光雕刻 ===
        LocalHeuristicEngine.CategoryTermWeight("拓竹Bambu Lab 3D打印机", 2.0, "台", "工作室/3D打印区", defaultPrice = 3999.0, defaultTags = listOf("X1-Carbon/P1S", "高速多色AMS", "自动调平", "振动补偿")),
        LocalHeuristicEngine.CategoryTermWeight("拓竹A1 mini桌面3D打印机", 2.0, "台", "工作室/3D打印区", defaultPrice = 1599.0, defaultTags = listOf("全自动免调平", "静音电机", "多色打印")),
        LocalHeuristicEngine.CategoryTermWeight("PLA+环保3D打印耗材", 1.8, "卷", "工作室/3D耗材架", defaultPrice = 55.0, defaultTags = listOf("1.75mm", "高韧性无翘边", "1kg真空包装")),
        LocalHeuristicEngine.CategoryTermWeight("PETG耐热3D打印耗材", 1.8, "卷", "工作室/3D耗材架", defaultPrice = 60.0, defaultTags = listOf("耐温防水", "高强度结构件")),
        LocalHeuristicEngine.CategoryTermWeight("TPU高弹柔性3D打印耗材", 1.8, "卷", "工作室/3D耗材架", defaultPrice = 75.0, defaultTags = listOf("95A硬度", "减震密封垫圈")),
        LocalHeuristicEngine.CategoryTermWeight("双面纹理PEI弹簧钢板", 1.8, "张", "工作室/3D打印区", defaultPrice = 45.0, defaultTags = listOf("磁吸热床板", "易粘易取模", "金粉纹理")),
        LocalHeuristicEngine.CategoryTermWeight("硬化钢耐磨喷嘴", 1.8, "只", "工作室/3D备件盒", defaultPrice = 25.0, defaultTags = listOf("0.4mm/0.6mm", "耐磨抗碳纤维摩擦")),
        LocalHeuristicEngine.CategoryTermWeight("CNC 3018桌面雕刻机", 1.9, "台", "工作室/数控加工区", defaultPrice = 850.0, defaultTags = listOf("PCB刻板", "亚克力切割", "铝板雕刻", "GRBL主控")),
        LocalHeuristicEngine.CategoryTermWeight("PCB平底尖刀雕刻铣刀", 1.7, "盒", "工作室/刀具盒", defaultPrice = 30.0, defaultTags = listOf("钨钢硬质合金", "3.175mm柄径", "0.1mm 30度")),

        // === 4. 五金紧固与精密装配手工工具 ===
        LocalHeuristicEngine.CategoryTermWeight("米家Wiha精密螺丝刀套装", 1.9, "套", "工作台/装配抽屉", defaultPrice = 89.0, defaultTags = listOf("24合1批头", "S2合金钢", "铝合金磁吸收纳盒")),
        LocalHeuristicEngine.CategoryTermWeight("南旗22合1精密批头螺丝刀", 1.9, "套", "工作台/装配抽屉", defaultPrice = 110.0, defaultTags = listOf("加长批头", "手机数码拆机专用")),
        LocalHeuristicEngine.CategoryTermWeight("SN-28B多功能端子冷压钳", 1.9, "把", "工作台/压线工具架", defaultPrice = 58.0, defaultTags = listOf("杜邦端子/XH2.54/PH2.0专用棘轮压线钳")),
        LocalHeuristicEngine.CategoryTermWeight("宝工自动剥线钳", 1.8, "把", "工作台/工具挂架", defaultPrice = 65.0, defaultTags = listOf("鸭嘴多功能", "一秒无伤线芯剥皮")),
        LocalHeuristicEngine.CategoryTermWeight("PLATO 170电子斜口剪钳", 1.8, "把", "工作台/工具挂架", defaultPrice = 9.9, defaultTags = listOf("平口齐平剪切", "引脚水口修剪")),
        LocalHeuristicEngine.CategoryTermWeight("无线锂电热熔胶枪", 1.8, "把", "工作台/固定辅料区", defaultPrice = 58.0, defaultTags = listOf("快速出胶", "高粘性无毒胶棒", "结构固定封装")),
        LocalHeuristicEngine.CategoryTermWeight("Dremel琢美电动打磨微型电磨", 1.9, "台", "工作室/机械加工区", defaultPrice = 280.0, defaultTags = listOf("雕刻打磨抛光切割", "多档调速配件套装")),
        LocalHeuristicEngine.CategoryTermWeight("Bosch博世12V锂电手电钻/起子机", 2.0, "台", "工作室/重型工具柜", defaultPrice = 399.0, defaultTags = listOf("双速调节", "扭力离合", "五金打孔拧螺丝")),
        LocalHeuristicEngine.CategoryTermWeight("M3不锈钢内六角杯头螺丝套装", 1.8, "盒", "工作室/紧固件多格柜", defaultPrice = 28.0, defaultTags = listOf("M3*6至M3*25", "配平垫弹垫防松螺母", "304不锈钢")),
        LocalHeuristicEngine.CategoryTermWeight("M2/M2.5精密黑锌螺丝盒", 1.8, "盒", "工作室/紧固件多格柜", defaultPrice = 22.0, defaultTags = listOf("数码外壳", "平头沉头十字")),
        LocalHeuristicEngine.CategoryTermWeight("黄铜热熔滚花螺母预埋件", 1.9, "盒", "工作室/3D打印辅料盒", defaultPrice = 25.0, defaultTags = listOf("M3*4*5", "M2/M4", "电烙铁热压嵌入塑料件")),
        LocalHeuristicEngine.CategoryTermWeight("M3单通/双通尼龙隔离柱螺母", 1.8, "盒", "工作室/紧固件多格柜", defaultPrice = 18.0, defaultTags = listOf("电路板PCB绝缘支撑垫高柱")),
        LocalHeuristicEngine.CategoryTermWeight("乐泰Loctite 242中强度螺纹锁固胶", 1.8, "瓶", "工作台/胶水盒", defaultPrice = 25.0, defaultTags = listOf("防震防松", "可手工拆卸厌氧胶")),
        LocalHeuristicEngine.CategoryTermWeight("卡夫特704硅橡胶绝缘密封胶", 1.7, "支", "工作台/胶水盒", defaultPrice = 8.5, defaultTags = listOf("电子灌封", "防水绝缘耐温白胶"))
    )
}
