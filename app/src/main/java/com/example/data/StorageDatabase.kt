package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StorageItem::class,
        StorageContainer::class,
        StorageCategory::class,
        LocationNode::class,
        AiRecognitionCache::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StorageDatabase : RoomDatabase() {
    abstract fun storageItemDao(): StorageItemDao
    abstract fun storageContainerDao(): StorageContainerDao
    abstract fun storageCategoryDao(): StorageCategoryDao
    abstract fun locationNodeDao(): LocationNodeDao
    abstract fun aiRecognitionCacheDao(): AiRecognitionCacheDao

    companion object {
        @Volatile
        private var INSTANCE: StorageDatabase? = null

        fun getInstance(context: Context): StorageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StorageDatabase::class.java,
                    "storage_pro_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: StorageDatabase) {
            val defaultCategories = listOf(
                StorageCategory(name = "电子数码", icon = "devices", colorHex = "#3B82F6"),
                StorageCategory(name = "日常居家", icon = "home", colorHex = "#10B981"),
                StorageCategory(name = "服饰箱包", icon = "checkroom", colorHex = "#EC4899"),
                StorageCategory(name = "食品饮料", icon = "restaurant", colorHex = "#F59E0B"),
                StorageCategory(name = "药品个护", icon = "medication", colorHex = "#EF4444"),
                StorageCategory(name = "户外露营", icon = "camping", colorHex = "#8B5CF6"),
                StorageCategory(name = "重要证件", icon = "badge", colorHex = "#6366F1"),
                StorageCategory(name = "美妆护肤", icon = "spa", colorHex = "#F43F5E"),
                StorageCategory(name = "工具五金", icon = "build", colorHex = "#64748B"),
                StorageCategory(name = "电子元器件", icon = "memory", colorHex = "#10B981"),
                StorageCategory(name = "文具书籍", icon = "menu_book", colorHex = "#06B6D4"),
                StorageCategory(name = "运动健身", icon = "fitness_center", colorHex = "#F97316"),
                StorageCategory(name = "办公耗材", icon = "print", colorHex = "#0284C7"),
                StorageCategory(name = "家居园艺", icon = "yard", colorHex = "#22C55E"),
                StorageCategory(name = "乐器影音", icon = "music_note", colorHex = "#A855F7"),
                StorageCategory(name = "汽车用品", icon = "directions_car", colorHex = "#6366F1"),
                StorageCategory(name = "模型收藏", icon = "smart_toy", colorHex = "#EAB308"),
                StorageCategory(name = "家用电器", icon = "kitchen", colorHex = "#14B8A6")
            )
            database.storageCategoryDao().insertAll(defaultCategories)

            val defaultContainers = listOf(
                StorageContainer(
                    name = "卧室衣柜·01号整理箱",
                    locationPath = "主卧/衣柜/顶层",
                    boxCode = "BOX-2026-001",
                    iconName = "inventory_2",
                    colorHex = "#3B82F6",
                    notes = "存放过季衣物与羽绒服"
                ),
                StorageContainer(
                    name = "家庭急救医药箱",
                    locationPath = "客厅/电视柜/右侧抽屉",
                    boxCode = "BOX-2026-002",
                    iconName = "medical_services",
                    colorHex = "#EF4444",
                    notes = "感冒药、创口贴、体温计、消毒液"
                ),
                StorageContainer(
                    name = "户外露营装备大拉杆箱",
                    locationPath = "储物间/置物架B/底层",
                    boxCode = "BOX-2026-003",
                    iconName = "hiking",
                    colorHex = "#10B981",
                    notes = "帐篷、充气垫、露营灯、钛杯"
                )
            )
            database.storageContainerDao().insertAll(defaultContainers)

            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000
            val sampleItems = listOf(
                StorageItem(
                    name = "降噪头戴式耳机 XM5",
                    category = "电子数码",
                    locationPath = "书房/书桌/右侧挂钩",
                    quantity = 1,
                    price = 1999.0,
                    tags = "数码,音频,出差必备",
                    notes = "附带收纳盒与音频线"
                ),
                StorageItem(
                    name = "家用消毒碘伏喷雾",
                    category = "药品个护",
                    locationPath = "客厅/电视柜/右侧抽屉",
                    containerId = 2,
                    quantity = 2,
                    unit = "瓶",
                    price = 18.5,
                    expiryDate = now + 15 * dayMillis, // 15 days left - alert!
                    tags = "常备药品,临期预警",
                    notes = "已开封，请于30天内使用完"
                ),
                StorageItem(
                    name = "户外双人充气防潮垫",
                    category = "户外露营",
                    locationPath = "储物间/置物架B/底层",
                    containerId = 3,
                    quantity = 1,
                    price = 320.0,
                    tags = "露营,睡垫",
                    notes = "带脚踏充气阀"
                ),
                StorageItem(
                    name = "N95防护口罩 (50只独立装)",
                    category = "药品个护",
                    locationPath = "玄关/鞋柜上方收纳盒",
                    quantity = 50,
                    unit = "只",
                    price = 45.0,
                    expiryDate = now + 180 * dayMillis,
                    tags = "防护用品,消耗品"
                )
            )
            database.storageItemDao().insertAll(sampleItems)
        }
    }
}
